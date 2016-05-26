package evo.game;

import evo.game.feed.FeedingException;
import evo.game.feed.IdxPair;
import evo.game.list.SpeciesList;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by jackfriedson on 4/10/16.
 */
public class Utils {

  //todo: docs

  public static <T, U> List<U> map(Collection<T> list, Function<T, U> func) {
    return list.stream().map(func).collect(Collectors.toList());
  }

  public static <T> List<T> filter(List<T> list, Predicate<T> pred) {
    return list.stream().filter(pred).collect(Collectors.toList());
  }

  /**
   * Gets an {@link IdxPair} representing the attackable board that comes first in the
   * order defined in the given {@link Comparator} and the {@link EvoPlayer} that owns it
   *
   * @param attacker the attacking {@link SpeciesBoard}
   * @param otherBoards the {@link SpeciesList}s to search
   * @return an {@link IdxPair} representing the target attackable board and its owner
   */
  public static IdxPair getTargetAttackable(SpeciesBoard attacker, List<SpeciesList> otherBoards,
                                             Comparator<SpeciesBoard> comp) {
    Optional<IdxPair> result = Optional.empty();

    for (int p = 0; p < otherBoards.size(); p++) {
      SpeciesList curList = otherBoards.get(p);
      Optional<Integer> idx = curList.getTargetAttackableIdx(attacker, comp);
      if (idx.isPresent() && (!result.isPresent()
              || curList.get(idx.get()).compareToThat(result.get().getSpecies(otherBoards), comp))) {
        result = Optional.of(new IdxPair(p, idx.get()));
      }
    }
    if (!result.isPresent()) throw new FeedingException("No valid attackable boards");
    return result.get();
  }

  /**
   *
   * @param attacker
   * @param otherBoards
   * @return
   */
  public static List<IdxPair> getAllAttackables(SpeciesBoard attacker, List<SpeciesList> otherBoards) {
    List<IdxPair> result = new ArrayList<>();

    for (int p = 0; p < otherBoards.size(); p++) {
      SpeciesList curList = otherBoards.get(p);
      List<Integer> idxs = curList.getAllWithIdx(i -> attacker.canAttack(curList.get(i),
              curList.leftNeighborOf(i), curList.rightNeighborOf(i)));
      for (Integer s : idxs) result.add(new IdxPair(p, s));
    }
    return result;
  }

  /**
   * Gets a list of indices of all {@link SpeciesBoard}s in the given {@link SpeciesList} that pass
   * the given {@link Predicate}, sorted according to the {@link Comparator} produced by the given
   * {@link Function}
   *
   * @param boards the {@link SpeciesList} of {@link SpeciesBoard}s to filter and sort
   * @param pred the {@link Predicate} to test {@link SpeciesBoard}s with
   * @param func a function that will produce a {@link Comparator<Integer>} from the given list
   *             of boards
   * @return the filtered, sorted list of indices
   */
  public static List<Integer> sortedIdxs(SpeciesList boards, Predicate<SpeciesBoard> pred,
                                         Function<SpeciesList, Comparator<Integer>> func) {
    List<Integer> idxs = boards.getAllWith(pred);
    Comparator<Integer> comp = func.apply(boards);
    idxs.sort(comp);
    return idxs;
  }
}
