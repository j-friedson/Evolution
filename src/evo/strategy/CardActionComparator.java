package evo.strategy;


import evo.game.BasePlayer;
import evo.game.choose.CardAction;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/28/16.
 */
public class CardActionComparator implements Comparator<CardAction> {
  private final BasePlayer base;
  private final Comparator<BasePlayer> playerComp; // <- sorts best player to the front

  public CardActionComparator(BasePlayer base, Comparator<BasePlayer> playerComp) {
    this.base = base;
    this.playerComp = playerComp;
  }

  @Override
  public int compare(CardAction o1, CardAction o2) {
    BasePlayer p1 = base.copy();
    BasePlayer p2 = base.copy();
    o1.doAction(p1, p1.getHand().makeCopy());
    o2.doAction(p2, p2.getHand().makeCopy());
    return playerComp.compare(p1, p2);
  }
}