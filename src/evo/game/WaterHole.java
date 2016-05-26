package evo.game;

/**
 * Created by jackfriedson on 3/13/16.
 */
public class WaterHole {
  private int food;

  public WaterHole() {
    this.food = 0;
  }

  public WaterHole(int food) { this.food = food; }

  /**
   * Determines whether this {@link WaterHole} is empty
   *
   * @return true if this {@link WaterHole} is empty
   */
  public boolean isEmpty() {
    return food == 0;
  }

  /**
   * Effect: decrements this {@link WaterHole} supply and returns 1 (if possible)
   *
   * @return 1
   */
  public int takeOne() {
    if (isEmpty()) throw new IllegalStateException("The watering hole is empty");
    else {
      food--;
      return 1;
    }
  }

  /**
   * Effect: adds the given amount of (positive or negative) bag to this {@link WaterHole},
   *         ensuring that it doesn't go below 0
   *
   * @param toAdd the number of bag tokens to add
   */
  public void addFood(int toAdd) {
    int newFood = food + toAdd;
    food = newFood < 0 ? 0 : newFood;
  }

  /******************
   * Getter methods *
   ******************/
  public int getFood() {
    return food;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    WaterHole waterHole = (WaterHole) o;

    return getFood() == waterHole.getFood();

  }

  @Override
  public int hashCode() {
    return getFood();
  }

  @Override
  public String toString() {
    return "WaterHole{" +
            "food=" + food +
            '}';
  }
}
