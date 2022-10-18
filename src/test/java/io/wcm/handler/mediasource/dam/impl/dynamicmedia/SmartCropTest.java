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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;
import static io.wcm.handler.media.testcontext.AppAemContext.DAM_PATH;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.CROP_TYPE_SMART;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_BANNER;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_CROP_TYPE;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_WIDTH;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.canApply;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.getDimension;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.isMatchingSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class SmartCropTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;
  private NamedDimension dimension16_10;

  @BeforeEach
  void setUp() {
    asset = context.create().asset(DAM_PATH + "/image1.jpg", 160, 100, ContentType.JPEG);
    dimension16_10 = new NamedDimension("16-10", 16, 10);
  }

  @Test
  void testCanApply() {
    assertTrue(canApply(null, null));
    assertTrue(canApply(new CropDimension(0, 0, 10, 10, true), null));

    assertFalse(canApply(null, 90));
    assertFalse(canApply(new CropDimension(0, 0, 10, 10, true), 90));
    assertFalse(canApply(new CropDimension(0, 0, 10, 10, false), null));
  }

  @Test
  void testGetDimension() {
    ImageProfile profile1 = new ImageProfileImpl(
        context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "16-10,160,100|4-3,40,30"));

    assertNull(getDimension(profile1, 500, 500));
    assertNamedDimension(getDimension(profile1, 320, 200), "16-10", 320, 200);
    assertNamedDimension(getDimension(profile1, 400, 300), "4-3", 400, 300);
  }

  @Test
  void testGetDimension_NoProfile() {
    assertNull(getDimension(null, 100, 100));
  }

  @Test
  void testIsMatchingSize_NoRenditionResource() {
    // assume everything is ok if no "16-10" rendition exists (we have no other chance)
    assertTrue(isMatchingSize(asset, context.resourceResolver(), dimension16_10, 80, 50));
  }

  @Test
  void testIsMatchingSize_MatchesExact() {
    setSmartCropRenditionNormalizedSize(0.5, 0.5); // results in 80x50 cropping area
    assertTrue(isMatchingSize(asset, context.resourceResolver(), dimension16_10, 80, 50));
  }

  @Test
  void testIsMatchingSize_MatchesSmaller() {
    setSmartCropRenditionNormalizedSize(0.5, 0.5); // results in 80x50 cropping area
    assertTrue(isMatchingSize(asset, context.resourceResolver(), dimension16_10, 40, 25));
  }

  @Test
  void testIsMatchingSize_TooSmall() {
    setSmartCropRenditionNormalizedSize(0.5, 0.5); // results in 80x50 cropping area
    assertFalse(isMatchingSize(asset, context.resourceResolver(), dimension16_10, 120, 75));
  }

  @Test
  void testIsMatchingSize_InvalidNormalizedWidthHeight() {
    setSmartCropRenditionNormalizedSize(0, 0);
    // assume true because no valid normalized width/height provided - we do not know the cropping area
    assertTrue(isMatchingSize(asset, context.resourceResolver(), dimension16_10, 80, 50));
  }

  private static void assertNamedDimension(NamedDimension namedDimension,
      String expectedName, long expectedWith, long expectedHeight) {
    assertNotNull(namedDimension);
    assertEquals(expectedName, namedDimension.getName());
    assertEquals(expectedWith, namedDimension.getWidth());
    assertEquals(expectedHeight, namedDimension.getHeight());
  }

  private void setSmartCropRenditionNormalizedSize(double normalizedWith, double normalizedHeight) {
    String smartCropRenditionPath = asset.getPath() + "/" + JCR_CONTENT + "/" + RENDITIONS_FOLDER
        + "/" + dimension16_10.getName() + "/" + JCR_CONTENT;
    context.create().resource(smartCropRenditionPath,
        PN_NORMALIZED_WIDTH, normalizedWith,
        PN_NORMALIZED_HEIGHT, normalizedHeight);
  }

}
