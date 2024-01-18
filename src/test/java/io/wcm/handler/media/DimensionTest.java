/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.handler.media;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class DimensionTest {

  @Test
  void testSimple() {
    Dimension dimension = new Dimension(20, 10);

    assertEquals(20, dimension.getWidth());
    assertEquals(10, dimension.getHeight());
    assertEquals("[width=20,height=10]", dimension.toString());
  }

  @Test
  void testEquals() {
    Dimension dimension1 = new Dimension(20, 10);
    Dimension dimension2 = new Dimension(20, 10);
    Dimension dimension3 = new Dimension(21, 10);
    Dimension dimension4 = new Dimension(20, 11);

    assertEquals(dimension1, dimension2);
    assertNotEquals(dimension1, dimension3);
    assertNotEquals(dimension1, dimension4);

    assertEquals(dimension2, dimension1);
    assertNotEquals(dimension2, dimension3);
    assertNotEquals(dimension2, dimension4);

    assertNotEquals(dimension3, dimension1);
    assertNotEquals(dimension3, dimension2);
    assertNotEquals(dimension3, dimension4);

    assertNotEquals(dimension4, dimension1);
    assertNotEquals(dimension4, dimension2);
    assertNotEquals(dimension4, dimension3);
  }

}
