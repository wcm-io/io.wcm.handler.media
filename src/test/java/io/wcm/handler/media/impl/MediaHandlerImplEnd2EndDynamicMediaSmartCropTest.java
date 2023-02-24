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
package io.wcm.handler.media.impl;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.dam.api.DamConstants.RENDITIONS_FOLDER;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_16_10;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_4_3;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.CROP_TYPE_SMART;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_BANNER;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.ImageProfileImpl.PN_CROP_TYPE;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_HEIGHT;
import static io.wcm.handler.mediasource.dam.impl.dynamicmedia.SmartCrop.PN_NORMALIZED_WIDTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.dynamicmedia.DynamicMediaSupportServiceImpl;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Test media handling with Dynamic Media and Smart Cropping end-2-end.
 */
@ExtendWith(AemContextExtension.class)
class MediaHandlerImplEnd2EndDynamicMediaSmartCropTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Asset asset;
  private MediaHandler mediaHandler;

  @BeforeEach
  void setUp() {
    Resource profile1 = context.create().resource("/conf/global/settings/dam/adminui-extension/imageprofile/profile1",
        PN_CROP_TYPE, CROP_TYPE_SMART,
        PN_BANNER, "16-10,16,10|4-3,40,30");

    Resource assetFolder = context.create().resource("/content/dam/folder1");
    context.create().resource(assetFolder, JCR_CONTENT, DamConstants.IMAGE_PROFILE, profile1.getPath());

    asset = context.create().asset(assetFolder.getPath() + "/test.jpg", 160, 100, ContentType.JPEG,
        Scene7Constants.PN_S7_FILE, "DummyFolder/test");
    context.create().assetRenditionWebEnabled(asset, 128, 80); // simulate web rendition that is a bit smaller

    // original asset size is 160x100px
    // the 4-3 smart crop rendition defines a cropping area of 80x60px
    context.create().resource(asset.getPath() + "/" + JCR_CONTENT + "/" + RENDITIONS_FOLDER + "/4-3/" + JCR_CONTENT,
        PN_NORMALIZED_WIDTH, 0.5d,
        PN_NORMALIZED_HEIGHT, 0.6d);
    // the 16-10 smart crop rendition defines a cropping area of 120x75px
    context.create().resource(asset.getPath() + "/" + JCR_CONTENT + "/" + RENDITIONS_FOLDER + "/16-10/" + JCR_CONTENT,
        PN_NORMALIZED_WIDTH, 0.75d,
        PN_NORMALIZED_HEIGHT, 0.75d);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testValidSmartCroppedRenditionAndWidths() {
    Media media = getMediaWithWidths(RATIO_4_3, 80, 40);
    assertTrue(media.isValid());

    List<Rendition> renditions = List.copyOf(media.getRenditions());
    assertEquals(2, renditions.size());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=80&hei=60&fit=stretch", renditions.get(0).getUrl());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=40&hei=30&fit=stretch", renditions.get(1).getUrl());
  }

  @Test
  void testValidSmartCroppedRenditionAndWidths_DisableValidateSmartCropRenditionSizes() {
    context.registerInjectActivateService(DynamicMediaSupportServiceImpl.class,
        "validateSmartCropRenditionSizes", false,
        Constants.SERVICE_RANKING, 100);
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = getMediaWithWidths(RATIO_4_3, 100, 80, 40);
    assertTrue(media.isValid());

    List<Rendition> renditions = List.copyOf(media.getRenditions());
    assertEquals(3, renditions.size());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=100&hei=75&fit=stretch", renditions.get(0).getUrl());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=80&hei=60&fit=stretch", renditions.get(1).getUrl());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=40&hei=30&fit=stretch", renditions.get(2).getUrl());
  }

  @Test
  void testInvalidSmartCroppedRendition() {
    Media media = getMediaWithWidths(RATIO_4_3, 100);
    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.NO_MATCHING_RENDITION, media.getMediaInvalidReason());
  }

  @Test
  void testSomeInvalidSmartCroppedRendition() {
    Media media = getMediaWithWidths(RATIO_4_3, 100, 80, 40);
    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS, media.getMediaInvalidReason());
  }

  @Test
  void testValidSmartCroppedRendition_OnlyRatio() {
    Media media = getMediaWithRatio(RATIO_4_3);
    assertTrue(media.isValid());

    List<Rendition> renditions = List.copyOf(media.getRenditions());
    assertEquals(1, renditions.size());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A4-3?wid=80&hei=60&fit=stretch", renditions.get(0).getUrl());
  }

  @Test
  void testValidSmartCroppedRenditionOnlyRatio_MatchingOriginalRatio() {
    Media media = getMediaWithRatio(RATIO_16_10);
    assertTrue(media.isValid());

    List<Rendition> renditions = List.copyOf(media.getRenditions());
    assertEquals(1, renditions.size());
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/test%3A16-10?wid=120&hei=75&fit=stretch", renditions.get(0).getUrl());
  }

  private Media getMediaWithWidths(MediaFormat mediaFormat, long... widths) {
    return mediaHandler.get(asset.getPath())
        .pictureSource(new PictureSource(mediaFormat).widths(widths))
        .autoCrop(true)
        .build();
  }

  private Media getMediaWithRatio(MediaFormat mediaFormat) {
    return mediaHandler.get(asset.getPath())
        .mediaFormat(mediaFormat)
        .autoCrop(true)
        .build();
  }

}
