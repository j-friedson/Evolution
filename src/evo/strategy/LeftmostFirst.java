package evo.strategy;

import java.util.Comparator;

/**
 * Created by jackfriedson on 4/24/16.
 */
public class LeftmostFirst implements Comparator<Integer> {

  @Override
  public int compare(Integer o1, Integer o2) {
    return o1 - o2;
  }
}
