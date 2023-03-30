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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

class StripeIndexTest {

  @Test
  void testSize() {
    assertTrue(new StripeIndex(100).size() >= 100);
    assertEquals(256, new StripeIndex(256).size());
  }

  @Test
  void testInvalidSize() {
    assertThrows(IllegalArgumentException.class, () -> new StripeIndex(0));
    assertThrows(IllegalArgumentException.class, () -> new StripeIndex(-1));
  }

  @Test
  void testIndexFor() {
    final int STRIPES = 256;
    assertStripeIndexes(STRIPES, Range.between(0, STRIPES - 1));
  }

  @Test
  void testIndexFor_MaxSize() {
    final int STRIPES = Integer.MAX_VALUE;
    assertStripeIndexes(STRIPES, Range.between(0, STRIPES));
  }

  private static void assertStripeIndexes(int stripes, Range<Integer> validIndexes) {
    StripeIndex underTest = new StripeIndex(stripes);
    for (int i = 0; i < 10000; i++) {
      Object key = new Object();
      int index = underTest.indexFor(key);
      assertTrue(validIndexes.contains(index), "Index " + index + " for key " + key + " within range " + validIndexes);
    }
  }

}
