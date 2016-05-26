package evo.json;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Created by jackfriedson on 3/26/16.
 */
public interface JSerializable {

  /**
   * Note: All JSerializable classes also implement the static method serialize() and most
   *       implement the static method deserialize(), which return the specific serializer and
   *       deserializer for that class.
   */

  /**
   * Serializes this {@link JSerializable} object to its correct JSON representation
   *
   * @return a {@link JsonElement} representing this object
   */
  JsonElement toJson();

  /**
   * Serializes this {@link JSerializable} object to an output stream, represented by the given
   * {@link JsonWriter}
   *
   * @param writer the {@link JsonWriter} to write this object to (as JSON)
   * @throws IOException if writing to the output stream throws an exception
   */
  void toJson(JsonWriter writer) throws IOException;
}
