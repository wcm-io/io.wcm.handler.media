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

import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_1COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.EDITORIAL_2COL;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class AutoCroppingMediaHandlerTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Asset asset;
  private Resource resource;

  @BeforeEach
  void setUp() {
    // register RenditionMetadataListenerService to generate rendition metadata
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0,
        "allowedRunMode", new String[0]);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    // prepare asset with web rendition
    asset = context.create().asset("/content/dam/test.jpg", 400, 200, ContentType.JPEG);
    context.create().assetRenditionWebEnabled(asset, 300, 150);

    // prepare component with auto-cropping
    context.create().resource("/apps/app1/components/comp1",
        MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP, true);

    // prepare resource with asset reference
    resource = context.create().resource("/content/test",
        "sling:resourceType", "app1/components/comp1",
        "mediaRef", asset.getPath());
  }

  @Test
  void testMediaFormatWithRatio() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(RATIO)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(320, rendition.getWidth());
    assertEquals(200, rendition.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.320.200.40,0,360,200.file/test.jpg", media.getUrl());
  }

  @Test
  void testMediaFormatFixedDimension() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(EDITORIAL_1COL)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(215, rendition.getWidth());
    assertEquals(102, rendition.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.215.102.0,5,400,195.file/test.jpg", media.getUrl());
  }

  @Test
  void testMultipleMediaFormatsFixedDimension() {
    Media media = mediaHandler.get(resource)
        .mediaFormats(EDITORIAL_2COL, EDITORIAL_1COL)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(215, rendition.getWidth());
    assertEquals(102, rendition.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.215.102.0,6,400,195.file/test.jpg", media.getUrl());
  }

  @Test
  void testMediaFormatFixedDimension_NoMatch() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(EDITORIAL_2COL)
        .build();
    assertFalse(media.isValid());
  }

  @Test
  void testManualCroppingParametersHaveHigherPrecedence() {

    // prepare resource with asset reference and manual cropping parameters
    // this manual cropping results in a 16:10 image and should have higher precedence than auto-cropping
    Resource resource2 = context.create().resource("/content/test2",
        "sling:resourceType", "app1/components/comp1",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath(),
        MediaNameConstants.PN_MEDIA_CROP, new CropDimension(0, 0, 120, 75).getCropString());

    Media media = mediaHandler.get(resource2)
        .mediaFormat(RATIO)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(160, rendition.getWidth());
    assertEquals(100, rendition.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.160.100.0,0,160,100.file/test.jpg", media.getUrl());
  }

  @Test
  void testInvalidManualCroppingParametersFallbackToAutoCropping() {

    // prepare resource with asset reference and manual cropping parameters
    // this manual cropping results in a 1:1 image not matching the media format
    Resource resource2 = context.create().resource("/content/test2",
        "sling:resourceType", "app1/components/comp1",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath(),
        MediaNameConstants.PN_MEDIA_CROP, new CropDimension(20, 20, 50, 50).getCropString());

    Media media = mediaHandler.get(resource2)
        .mediaFormat(RATIO)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(320, rendition.getWidth());
    assertEquals(200, rendition.getHeight());
    assertFalse(rendition.isFallback());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.320.200.40,0,360,200.file/test.jpg", media.getUrl());
  }

  @Test
  void testMediaFormatWithRatio_WebRenditionsExcludedFromMediaHandling() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(RATIO)
        .includeAssetWebRenditions(false)
        .build();
    assertTrue(media.isValid());
    Rendition rendition = media.getRendition();
    assertEquals(320, rendition.getWidth());
    assertEquals(200, rendition.getHeight());
    assertEquals("/content/dam/test.jpg/_jcr_content/renditions/original.image_file.320.200.40,0,360,200.file/test.jpg", media.getUrl());
  }

}
