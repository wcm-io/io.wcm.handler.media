/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

import static com.day.cq.dam.api.DamConstants.PREFIX_ASSET_WEB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class WebEnabledRenditionCroppingTest {

  private final AemContext context = AppAemContext.newAemContext();

  @Test
  void testGetCropDimensionForOriginal() {
    Asset asset = context.create().asset("/content/dam/asset1.jpg", 160, 90, "image/jpeg");
    context.create().assetRendition(asset, PREFIX_ASSET_WEB + ".80.45.jpg", 80, 45, "image/jpeg");

    CropDimension result = WebEnabledRenditionCropping.getCropDimensionForOriginal(asset,
        new CropDimension(10, 15, 20, 30));

    assertEquals(20, result.getLeft());
    assertEquals(30, result.getTop());
    assertEquals(40, result.getWidth());
    assertEquals(60, result.getHeight());
  }

  @Test
  void testGetCropDimensionForOriginal_WebEnabledRenditionDoesNotExist() {
    Asset asset = context.create().asset("/content/dam/asset2.jpg", 160, 90, "image/jpeg");

    CropDimension result = WebEnabledRenditionCropping.getCropDimensionForOriginal(asset,
        new CropDimension(10, 15, 20, 30));

    assertEquals(10, result.getLeft());
    assertEquals(15, result.getTop());
    assertEquals(20, result.getWidth());
    assertEquals(30, result.getHeight());
  }

}
