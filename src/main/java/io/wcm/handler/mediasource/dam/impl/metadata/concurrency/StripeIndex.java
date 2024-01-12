/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.handler.mediasource.dam.impl.metadata.concurrency;

/**
 * Maps keys to a striped index set. Each key is mapped to a index within the max stripe count.
 * <p>
 * The logic is extracted from <a href=
 * "https://github.com/google/guava/blob/master/guava/src/com/google/common/util/concurrent/Striped.java">Striped</a>,
 * initially written by Dimitris Andreou from the Guava team (Apache 2.0 license).
 * </p>
 */
final class StripeIndex {

  // Capacity (power of two) minus one, for fast mod evaluation
  private final int mask;
  private final int size;

  // A bit mask were all bits are set.
  private static final int ALL_SET = ~0;

  // The largest power of two that can be represented as an {@code int}.
  private static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

  /**
   * @param stripes the minimum number of stripes required
   */
  StripeIndex(int stripes) {
    if (stripes <= 0) {
      throw new IllegalArgumentException("Invalid number of stripes: " + stripes);
    }
    this.mask = stripes > MAX_POWER_OF_TWO ? ALL_SET : ceilToPowerOfTwo(stripes) - 1;
    this.size = (mask == ALL_SET) ? Integer.MAX_VALUE : mask + 1;
  }

  /** Returns the total number of stripes in this instance. */
  int size() {
    return size;
  }

  /**
   * Returns the index to which the given key is mapped, so that getAt(indexFor(key)) == get(key).
   */
  int indexFor(Object key) {
    int hash = smear(key.hashCode());
    return hash & mask;
  }

  private static int smear(int hashCode) {
    int newHashCode = hashCode;
    newHashCode ^= (newHashCode >>> 20) ^ (newHashCode >>> 12);
    return newHashCode ^ (newHashCode >>> 7) ^ (newHashCode >>> 4);
  }

  private static int ceilToPowerOfTwo(int x) {
    return 1 << log2RoundCeiling(x);
  }

  private static int log2RoundCeiling(int x) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(x - 1);
  }

}
