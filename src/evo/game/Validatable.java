package evo.game;

/**
 * Created by jackfriedson on 4/2/16.
 */
public interface Validatable {

  /**
   * Determines whether this object is valid, according various game invariants.
   *
   * @return true if this is a valid object, false if it is not
   */
  boolean isValid();
}
