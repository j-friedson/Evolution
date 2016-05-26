package evo.game;

/**
 * Created by jackfriedson on 4/4/16.
 */
public class PlayerScore {
  private final int id;
  private final String name;
  private final int score;

  public PlayerScore(int id, String name, int score) {
    this.id = id;
    this.name = name;
    this.score = score;
  }

  public int getId() {
    return id;
  }

  public String getName() { return name; }

  public int getScore() {
    return score;
  }

  @Override
  public String toString() {
    return "ID: " + id + "  Name: " + name +  "  Score: " + score;
  }
}
