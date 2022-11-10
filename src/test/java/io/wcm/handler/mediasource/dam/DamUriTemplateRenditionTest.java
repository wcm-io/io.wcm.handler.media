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
package io.wcm.handler.mediasource.dam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;

import org.apache.sling.testing.mock.osgi.MapUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Test DAM URI template for renditions.
 */
@ExtendWith(AemContextExtension.class)
class DamUriTemplateRenditionTest {

  final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;

  @BeforeEach
  void setUp() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testOriginal_CropCenter() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO).build();
    Rendition rendition = media.getRendition();

    // CROP_CENTER not supported for renditions
    assertThrows(IllegalArgumentException.class, () -> {
      rendition.getUriTemplate(UriTemplateType.CROP_CENTER);
    });
  }

  @Test
  void testOriginal_ScaleWidth() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_WIDTH, 160, 100,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg");
  }

  @Test
  void testOriginal_ScaleHeight() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_HEIGHT, 160, 100,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void testOriginal_ScaleWidth_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_WIDTH, 160, 100,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}");
  }

  @Test
  void testOriginal_ScaleHeight_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_HEIGHT, 160, 100,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}");
  }

  @Test
  void testRendition43_ScaleWidth() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO2).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_WIDTH, 120, 90,
        "/content/dam/sample.jpg/_jcr_content/renditions/rendition43.jpg.image_file.{width}.0.file/sample.jpg");
  }

  @Test
  void testRendition43_ScaleHeight() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath(), DummyMediaFormats.RATIO2).build();

    assertUriTemplate(media.getRendition(), UriTemplateType.SCALE_HEIGHT, 120, 90,
        "/content/dam/sample.jpg/_jcr_content/renditions/rendition43.jpg.image_file.0.{height}.file/sample.jpg");
  }

  Asset createSampleAsset() {
    return createSampleAsset(false);
  }

  Asset createSampleAssetWithDynamicMedia() {
    return createSampleAsset(true);
  }

  Asset createSampleAsset(boolean dmMetadata) {
    Map<String, Object> metadata;
    if (dmMetadata) {
      metadata = MapUtil.toMap(Scene7Constants.PN_S7_FILE, "DummyFolder/sample.jpg");
    }
    else {
      metadata = Collections.emptyMap();
    }
    // create asset with original in 16:10 and rendition in 4:3 format
    Asset asset = context.create().asset("/content/dam/sample.jpg", 160, 100, ContentType.JPEG, metadata);
    context.create().assetRendition(asset, "rendition43.jpg", 120, 90, ContentType.JPEG);
    return asset;
  }

  static void assertUriTemplate(Rendition rendition, UriTemplateType type,
      long expectedMaxWith, long expectedMaxHeight, String expectedTemplate) {
    assertNotNull(rendition, "rendition valid");
    UriTemplate uriTemplate = rendition.getUriTemplate(type);
    assertEquals(type, uriTemplate.getType(), "uriTemplateType");
    assertEquals(expectedMaxWith, uriTemplate.getMaxWidth(), "maxWidth");
    assertEquals(expectedMaxHeight, uriTemplate.getMaxHeight(), "maxHeight");
    assertEquals(expectedTemplate, uriTemplate.getUriTemplate(), "uriTemplate");
  }

}
