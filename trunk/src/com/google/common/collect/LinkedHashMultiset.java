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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A Multiset implementation with predictable iteration order.  Elements
 * appear in the iterator in order by when the <i>first</i> occurrence of the
 * element was added.  If all occurrences of an element are removed, then one
 * or more elements added again, the element will not retain its earlier
 * iteration position, but will appear at the end as if it had never been
 * present.
 *
 * <p>To preserve iteration order across non-distinct elements, use {@link
 * LinkedListMultiset} instead.
 *
 * @author kevinb@google.com (Kevin Bourrillion)
 * @see LinkedListMultiset
 */
public final class LinkedHashMultiset<E> extends AbstractMapBasedMultiset<E>
    implements Cloneable {

  /**
   * Constructs an empty multiset with default capacity.
   */
  public LinkedHashMultiset() {
    super(new LinkedHashMap<E, AtomicInteger>());
  }

  /**
   * Constructs an empty multiset, with a hint for the capacity.
   *
   * @param distinctElements how many distinct elements the multiset should
   *     be sized to hold comfortably
   * @throws IllegalArgumentException if distinctElements is negative
   */
  public LinkedHashMultiset(int distinctElements) {
    super(new LinkedHashMap<E, AtomicInteger>(distinctElements * 4 / 3));
    if (distinctElements < 0) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Constructs an empty multiset as a copy of an existing multiset.
   */
  public LinkedHashMultiset(Multiset<? extends E> initialElements) {
    this(initialElements.elementSet().size());
    addAll(initialElements); // careful if we ever make this class nonfinal
  }

  /**
   * Constructs an empty multiset containing the given initial elements.
   */
  public LinkedHashMultiset(Collection<? extends E> initialElements) {
    this();
    addAll(initialElements); // careful if we ever make this class nonfinal
  }

  @SuppressWarnings("unchecked")
  @Override public LinkedHashMultiset<E> clone() {
    try {
      return (LinkedHashMultiset<E>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override protected Map<E, AtomicInteger> cloneBackingMap() {
    return (Map<E, AtomicInteger>)
        ((LinkedHashMap<E, AtomicInteger>) backingMap()).clone();
  }

  private static final long serialVersionUID = -1489616374694050806L;
}
