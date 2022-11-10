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

import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_CROP;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_REF;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_ROTATION;
import static io.wcm.handler.media.UriTemplateType.CROP_CENTER;
import static io.wcm.handler.media.UriTemplateType.SCALE_HEIGHT;
import static io.wcm.handler.media.UriTemplateType.SCALE_WIDTH;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_16_10;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_4_3;
import static io.wcm.handler.media.testcontext.DummyMediaFormats.RATIO_SQUARE;
import static io.wcm.handler.media.testcontext.UriTemplateAssert.assertUriTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.osgi.MapUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;
import com.google.common.collect.ImmutableList;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.dam.impl.metadata.AssetSynchonizationService;
import io.wcm.handler.mediasource.dam.impl.metadata.RenditionMetadataListenerService;
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
    // register RenditionMetadataListenerService to generate rendition metadata
    context.registerInjectActivateService(new AssetSynchonizationService());
    context.registerInjectActivateService(new RenditionMetadataListenerService(),
        "threadPoolSize", 0,
        "allowedRunMode", new String[0]);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testOriginal_CropCenter() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(RATIO_16_10)
        .build();
    Rendition rendition = media.getRendition();

    // CROP_CENTER not supported for renditions
    assertThrows(IllegalArgumentException.class, () -> {
      rendition.getUriTemplate(CROP_CENTER);
    });
  }

  @Test
  void testOriginal() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(RATIO_16_10)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void testOriginal_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(RATIO_16_10)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}");
  }

  @Test
  void test4_3() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(RATIO_4_3)
        .autoCrop(true)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 120, 90,
        "/content/dam/sample.jpg/_jcr_content/renditions/rendition43.jpg.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 120, 90,
        "/content/dam/sample.jpg/_jcr_content/renditions/rendition43.jpg.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void test4_3_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Media media = mediaHandler.get(asset.getPath())
        .mediaFormat(RATIO_4_3)
        .autoCrop(true)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 160, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=16,0,160,120&wid={width}");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 160, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=16,0,160,120&hei={height}");
  }

  @Test
  void testMultiple() {
    Asset asset = createSampleAsset();
    Media media = mediaHandler.get(asset.getPath())
        .pictureSource(new PictureSource(RATIO_16_10).widths(120, 96))
        .pictureSource(new PictureSource(RATIO_SQUARE).widths(100))
        .pictureSource(new PictureSource(RATIO_4_3).widths(144))
        .autoCrop(true)
        .build();

    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());
    assertEquals(4, renditions.size());

    assertUriTemplate(renditions.get(0), SCALE_WIDTH, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(renditions.get(1), SCALE_WIDTH, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(renditions.get(2), SCALE_WIDTH, 120, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.36,0,156,120.file/sample.jpg");
    assertUriTemplate(renditions.get(3), SCALE_WIDTH, 160, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.16,0,176,120.file/sample.jpg");

    assertUriTemplate(renditions.get(0), SCALE_HEIGHT, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
    assertUriTemplate(renditions.get(1), SCALE_HEIGHT, 192, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
    assertUriTemplate(renditions.get(2), SCALE_HEIGHT, 120, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.36,0,156,120.file/sample.jpg");
    assertUriTemplate(renditions.get(3), SCALE_HEIGHT, 160, 120,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.16,0,176,120.file/sample.jpg");
  }

  @Test
  void testMultiple_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Media media = mediaHandler.get(asset.getPath())
        .pictureSource(new PictureSource(RATIO_16_10).widths(120, 96))
        .pictureSource(new PictureSource(RATIO_SQUARE).widths(100))
        .pictureSource(new PictureSource(RATIO_4_3).widths(144))
        .autoCrop(true)
        .build();

    List<Rendition> renditions = ImmutableList.copyOf(media.getRenditions());
    assertEquals(4, renditions.size());

    assertUriTemplate(renditions.get(0), SCALE_WIDTH, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}");
    assertUriTemplate(renditions.get(1), SCALE_WIDTH, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}");
    assertUriTemplate(renditions.get(2), SCALE_WIDTH, 120, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=36,0,120,120&wid={width}");
    assertUriTemplate(renditions.get(3), SCALE_WIDTH, 160, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=16,0,160,120&wid={width}");

    assertUriTemplate(renditions.get(0), SCALE_HEIGHT, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}");
    assertUriTemplate(renditions.get(1), SCALE_HEIGHT, 192, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}");
    assertUriTemplate(renditions.get(2), SCALE_HEIGHT, 120, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=36,0,120,120&hei={height}");
    assertUriTemplate(renditions.get(3), SCALE_HEIGHT, 160, 120,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=16,0,160,120&hei={height}");
  }

  @Test
  @SuppressWarnings("null")
  void testManualCroppingRotation() {
    Asset asset = createSampleAsset();
    Resource content = context.create().resource(context.currentPage(), "asset",
        PN_MEDIA_REF, asset.getPath(),
        PN_MEDIA_CROP, "5,5,80,55",
        PN_MEDIA_ROTATION, 90);
    Media media = mediaHandler.get(content)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 50, 75,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.5,5,80,55.90.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 50, 75,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.5,5,80,55.90.file/sample.jpg");
  }

  @Test
  @SuppressWarnings("null")
  void testManualCroppingRotation_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia();
    Resource content = context.create().resource(context.currentPage(), "asset",
        PN_MEDIA_REF, asset.getPath(),
        PN_MEDIA_CROP, "5,5,80,55",
        PN_MEDIA_ROTATION, 90);
    Media media = mediaHandler.get(content)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 50, 75,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=5,5,75,50&rotate=90&wid={width}");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 50, 75,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?crop=5,5,75,50&rotate=90&hei={height}");
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
    Asset asset = context.create().asset("/content/dam/sample.jpg", 192, 120, ContentType.JPEG, metadata);
    context.create().assetRenditionWebEnabled(asset, 192, 120);
    context.create().assetRendition(asset, "rendition43.jpg", 120, 90, ContentType.JPEG);
    return asset;
  }

}
