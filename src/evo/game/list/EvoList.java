package evo.game.list;

import com.google.gson.*;
import evo.game.EvoElement;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by jackfriedson on 4/2/16.
 */
abstract class EvoList<T extends EvoElement> extends EvoElement implements Iterable<T> {
  protected final List<T> contents;
  protected final boolean allowDuplicates;

  protected EvoList(boolean allowDups) {
    this.contents = new ArrayList<>();
    this.allowDuplicates = allowDups;
  }

  protected EvoList(List<T> contents, boolean allowDups) {
    this.contents = Objects.requireNonNull(contents);
    this.allowDuplicates = allowDups;
  }

  public List<T> getContents() { return this.contents; }

  public void update(EvoList<T> newList) {
    Objects.requireNonNull(newList);
    this.contents.clear();
    this.contents.addAll(newList.getContents());
  }

  public void sort(Comparator<T> comp) { this.contents.sort(comp); }

  public T get(int i) { return this.contents.get(i); }

  public boolean isEmpty() { return this.contents.isEmpty(); }

  public int indexOf(T toFind) { return this.contents.indexOf(toFind); }

  public void add(T toAdd) { this.contents.add(toAdd); }

  public void add(int idx, T toAdd) { this.contents.add(idx, toAdd); }

  public void addAll(Collection<? extends T> toAdd) { this.contents.addAll(toAdd); }

  public void remove(T toRem) { this.contents.remove(toRem); }

  public T remove(int idx) { return this.contents.remove(idx); }

  public int size() { return this.contents.size(); }

  public abstract EvoList<T> makeCopy();

  public abstract EvoList<T> filterBy(Predicate<T> pred);

  @Override
  public Iterator<T> iterator() {
    return contents.iterator();
  }

  @Override
  public boolean isValid() {
    Set<T> set = new HashSet<>();

    for (T t : this) {
      if (!t.isValid()) return false;
      if (!allowDuplicates && !set.add(t)) return false;
    }

    return true;
  }

  public static JsonSerializer serializer() { return new EvoListSerializer(); }
  private static class EvoListSerializer<U extends EvoElement>
          implements JsonSerializer<EvoList<U>> {
    @Override
    public JsonElement serialize(EvoList<U> evoList, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
      JsonArray result = new JsonArray();
      for (U u : evoList) result.add(u.toJson());
      return result;
    }
  }
}
