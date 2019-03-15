/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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
package io.wcm.handler.mediasource.dam.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.wcm.handler.media.CropDimension;

public class AutoCroppingTest {

  @Test
  public void testCalculateAutoCropDimension_AdaptWidth() {
    CropDimension result = AutoCropping.calculateAutoCropDimension(180, 90, 16d / 9d);
    assertEquals(10, result.getLeft());
    assertEquals(0, result.getTop());
    assertEquals(160, result.getWidth());
    assertEquals(90, result.getHeight());
  }

  @Test
  public void testCalculateAutoCropDimension_AdaptHeight() {
    CropDimension result = AutoCropping.calculateAutoCropDimension(160, 100, 16d / 9d);
    assertEquals(0, result.getLeft());
    assertEquals(5, result.getTop());
    assertEquals(160, result.getWidth());
    assertEquals(90, result.getHeight());
  }

}