/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test DAM URI template
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
  void testGetUriTemplate_CropCenter() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_CropCenter_EnforceOutputFileExtension() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath())
        .enforceOutputFileExtension(FileExtension.PNG)
        .build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.{height}.file/sample.png", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleWidth() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.{width}.0.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleHeight() {
    Asset asset = createSampleAsset("/filetype/sample.jpg", ContentType.JPEG);
    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_HEIGHT);
    assertEquals("/content/dam/sample.jpg/_jcr_content/renditions/original.image_file.0.{height}.file/sample.jpg", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_HEIGHT, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_CropCenter_DynamicMedia() {
    // activate dynamic media
    Asset asset = createSampleAssetWithDynamicMedia("/filetype/sample.jpg", ContentType.JPEG);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.CROP_CENTER);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}&hei={height}&fit=crop", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.CROP_CENTER, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleWidth_DynamicMedia() {
    // activate dynamic media
    Asset asset = createSampleAssetWithDynamicMedia("/filetype/sample.jpg", ContentType.JPEG);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?wid={width}", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
  }

  @Test
  void testGetUriTemplate_ScaleHeight_DynamicMedia() {
    // activate dynamic media
    Asset asset = createSampleAssetWithDynamicMedia("/filetype/sample.jpg", ContentType.JPEG);

    Media media = mediaHandler.get(asset.getPath()).build();

    UriTemplate uriTemplate = media.getAsset().getUriTemplate(UriTemplateType.SCALE_HEIGHT);
    assertEquals("https://dummy.scene7.com/is/image/DummyFolder/sample.jpg?hei={height}", uriTemplate.getUriTemplate());
    assertEquals(100, uriTemplate.getMaxWidth());
    assertEquals(50, uriTemplate.getMaxHeight());
    assertEquals(UriTemplateType.SCALE_HEIGHT, uriTemplate.getType());
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
