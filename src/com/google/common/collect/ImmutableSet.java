/*
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A high-performance, immutable {@code Set} with reliable, user-specified
 * iteration order. Does not permit null elements.
 *
 * <p>Unlike {@link Collections#unmodifiableSet}, which is a <i>view</i> of a
 * separate collection that can still change, an instance of this class contains
 * its own private data and will <i>never</i> change. This class is convenient
 * for {@code public static final} sets ("constant sets") and also lets you
 * easily make a "defensive copy" of a set provided to your class by a caller.
 *
 * <p><b>Warning:</b> Like most sets, an {@code ImmutableSet} will not function
 * correctly if an element is modified after being placed in the set. For this
 * reason, and to avoid general confusion, it is strongly recommended to place
 * only immutable objects into this collection.
 *
 * <p>This class has been observed to perform significantly better than {@link
 * HashSet} for objects with very fast {@link Object#hashCode} implementations
 * (as a well-behaved immutable object should). While this class's factory
 * methods create hash-based instances, the {@link ImmutableSortedSet} subclass
 * performs binary searches instead.
 *
 * <p><b>Note</b>: Although this class is not final, it cannot be subclassed
 * outside its package as it has no public or protected constructors. Thus,
 * instances of this type are guaranteed to be immutable.
 *
 * @see ImmutableList
 * @see ImmutableMap
 * @author Kevin Bourrillion
 */
