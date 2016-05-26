package evo.game;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import evo.json.JFactory;
import evo.json.JSerializable;

import java.io.IOException;

/**
 * Created by jackfriedson on 4/22/16.
 */
public abstract class EvoElement implements Validatable, JSerializable {

  @Override
  public JsonElement toJson() {
    return JFactory.getGson().toJsonTree(this, this.getClass());
  }

  @Override
  public void toJson(JsonWriter writer) throws IOException {
    JFactory.getGson().toJson(this, this.getClass(), writer);
  }
}
