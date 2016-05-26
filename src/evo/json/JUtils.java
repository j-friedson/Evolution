package evo.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import evo.game.BasePlayer;
import evo.game.EvoElement;
import evo.game.list.SpeciesList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jackfriedson on 4/1/16.
 */
public class JUtils {

  public static void writeIntField(JsonWriter writer, String name, int val) throws IOException {
    writer.beginArray();
    writer.value(name);
    writer.value(val);
    writer.endArray();
  }

  public static int readIntField(JsonReader reader, String expected) throws IOException {
    reader.beginArray();
    String actual = reader.nextString();
    if (!actual.equals(expected))
      throw new JsonParseException("Expected: " + expected + " Actual: " + actual);
    int val = reader.nextInt();
    reader.endArray();
    return val;
  }

  /**
   * Reads the next natural number from the give {@link JsonReader}
   *
   * @param reader the {@link JsonReader} to read from
   * @return the natural number
   */
  public static int getNat(JsonReader reader) {
    try {
      int i = reader.nextInt();
      if (i < 0) throw new JsonParseException("Natural numbers cannot be negative");
      else return i;
    }  catch (IOException io) {
      throw new JsonParseException("Could not parse nat: " + io.getMessage());
    }
  }

  public static JsonArray getArrayFromField(JsonArray jArray, String expected, int idx) {
    JsonArray field = jArray.get(idx).getAsJsonArray();
    String name = field.get(0).getAsString();
    JsonArray value = field.get(1).getAsJsonArray();
    if (!name.equals(expected)) throw new JsonParseException("Field name was: " + name +
            " but expected: " + expected);
    return value;
  }

  public static int getIntFromField(JsonArray jArray, String expected, int idx) {
    JsonArray field = jArray.get(idx).getAsJsonArray();
    String name = field.get(0).getAsString();
    int value = field.get(1).getAsInt();
    if (!name.equals(expected)) throw new JsonParseException("Field name was: " + name +
            " but expected: " + expected);
    return value;
  }

  public static JsonArray makeIntField(String name, int value) {
    JsonArray result = new JsonArray();
    result.add(name);
    result.add(new JsonPrimitive(value));
    return result;
  }

  public static JsonArray makeListField(String name, Iterable<? extends JSerializable> values) {
    JsonArray result = new JsonArray();
    result.add(name);
    result.add(listToJArray(values));
    return result;
  }

  public static JsonArray makeJsonField(String name, JsonElement value) {
    JsonArray result = new JsonArray();
    result.add(name);
    result.add(value);
    return result;
  }

  public static JsonArray listToJArray(Iterable<? extends JSerializable> values) {
    JsonArray result = new JsonArray();
    values.forEach(s -> result.add(s.toJson()));
    return result;
  }

  public static void writeStartMsg(JsonWriter writer, int wh, BasePlayer player)
          throws IOException {
    writer.beginArray();
    writer.value(wh);
    writer.value(player.getBag());
    writeList(writer, player.getBoards());
    writeList(writer, player.getHand());
    writer.endArray();
    writer.flush();
  }

  public static void writeChooseMsg
  (JsonWriter writer, List<SpeciesList> before, List<SpeciesList> after) throws IOException {
    writer.beginArray();
    writeList(writer, before);
    writeList(writer, after);
    writer.endArray();
    writer.flush();
  }

  public static void writeFeedNextMsg(JsonWriter writer, BasePlayer player,
                                      List<SpeciesList> allBoards, int tokens) throws IOException {
    writer.beginArray();
    writer.value(player.getBag());
    writeList(writer, player.getBoards());
    writeList(writer, player.getHand());
    writer.value(tokens);
    writeList(writer, allBoards);
    writer.endArray();
    writer.flush();
  }

  private static <U extends Iterable<? extends EvoElement>>
  void writeList(JsonWriter writer, U list) throws IOException {
    writer.beginArray();
    for (EvoElement t : list) t.toJson(writer);
    writer.endArray();
  }

  public static List<SpeciesList> readLOB(JsonReader reader) throws IOException {
    return readList(reader, SpeciesList.class);
  }

  public static <T extends JSerializable>
  List<T> readList(JsonReader reader, Class<T> classOfT) throws IOException {
    List<T> result = new ArrayList<>();
    reader.beginArray();

    while (reader.hasNext())
      result.add(JFactory.fromJson(reader, classOfT));

    reader.endArray();
    return result;
  }
}
