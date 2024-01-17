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
package io.wcm.handler.media.impl;

import static io.wcm.handler.media.impl.ImageFileServletSelector.build;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import io.wcm.handler.media.CropDimension;

class ImageFileServletSelectorTest {

  @Test
  void testParse_Empty() {
    ImageFileServletSelector underTest = new ImageFileServletSelector(new String[0]);
    assertEquals(0, underTest.getWidth());
    assertEquals(0, underTest.getHeight());
    assertEquals(null, underTest.getCropDimension());
    assertEquals(0, underTest.getRotation());
    assertEquals(0, underTest.getQuality());
  }

  @Test
  void testParse_AllParams() {
    ImageFileServletSelector underTest = new ImageFileServletSelector(new String[] {
        "image_file",
        "10",
        "20",
        "2,4,8,12",
        "90",
        "60"
    });
    assertEquals(10, underTest.getWidth());
    assertEquals(20, underTest.getHeight());
    CropDimension cropDimension = underTest.getCropDimension();
    assertNotNull(cropDimension);
    assertEquals("2,4,8,12", cropDimension.getCropString());
    assertEquals(90, underTest.getRotation());
    assertEquals(60, underTest.getQuality());
  }

  @Test
  void testParse_InvalidParams() {
    ImageFileServletSelector underTest = new ImageFileServletSelector(new String[] {
        "image_file",
        "-5",
        "0",
        "1,2,3",
        "33",
        "150"
    });
    assertEquals(0, underTest.getWidth());
    assertEquals(0, underTest.getHeight());
    assertEquals(null, underTest.getCropDimension());
    assertEquals(0, underTest.getRotation());
    assertEquals(0, underTest.getQuality());
  }

  @Test
  void testBuild() {
    CropDimension crop = new CropDimension(2, 4, 6, 8);
    assertEquals("image_file.10.20", build(10, 20, null, null, null, false));
    assertEquals("image_file.10.20.download_attachment", build(10, 20, null, null, null, true));
    assertEquals("image_file.10.20.-.0.50.download_attachment", build(10, 20, null, null, 0.5d, true));
    assertEquals("image_file.10.20.2,4,8,12", build(10, 20, crop, null, null, false));
    assertEquals("image_file.10.20.2,4,8,12.download_attachment", build(10, 20, crop, null, null, true));
    assertEquals("image_file.10.20.2,4,8,12.90", build(10, 20, crop, 90, null, false));
    assertEquals("image_file.10.20.2,4,8,12.90.download_attachment", build(10, 20, crop, 90, null, true));
    assertEquals("image_file.10.20.2,4,8,12.90.60", build(10, 20, crop, 90, 0.6d, false));
    assertEquals("image_file.10.20.-.90", build(10, 20, null, 90, null, false));
    assertEquals("image_file.10.20.-.90.download_attachment", build(10, 20, null, 90, null, true));
    assertEquals("image_file.10.20.-.0", build(10, 20, null, null, -0.2d, false));
  }

}
