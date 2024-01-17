/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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

import static io.wcm.handler.media.UriTemplateType.CROP_CENTER;
import static io.wcm.handler.media.UriTemplateType.SCALE_HEIGHT;
import static io.wcm.handler.media.UriTemplateType.SCALE_WIDTH;
import static io.wcm.handler.media.testcontext.UriTemplateAssert.assertUriTemplate;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.testing.mock.osgi.MapUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.MockAssetDelivery;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test DAM URI template for assets.
 */
@ExtendWith(AemContextExtension.class)
class DamUriTemplateTest {

  final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;

  @BeforeEach
  void setUp() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testGetUriTemplate() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();

    assertUriTemplate(media, CROP_CENTER, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.jpg");
    assertUriTemplate(media, SCALE_WIDTH, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(media, SCALE_HEIGHT, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void testGetUriTemplate_CropCenter_EnforceOutputFileExtension() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath())
        .enforceOutputFileExtension(FileExtension.PNG)
        .build();

    assertUriTemplate(media, CROP_CENTER, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.png");
    assertUriTemplate(media, SCALE_WIDTH, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.png");
    assertUriTemplate(media, SCALE_HEIGHT, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.png");
  }

  @Test
  void testGetUriTemplate_DynamicMedia() {
    Asset asset = createSampleAssetWithDynamicMedia("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();

    assertUriTemplate(media, CROP_CENTER, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}&hei={height}&fit=crop");
    assertUriTemplate(media, SCALE_WIDTH, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}");
    assertUriTemplate(media, SCALE_HEIGHT, 100, 50,
        "https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}");
  }

  @Test
  void testGetUriTemplate_WebOptimizedImageDelivery() {
    context.registerInjectActivateService(MockAssetDelivery.class);

    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    String assetId = MockAssetDelivery.getAssetId(asset);
    Media media = mediaHandler.get(asset.getPath()).build();

    assertUriTemplate(media, CROP_CENTER, 100, 50,
        "/asset/delivery/" + assetId + "/sample.jpg?preferwebp=true&quality=98&width={width}");
    assertUriTemplate(media, SCALE_WIDTH, 100, 50,
        "/asset/delivery/" + assetId + "/sample.jpg?preferwebp=true&quality=98&width={width}");
    assertUriTemplate(media, SCALE_HEIGHT, 100, 50,
        "/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg");
  }

  Asset createSampleAsset(String classpathResource, String contentType) {
    return createSampleAsset(classpathResource, contentType, false);
  }

  Asset createSampleAssetWithDynamicMedia(String classpathResource, String contentType) {
    return createSampleAsset(classpathResource, contentType, true);
  }

  Asset createSampleAsset(String classpathResource, String contentType, boolean dmMetadata) {
    String fileName = FilenameUtils.getName(classpathResource);
    Map<String, Object> metadata;
    if (dmMetadata) {
      metadata = MapUtil.toMap(Scene7Constants.PN_S7_FILE, "DummyFolder/" + fileName);
    }
    else {
      metadata = Collections.emptyMap();
    }
    return context.create().asset("/content/dam/" + fileName, classpathResource, contentType, metadata);
  }

}
