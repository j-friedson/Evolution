package evo.game.list;

import com.google.gson.*;
import evo.game.SpeciesBoard;
import evo.game.Utils;
import evo.json.JFactory;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 3/24/16.
 */
public class SpeciesList extends EvoList<SpeciesBoard> {

  public SpeciesList() {
    super(true);
  }

  public SpeciesList(List<SpeciesBoard> boards) {
    super(boards, true);
  }

  public SpeciesList(SpeciesList boards, Collection<Integer> idxs) {
    super(Utils.map(idxs, boards::get), true);
  }

  @Override
  public SpeciesList makeCopy() {
    SpeciesList result = new SpeciesList();
    this.forEach(sb -> result.add(sb.makeCopy()));
    return result;
  }

  @Override
  public SpeciesList filterBy(Predicate<SpeciesBoard> pred) {
    SpeciesList result = new SpeciesList();
    this.forEach(sb -> { if (pred.test(sb)) result.add(sb); });
    return result;
  }

  public int getFirstWith(Predicate<SpeciesBoard> pred) {
    for (int i = 0; i < size(); i++)
      if (pred.test(get(i))) return i;
    return -1;
  }

  public int getLastWith(Predicate<SpeciesBoard> pred) {
    for (int i = size() - 1; i >= 0; i--)
      if (pred.test(get(i))) return i;
    return -1;
  }

  /**
   * Gets the list of indices of all {@link SpeciesBoard}s that pass the given {@link Predicate}
   *
   * @param pred the {@link Predicate} to test each {@link SpeciesBoard}
   * @return the list of indices corresponding to the {@link SpeciesBoard}s in this
   *         {@link SpeciesList}
   */
  public List<Integer> getAllWith(Predicate<SpeciesBoard> pred) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < this.contents.size(); i++) {
      if (pred.test(this.contents.get(i)))
        result.add(i);
    }
    return result;
  }

  public List<Integer> getAllWithIdx(Predicate<Integer> pred) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < this.contents.size(); i++)
      if (pred.test(i))
        result.add(i);
    return result;
  }

  public Optional<Integer> getTargetAttackableIdx(SpeciesBoard attacker,
                                                  Comparator<SpeciesBoard> comp) {
    Optional<Integer> result = Optional.empty();
    for (int i = 0; i < this.size(); i++) {
      SpeciesBoard s = this.get(i);
      if (attacker.canAttack(s, leftNeighborOf(i), rightNeighborOf(i))
              && (!result.isPresent() || s.compareToThat(this.get(result.get()), comp)))
        result = Optional.of(i);
    }
    return result;
  }

  public Optional<SpeciesBoard> rightNeighborOf(int speciesIdx) {
    if (speciesIdx < 0 || speciesIdx >= this.size())
      throw new IndexOutOfBoundsException("The given SpeciesBoard must be in the list.");

    int neighborIdx = speciesIdx + 1;
    if (neighborIdx < this.size()) return Optional.of(this.get(neighborIdx));
    else return Optional.empty();
  }

  public Optional<SpeciesBoard> leftNeighborOf(int speciesIdx) {
    if (speciesIdx < 0 || speciesIdx >= this.size())
      throw new IndexOutOfBoundsException("The given SpeciesBoard must be in the list.");

    int neighborIdx = speciesIdx - 1;
    if (neighborIdx >= 0) return Optional.of(this.get(neighborIdx));
    else return Optional.empty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SpeciesList that = (SpeciesList) o;

    return contents.equals(that.contents);
  }

  @Override
  public int hashCode() {
    return contents.hashCode();
  }

  @Override
  public String toString() {
    return "SpeciesList{" +
            "boards=" + contents +
            '}';
  }

  /**
   * A Boards is [Species+,...,Species+].
   */

  public static JsonDeserializer deserializer() { return new SpeciesListDeserializer(); }

  private static class SpeciesListDeserializer implements JsonDeserializer<SpeciesList> {
    @Override
    public SpeciesList deserialize(JsonElement jsonElement, Type type,
                                   JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
      JsonArray jsonList = jsonElement.getAsJsonArray();
      SpeciesList result = new SpeciesList();
      for (JsonElement j : jsonList)
        result.add(JFactory.fromJson(j, SpeciesBoard.class));
      if (!result.isValid()) throw new JsonParseException("Invalid SpeciesList: " + result.toString());
      else return result;
    }
  }
}