@SuppressWarnings("serial") // we're overriding default serialization
public abstract class ImmutableSet<E> extends ImmutableCollection<E>
    implements Set<E> {
  private static final ImmutableSet<?> EMPTY_IMMUTABLE_SET
      = new EmptyImmutableSet();

  /**
   * Returns the empty immutable set. This set behaves and performs comparably
   * to {@link Collections#emptySet}, and is preferable mainly for consistency
   * and maintainability of your code.
   */
  // Casting to any type is safe because the set will never hold any elements.
  @SuppressWarnings({"unchecked"})
  public static <E> ImmutableSet<E> of() {
    return (ImmutableSet<E>) EMPTY_IMMUTABLE_SET;
  }

  /**
   * Returns an immutable set containing a single element. This set behaves and
   * performs comparably to {@link Collections#singleton}, but will not accept
   * a null element. It is preferable mainly for consistency and
   * maintainability of your code.
   */
  public static <E> ImmutableSet<E> of(E element) {
    return new SingletonImmutableSet<E>(element, element.hashCode());
  }

  /**
   * Returns an immutable set containing the given elements, in order. Repeated
   * occurrences of an element (according to {@link Object#equals}) after the
   * first are ignored (but too many of these may result in the set being
   * sized inappropriately).
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableSet<E> of(E... elements) {
    switch (elements.length) {
      case 0:
        return of();
      case 1:
        return of(elements[0]);
      default:
        return create(Arrays.asList(elements), elements.length);
    }
  }

  /**
   * Returns an immutable set containing the given elements, in order. Repeated
   * occurrences of an element (according to {@link Object#equals}) after the
   * first are ignored (but too many of these may result in the set being
   * sized inappropriately).
   *
   * <p>Note that if {@code s} is a {@code Set<String>}, then {@code
   * ImmutableSet.copyOf(s)} returns an {@code ImmutableSet<String>} containing
   * each of the strings in {@code s}, while {@code ImmutableSet.of(s)} returns
   * a {@code ImmutableSet<Set<String>>} containing one element (the given set
   * itself).
   *
   * <p><b>Note:</b> Despite what the method name suggests, if {@code elements}
   * is an {@code ImmutableSet}, no copy will actually be performed, and the
   * given set itself will be returned.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableSet<E> copyOf(Iterable<? extends E> elements) {
    if (elements instanceof ImmutableSet) {
      @SuppressWarnings("unchecked") // all supported methods are covariant
      ImmutableSet<E> set = (ImmutableSet<E>) elements;
      return set;
    }
    return copyOfInternal(Collections2.toCollection(elements));
  }

  /**
   * Returns an immutable set containing the given elements, in order. Repeated
   * occurrences of an element (according to {@link Object#equals}) after the
   * first are ignored.
   *
   * @throws NullPointerException if any of {@code elements} is null
   */
  public static <E> ImmutableSet<E> copyOf(Iterator<? extends E> elements) {
    Collection<E> list = Lists.newArrayList(elements);
    return copyOfInternal(list);
  }

  private static <E> ImmutableSet<E> copyOfInternal(
      Collection<? extends E> collection) {
    // TODO: Support concurrent collections that change while this method is
    // running.
    switch (collection.size()) {
      case 0:
        return of();
      case 1:
        // TODO: Remove "ImmutableSet.<E>" when eclipse bug is fixed.
        return ImmutableSet.<E>of(collection.iterator().next());
      default:
        return create(collection, collection.size());
    }
  }

  ImmutableSet() {}

  /** Returns {@code true} if the {@code hashCode()} method runs quickly. */
  boolean isHashCodeFast() {
    return false;
  }
  
  @Override public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof ImmutableSet
        && isHashCodeFast()
        && ((ImmutableSet<?>) object).isHashCodeFast()
        && hashCode() != object.hashCode()) {
      return false;
    }
    if (object instanceof Set) {
      Set<?> that = (Set<?>) object;
      return size() == that.size() && containsAll(that);
    }
    return false;
  }
  
  @Override public int hashCode() {
    return Sets.hashCodeImpl(this);
  }
  
  @Override public String toString() {
    if (isEmpty()) {
      return "[]";
    }
    Iterator<E> iterator = iterator();
    StringBuilder result = new StringBuilder(size() * 16);
    result.append('[').append(iterator.next().toString());
    for (int i = 1; i < size(); i++) {
      result.append(", ").append(iterator.next().toString());
    }
    return result.append(']').toString();
  }

  private static final class EmptyImmutableSet extends ImmutableSet<Object> {
    public int size() {
      return 0;
    }

    @Override public boolean isEmpty() {
      return true;
    }

    @Override public boolean contains(Object target) {
      return false;
    }

    public Iterator<Object> iterator() {
      return Iterators.emptyIterator();
    }

    @Override public Object[] toArray() {
      return ObjectArrays.EMPTY_ARRAY;
    }

    @Override public <T> T[] toArray(T[] a) {
      if (a.length > 0) {
        a[0] = null;
      }
      return a;
    }

    @Override public boolean containsAll(Collection<?> targets) {
      return targets.isEmpty();
    }

    @Override public boolean equals(Object object) {
      return object == this
          || (object instanceof Set && ((Set<?>) object).isEmpty());
    }

    @Override public final int hashCode() {
      return 0;
    }
    
    @Override boolean isHashCodeFast() {
      return true;
    }
    
    @Override public String toString() {
      return "[]";
    }
  }

  private static final class SingletonImmutableSet<E> extends ImmutableSet<E> {
    final E element;
    final int hashCode;
    
    SingletonImmutableSet(E element, int hashCode) {
      this.element = element;
      this.hashCode = hashCode;
    }

    public int size() {
      return 1;
    }

    @Override public boolean isEmpty() {
      return false;
    }

    @Override public boolean contains(Object target) {
      return element.equals(target);
    }

    public Iterator<E> iterator() {
      return Iterators.singletonIterator(element);
    }

    @Override public Object[] toArray() {
      return new Object[] { element };
    }

    @SuppressWarnings({"unchecked"})
    @Override public <T> T[] toArray(T[] array) {
      if (array.length == 0) {
        array = ObjectArrays.newArray(array, 1);
      } else if (array.length > 1) {
        array[1] = null;
      }
      array[0] = (T) element;
      return array;
    }

    @Override public boolean equals(Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof Set) {
        Set<?> set = (Set<?>) object;
        return set.size() == 1 && contains(set.iterator().next());
      }
      return false;
    }

    @Override public final int hashCode() {
      return hashCode;
    }
    
    @Override boolean isHashCodeFast() {
      return true;
    }    

    @Override public String toString() {
      String elementToString = element.toString();
      return new StringBuilder(elementToString.length() + 2)
          .append('[')
          .append(elementToString)
          .append(']')
          .toString();
    }
  }

  private static <E> ImmutableSet<E> create(
      Iterable<? extends E> iterable, int count) {
    // count is always the (nonzero) number of elements in the iterable
    int tableSize = Hashing.chooseTableSize(count);
    Object[] table = new Object[tableSize];
    int mask = tableSize - 1;

    List<E> elements = new ArrayList<E>(count);
    int hashCode = 0;

    for (E element : iterable) {
      int hash = element.hashCode();
      for (int i = Hashing.smear(hash); true; i++) {
        int index = i & mask;
        Object value = table[index];
        if (value == null) {
          // Came to an empty bucket. Put the element here.
          table[index] = element;
          elements.add(element);
          hashCode += hash;
          break;
        } else if (value.equals(element)) {
          break; // Found a duplicate. Nothing to do.
        }
      }
    }

    // The iterable might have contained only duplicates of the same element.
    return (elements.size() == 1)
        ? new SingletonImmutableSet<E>(elements.get(0), hashCode)
        : new RegularImmutableSet<E>(elements.toArray(), hashCode, table, mask);
  }

  abstract static class ArrayImmutableSet<E> extends ImmutableSet<E> {
    final Object[] elements; // the elements (two or more) in the desired order

    ArrayImmutableSet(Object[] elements) {
      this.elements = elements;
    }

    public int size() {
      return elements.length;
    }

    @Override public boolean isEmpty() {
      return false;
    }

    /*
     * The cast is safe because the only way to create an instance is via the
     * create() method above, which only permits elements of type E.
     */
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
      return (Iterator<E>) Iterators.forArray(elements);
    }

    @Override public Object[] toArray() {
      Object[] array = new Object[size()];
      System.arraycopy(elements, 0, array, 0, size());
      return array;
    }

    @Override public <T> T[] toArray(T[] array) {
      int size = size();
      if (array.length < size) {
        array = ObjectArrays.newArray(array, size);
      } else if (array.length > size) {
        array[size] = null;
      }
      System.arraycopy(elements, 0, array, 0, size);
      return array;
    }

    @Override public boolean containsAll(Collection<?> targets) {
      if (targets == this) {
        return true;
      }
      if (!(targets instanceof ArrayImmutableSet)) {
        return super.containsAll(targets);
      }
      if (targets.size() > size()) {
        return false;
      }
      for (Object target : ((ArrayImmutableSet<?>) targets).elements) {
        if (!contains(target)) {
          return false;
        }
      }
      return true;
    }
  }

  private static final class RegularImmutableSet<E>
      extends ArrayImmutableSet<E> {
    final Object[] table; // the same elements in hashed positions (plus nulls)
    final int mask; // 'and' with an int to get a valid table index
    final int hashCode;
    
    RegularImmutableSet(Object[] elements, int hashCode,
        Object[] table, int mask) {
      super(elements);
      this.table = table;
      this.mask = mask;
      this.hashCode = hashCode;
    }

    @Override public boolean contains(Object target) {
      if (target == null) {
        return false;
      }
      for (int i = Hashing.smear(target.hashCode()); true; i++) {
        Object candidate = table[i & mask];
        if (candidate == null) {
          return false;
        }
        if (candidate.equals(target)) {
          return true;
        }
      }
    }
    
    @Override public int hashCode() {
      return hashCode;
    }
    
    @Override boolean isHashCodeFast() {
      return true;
    }    
  }

  /** such as ImmutableMap.keySet() */
  abstract static class TransformedImmutableSet<D, E> extends ImmutableSet<E> {
    final D[] source;
    final int hashCode;

    TransformedImmutableSet(D[] source, int hashCode) {
      this.source = source;
      this.hashCode = hashCode;
    }

    abstract E transform(D element);

    public int size() {
      return source.length;
    }

    @Override public boolean isEmpty() {
      return false;
    }

    public Iterator<E> iterator() {
      return new AbstractIterator<E>() {
        int index = 0;
        @Override protected E computeNext() {
          return index < source.length
              ? transform(source[index++])
              : endOfData();
        }
      };
    }

    @Override public Object[] toArray() {
      return toArray(new Object[size()]);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T[] toArray(T[] array) {
      int size = size();
      if (array.length < size) {
        array = ObjectArrays.newArray(array, size);
      } else if (array.length > size) {
        array[size] = null;
      }

      for (int i = 0; i < source.length; i++) {
        array[i] = (T) transform(source[i]);
      }
      return array;
    }

    @Override public final int hashCode() {
      return hashCode;
    }
    
    @Override boolean isHashCodeFast() {
      return true;
    }    
  }

  /*
   * This class is used to serialize all ImmutableSet instances, regardless of
   * implementation type. It captures their "logical contents" and they are
   * reconstructed using public static factories. This is necessary to ensure
   * that the existence of a particular implementation type is an implementation
   * detail.
   */
  private static class SerializedForm implements Serializable {
    final Object[] elements;
    SerializedForm(Object[] elements) {
      this.elements = elements;
    }
    Object readResolve() {
      return of(elements);
    }
    private static final long serialVersionUID = 0;
  }

  @Override Object writeReplace() {
    return new SerializedForm(toArray());
  }
}