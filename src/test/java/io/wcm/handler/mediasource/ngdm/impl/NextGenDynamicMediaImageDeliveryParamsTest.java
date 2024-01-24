/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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
package io.wcm.handler.mediasource.ngdm.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import io.wcm.handler.media.Dimension;

class NextGenDynamicMediaImageDeliveryParamsTest {

  @Test
  void testEmpty() {
    NextGenDynamicMediaImageDeliveryParams underTest = new NextGenDynamicMediaImageDeliveryParams();

    assertNull(underTest.getWidth());
    assertNull(underTest.getWidthPlaceholder());
    assertNull(underTest.getCropSmartRatio());
    assertNull(underTest.getRotation());
    assertNull(underTest.getQuality());
  }

  @Test
  void testWithProps() {
    NextGenDynamicMediaImageDeliveryParams underTest = new NextGenDynamicMediaImageDeliveryParams()
        .width(100L)
        .widthPlaceholder("{width}")
        .cropSmartRatio(new Dimension(16, 90))
        .rotation(90)
        .quality(70);

    assertEquals(100L, underTest.getWidth());
    assertEquals("{width}",underTest.getWidthPlaceholder());
    assertEquals(new Dimension(16, 90), underTest.getCropSmartRatio());
    assertEquals(90, underTest.getRotation());
    assertEquals(70, underTest.getQuality());
  }

}
