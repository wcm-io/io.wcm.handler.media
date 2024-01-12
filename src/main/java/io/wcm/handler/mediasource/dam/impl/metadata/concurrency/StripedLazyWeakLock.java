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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/**
 * A striped {@code Lock}. This offers the underlying lock striping similar
 * to that of {@code ConcurrentHashMap} in a reusable form.
 * Conceptually, lock striping is the technique of dividing a lock into many
 * <i>stripes</i>, increasing the granularity of a single lock and allowing independent operations
 * to lock different stripes and proceed concurrently, instead of creating contention for a single
 * lock.
 * <p>
 * This is inspired by Guava's <a href=
 * "https://github.com/google/guava/blob/master/guava/src/com/google/common/util/concurrent/Striped.java">Striped</a>,
 * but uses Caffeine internally.
 * </p>
 */
public final class StripedLazyWeakLock {

  private final StripeIndex stripeIndex;
  private final LoadingCache<Integer, Lock> locks;

  /**
   * Creates a {@code Striped<Lock>} with lazily initialized, weakly referenced locks. Every lock is
   * reentrant.
   * @param stripes the minimum number of stripes (locks) required
   */
  public StripedLazyWeakLock(int stripes) {
    this.stripeIndex = new StripeIndex(stripes);
    this.locks = Caffeine.newBuilder().weakValues().build(key -> new ReentrantLock());
  }

  /**
   * Returns the stripe that corresponds to the passed key. It is always guaranteed that if {@code
   * key1.equals(key2)}, then {@code get(key1) == get(key2)}.
   * @param key an arbitrary, non-null key
   * @return the stripe that the passed key corresponds to
   */
  public Lock get(Object key) {
    return locks.get(stripeIndex.indexFor(key));
  }

}
