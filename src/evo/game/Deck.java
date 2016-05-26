package evo.game;

import evo.Constants;
import evo.game.compare.TraitCardLexOrder;
import evo.game.list.CardList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by jackfriedson on 3/12/16.
 */
public class Deck {
  private final CardList contents;

  public Deck() {
    this.contents = new CardList();
    init();
    contents.sort(new TraitCardLexOrder());
  }

  public Deck(CardList contents) {
    this.contents = Objects.requireNonNull(contents);
  }

  public Deck(List<TraitCard> contents) {
    this.contents = new CardList(Objects.requireNonNull(contents));
  }

  /**
   * Determines the size of the deck
   *
   * @return the number of cards remaining in the deck
   */
  public int size() { return this.contents.size(); }

  /**
   * Attempts to draw the given number of {@link TraitCard}s from this {@link Deck}, and return
   * them as a list. If the requested number of cards are not in the deck, returns whatever is in
   * the deck instead.
   *
   * Effect: removes the given number of {@link TraitCard}s from the {@link Deck}
   *
   * @param numCards the number of {@link TraitCard}s to draw
   * @return the list of drawn {@link TraitCard}s
   */
  public List<TraitCard> draw(int numCards) {
    List<TraitCard> result = new ArrayList<>();

    int i = 0;
    while (!this.isEmpty() && i++ < numCards)
      result.add(contents.remove(0));

    return result;
  }

  /**
   * Effect: initializes this {@link Deck} to contain every card in the game
   */
  private void init() {
    for (int i = Constants.MIN_CARN_FOODVAL; i <= Constants.MAX_CARN_FOODVAL; i++)
      contents.add(new TraitCard(i, TraitName.CARNIVORE));

    for (int i = Constants.MIN_VEG_FOODVAL; i <= Constants.MAX_VEG_FOODVAL; i++)
      for (TraitName tn : TraitName.values())
          if (tn != TraitName.CARNIVORE) contents.add(new TraitCard(i, tn));
  }

  /**
   * Effect: shuffles this {@link Deck}
   */
  public void shuffle() { contents.shuffle(); }

  /**
   * Determines whether this {@link Deck} is empty
   *
   * @return true if this {@link Deck} is empty
   */
  public boolean isEmpty() {
    return contents.isEmpty();
  }

  /******************
   * Getter methods *
   ******************/
  public List<TraitCard> getContents() { return contents.getContents(); }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Deck deck = (Deck) o;

    return getContents().equals(deck.getContents());

  }

  @Override
  public int hashCode() {
    return getContents().hashCode();
  }

  @Override
  public String toString() {
    return "Deck{" +
            "contents=" + contents +
            '}';
  }
}
