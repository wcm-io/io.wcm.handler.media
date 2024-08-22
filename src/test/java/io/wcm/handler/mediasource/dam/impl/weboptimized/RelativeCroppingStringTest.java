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
package io.wcm.handler.mediasource.dam.impl.weboptimized;

import static io.wcm.handler.mediasource.dam.impl.weboptimized.RelativeCroppingString.create;
import static io.wcm.handler.mediasource.dam.impl.weboptimized.RelativeCroppingString.createFromCropDimension;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Dimension;


class RelativeCroppingStringTest {

  @Test
  void testCreate() {
    assertEquals("0.0p,0.0p,100.0p,100.0p", create(0.0, 0.0, 1.0, 1.0));
    assertEquals("15.0p,20.0p,55.0p,60.0p", create(0.15, 0.20, 0.55, 0.60));
    assertEquals("15.1p,21.3p,49.5p,59.2p", create(0.1512, 0.2131, 0.4954, 0.5915));
  }

  @Test
  void testCreateFromCropDimension() {
    assertEquals("0.0p,0.0p,100.0p,100.0p", createFromCropDimension(new CropDimension(0, 0, 200, 100), new Dimension(200, 100)));
    assertEquals("6.5p,55.0p,52.5p,67.0p", createFromCropDimension(new CropDimension(13, 55, 105, 67), new Dimension(200, 100)));
  }

}
