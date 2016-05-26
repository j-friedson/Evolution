package evo.strategy;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import evo.game.*;
import evo.game.list.PlayerList;
import evo.game.list.SpeciesList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by jackfriedson on 4/24/16.
 */
public class GameData {
  private static GameData gameData;

  public static final String DATA_FILE = "/Users/jackfriedson/Google Drive/Old/2016 Spring/School/"
          + "Software Development/cs4500-jackfri-chrisluk/14/strategy/game-data.json";

  private static JsonReader reader;
  private static JsonWriter writer;

  private double avgNumBoards;
  private double avgPopSize;
  private double avgBodySize;
  private int numBoardEntries;
  private int numPopEntries;
  private int numBodyEntries;
  private int numTraitEntries;
  private Map<TraitName, Double> traitValueMap;

  private GameData() {
    this.avgNumBoards = 0;
    this.avgPopSize = 0;
    this.avgBodySize = 0;
    this.numBoardEntries = 0;
    this.numPopEntries = 0;
    this.numBodyEntries = 0;
    this.numTraitEntries = 0;
    this.traitValueMap = new HashMap<>();
    for (TraitName tn : TraitName.values()) traitValueMap.put(tn, 0.0);
  }

  public static GameData getGameData() {
    initStreams();
    if (gameData == null) { gameData = new GameData(); }
    return gameData;
  }

  public static void initStreams() {
    try {
      reader = new JsonReader(new FileReader(DATA_FILE));
      writer = new JsonWriter(new FileWriter(DATA_FILE));
      writer.setIndent("\t");
    } catch (IOException e) { throw new RuntimeException(e); }
  }

  public static GameData readDataFile() {
    try {
      BufferedReader br = new BufferedReader(new FileReader(DATA_FILE));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) sb.append(line);
      String jsonData = sb.toString();
      if (jsonData.equals("")) return getGameData();
      else return getGson().fromJson(sb.toString(), GameData.class);
    } catch (IOException e) { throw new RuntimeException(e); }
  }

  private static Gson getGson() {
    return new GsonBuilder()
            .registerTypeAdapter(GameData.class, new GameDataDeserializer())
            .setPrettyPrinting()
            .create();
  }

  public void addData(PlayerList players) {
    List<PlayerScore> rankings = players.getRankings();
    int sum = 0;
    for (PlayerScore ps : rankings) sum += ps.getScore();
    int avgScore = sum / rankings.size();

    EvoPlayer winner = players.get(rankings.get(0).getId() - 1);
    SpeciesList boards = winner.getBoards();
    int winningScore = rankings.get(0).getScore();

    for (int i = 0; i < winningScore - avgScore; i++) {
      avgNumBoards = ((avgNumBoards * numBoardEntries++) + boards.size()) / numBoardEntries;
      for (SpeciesBoard sb : boards) {
        avgPopSize = ((avgPopSize * numPopEntries++) + sb.getPopulationSize()) / numPopEntries;
        avgBodySize = ((avgBodySize * numBodyEntries++) + sb.getBodySize()) / numBodyEntries;
        for (TraitCard tc : sb.getTraitCards()) {
          numTraitEntries++;
          traitValueMap.put(tc.getName(), traitValueMap.get(tc.getName()) + 1);
        }
      }
    }

    // ensure there are no duplicate values
    Set<Double> vals = new HashSet<>();
    for (TraitName tn : traitValueMap.keySet()) {
      double val = traitValueMap.get(tn);
      if (!vals.add(val))
        traitValueMap.put(tn, val + 0.1);
    }
  }

  public void saveToFile() {
    getGson().toJson(this, GameData.class, writer);
    try { writer.flush(); }
    catch (IOException e) { throw new RuntimeException(e); }
  }

  public static void closeFile() {
    try {
      writer.flush();
      reader.close();
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public double getAvgNumBoards() { return avgNumBoards; }
  public double getAvgPopSize() { return avgPopSize; }
  public double getAvgBodySize() { return avgBodySize; }

  public int getTraitValue(TraitCard tc) {
    return (int)(100 * (traitValueMap.get(tc.getName()) / (double)numTraitEntries)) - tc.getFoodPoints();
  }

  public double getTraitValueDbl(TraitCard tc) {
    return (100 * (traitValueMap.get(tc.getName()) / (double)numTraitEntries)) - tc.getFoodPoints();
  }

  private static class GameDataDeserializer implements JsonDeserializer<GameData> {

    @Override
    public GameData deserialize(JsonElement jsonElement, Type type,
                                JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonObject obj = jsonElement.getAsJsonObject();

      double avgBoards = obj.get("avgNumBoards").getAsDouble();
      double avgPop = obj.get("avgPopSize").getAsDouble();
      double avgBody = obj.get("avgBodySize").getAsDouble();
      int boardEntries = obj.get("numBoardEntries").getAsInt();
      int popEntries = obj.get("numPopEntries").getAsInt();
      int bodyEntries = obj.get("numBodyEntries").getAsInt();
      int traitEntries = obj.get("numTraitEntries").getAsInt();

      JsonObject traitData = obj.get("traitValueMap").getAsJsonObject();
      Map<TraitName, Double> traitMap = new HashMap<>();
      for (Map.Entry<String, JsonElement> e : traitData.entrySet())
        traitMap.put(TraitName.fromString(e.getKey()), e.getValue().getAsDouble());

      GameData data = getGameData();
      data.avgNumBoards = avgBoards;
      data.avgPopSize = avgPop;
      data.avgBodySize = avgBody;
      data.numBoardEntries = boardEntries;
      data.numPopEntries = popEntries;
      data.numBodyEntries = bodyEntries;
      data.numTraitEntries = traitEntries;
      data.traitValueMap = traitMap;
      return data;
    }
  }
}