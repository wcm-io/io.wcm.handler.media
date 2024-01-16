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
package io.wcm.handler.mediasource.dam.impl.ngdm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.testing.mock.aem.dam.MockAssetDelivery;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class WebOptimizedImageDeliveryServiceImplTest {

  private final AemContext context = new AemContext();

  @Test
  void testEnabled_AssetDeliveryNotPresent() {
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    assertFalse(underTest.isEnabled());
  }

  @Test
  void testEnabled_AssetDeliveryPresent() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    assertTrue(underTest.isEnabled());
  }

  @Test
  void testEnabled_AssetDeliveryPresent_Disabled() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class,
        "enabled", false);
    assertFalse(underTest.isEnabled());
  }

  @Test
  void testGetDeliveryUrl_AssetDeliveryNotPresent() {
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    Asset asset = context.create().asset("/content/dam/test.jpg", 10, 10, ContentType.JPEG);

    assertNull(underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()));
  }

  @Test
  void testGetDeliveryUrl_AssetDeliveryPresent() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    Asset asset = context.create().asset("/content/dam/test.jpg", 10, 10, ContentType.JPEG);
    String assetId = MockAssetDelivery.getAssetId(asset);

    assertEquals("/asset/delivery/" + assetId + "/test.jpg?preferwebp=true",
        underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()));

    assertEquals("/asset/delivery/" + assetId + "/test.jpg?c=0%2C0%2C2%2C4&preferwebp=true&r=90&width=10",
        underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()
            .width(10L).cropDimension(new CropDimension(0, 0, 2, 4)).rotation(90)));
  }

}
