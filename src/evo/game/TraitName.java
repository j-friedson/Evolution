package evo.game;


import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import evo.json.JFactory;
import evo.json.JSerializable;

import java.io.IOException;
import java.lang.reflect.Type;

public enum TraitName implements JSerializable {
  CARNIVORE,
  AMBUSH,
  BURROWING,
  CLIMBING,
  COOPERATION,
  FAT_TISSUE,
  FERTILE,
  FORAGING,
  HARD_SHELL,
  HERDING,
  HORNS,
  LONG_NECK,
  PACK_HUNTING,
  SCAVENGER,
  SYMBIOSIS,
  WARNING_CALL;

  @Override
  public String toString() {
    String str = this.name().contains("_") ? this.name().replace('_', '-') : this.name();
    return str.toLowerCase();
  }

  public static TraitName fromString(String str) {
    switch (str) {
      case "carnivore":
        return CARNIVORE;
      case "ambush":
        return AMBUSH;
      case "burrowing":
        return BURROWING;
      case "climbing":
        return CLIMBING;
      case "cooperation":
        return COOPERATION;
      case "fat-tissue":
        return FAT_TISSUE;
      case "fertile":
        return FERTILE;
      case "foraging":
        return FORAGING;
      case "hard-shell":
        return HARD_SHELL;
      case "herding":
        return HERDING;
      case "horns":
        return HORNS;
      case "long-neck":
        return LONG_NECK;
      case "pack-hunting":
        return PACK_HUNTING;
      case "scavenger":
        return SCAVENGER;
      case "symbiosis":
        return SYMBIOSIS;
      case "warning-call":
        return WARNING_CALL;
      default:
        throw new IllegalArgumentException(str + " is not a valid trait name.");
    }
  }

  @Override
  public JsonElement toJson() {
    return JFactory.getGson().toJsonTree(this, this.getClass());
  }

  @Override
  public void toJson(JsonWriter writer) throws IOException {
    JFactory.getGson().toJson(this, this.getClass(), writer);
  }

  public static TraitNameSerializer serializer() { return new TraitNameSerializer(); }

  private static class TraitNameSerializer implements JsonSerializer<TraitName> {
    @Override
    public JsonElement serialize(TraitName traitName, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      return new JsonPrimitive(this.toString());
    }
  }
}
