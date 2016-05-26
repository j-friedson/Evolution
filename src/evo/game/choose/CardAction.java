package evo.game.choose;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import evo.game.BasePlayer;
import evo.game.list.CardList;
import evo.game.EvoPlayer;
import evo.json.JFactory;
import evo.json.JSerializable;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by jackfriedson on 3/31/16.
 */
public abstract class CardAction implements JSerializable {
  protected final int cardIdx;

  protected CardAction(int cardIdx) {
    this.cardIdx = cardIdx;
  }

  /**
   * Gets the index of the card used in this {@link CardAction} (to be removed from the player's
   * hand).
   *
   * Effect: performs the necessary modifcation to the given {@link BasePlayer}
   *
   * @param player the {@link BasePlayer} to modify
   * @param newHand the {@link CardList} that will become the player's new hand
   */
  public abstract void doAction(BasePlayer player, CardList newHand);

  /**
   * Verifies that this {@link CardAction} is valid, given the {@link EvoPlayer} that it
   * corresponds to
   *
   * @param player the {@link EvoPlayer} that this {@link CardAction} corresponds to
   * @return true if this is a valid {@link CardAction}
   */
  protected boolean isValid(BasePlayer player) {
    return this.cardIdx >= 0 && this.cardIdx < player.getHand().size();
  }

  public void removeCardsFrom(Collection<Integer> cards) {
    if (!cards.remove(cardIdx)) throw new IllegalStateException("Card not present");
  }

  public abstract void addToAction4(Action4 action4);

  /******************
   * Getter methods *
   ******************/
  public int getCardIdx() {
    return cardIdx;
  }

  @Override
  public JsonElement toJson() {
    return JFactory.getGson().toJsonTree(this, this.getClass());
  }

  @Override
  public void toJson(JsonWriter writer) throws IOException {
    JFactory.getGson().toJson(this, this.getClass(), writer);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CardAction that = (CardAction) o;

    return getCardIdx() == that.getCardIdx();
  }

  @Override
  public int hashCode() {
    return getCardIdx();
  }
}