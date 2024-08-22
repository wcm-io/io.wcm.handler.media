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

import static io.wcm.handler.mediasource.dam.impl.weboptimized.ParameterMap.createRelativeCroppingString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.dam.ngdm.MockAssetDelivery;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class WebOptimizedImageDeliveryServiceImplTest {

  private final AemContext context = AppAemContext.newAemContext();

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
    Asset asset = context.create().asset("/content/dam/Test_1.jpg", 10, 10, ContentType.JPEG);
    String assetId = MockAssetDelivery.getAssetId(asset);

    assertEquals("/adobe/dynamicmedia/deliver/" + assetId + "/test-1.jpg?preferwebp=true",
        underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()));

    String cropping = URLEncoder.encode(createRelativeCroppingString(0, 0, 0.2, 0.4), StandardCharsets.UTF_8);
    assertEquals("/adobe/dynamicmedia/deliver/" + assetId + "/test-1.jpg?c=" + cropping + "&preferwebp=true&r=90&width=10",
        underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()
            .width(10L).cropDimension(new CropDimension(0, 0, 2, 4)).rotation(90)));
  }

  @Test
  void testGetDeliveryUrl_relativeCropping() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class);
    Asset asset = context.create().asset("/content/dam/Test_1.jpg", 1920, 604, ContentType.JPEG);
    String assetId = MockAssetDelivery.getAssetId(asset);

    assertEquals(WebOptimizedImageDeliveryCropOption.RELATIVE_PARAMETERS, underTest.getCropOption());

    String cropping = URLEncoder.encode(createRelativeCroppingString(0.54, 0, 0.42, 1), StandardCharsets.UTF_8);
    assertEquals("/adobe/dynamicmedia/deliver/" + assetId + "/test-1.jpg?c=" + cropping + "&preferwebp=true&width=806",
            underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()
                    .width(806L)
                    .cropDimension(new CropDimension(1028, 0, 806, 604))));
  }

  @Test
  void testGetDeliveryUrl_absoluteCropping() {
    context.registerInjectActivateService(MockAssetDelivery.class);
    WebOptimizedImageDeliveryService underTest = context.registerInjectActivateService(WebOptimizedImageDeliveryServiceImpl.class,
        "cropOption", WebOptimizedImageDeliveryCropOption.ABSOLUTE_PARAMETERS.name());
    Asset asset = context.create().asset("/content/dam/Test_1.jpg", 1920, 604, ContentType.JPEG);
    String assetId = MockAssetDelivery.getAssetId(asset);

    assertEquals(WebOptimizedImageDeliveryCropOption.ABSOLUTE_PARAMETERS, underTest.getCropOption());

    String cropping = URLEncoder.encode("1028,0,806,604", StandardCharsets.UTF_8);
    assertEquals("/adobe/dynamicmedia/deliver/" + assetId + "/test-1.jpg?c=" + cropping + "&preferwebp=true&width=806",
        underTest.getDeliveryUrl(asset, new WebOptimizedImageDeliveryParams()
            .width(806L)
            .cropDimension(new CropDimension(1028, 0, 806, 604))));
  }

}
