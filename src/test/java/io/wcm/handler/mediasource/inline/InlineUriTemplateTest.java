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
package io.wcm.handler.mediasource.inline;

import static io.wcm.handler.media.MediaNameConstants.NN_MEDIA_INLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.io.FilenameUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.scene7.api.constants.Scene7Constants;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.testcontext.MediaSourceInlineAppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.sling.commons.resource.ImmutableValueMap;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test Inline URI template for assets.
 */
@ExtendWith(AemContextExtension.class)
class InlineUriTemplateTest {

  final AemContext context = MediaSourceInlineAppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Resource inlineImage;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() throws PersistenceException {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    // prepare inline media object with real image binary data to test scaling
    inlineImage = context.resourceResolver().create(context.currentPage().getContentResource(), "inlineImage",
        ImmutableValueMap.builder()
            .put(NN_MEDIA_INLINE + "Name", "sample_image_215x102.jpg")
            .build());
    context.load().binaryResource("/sample_image_215x102.jpg",
        inlineImage.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);
  }

  @Test
  void testGetUriTemplate_CropCenter() {
    Media media = mediaHandler.get(inlineImage).build();

    assertUriTemplate(media, UriTemplateType.CROP_CENTER, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.{height}.file/sample_image_215x102.jpg");
  }

  @Test
  void testGetUriTemplate_CropCenter_EnforceOutputFileExtension() {
    Media media = mediaHandler.get(inlineImage)
        .enforceOutputFileExtension(FileExtension.PNG)
        .build();

    assertUriTemplate(media, UriTemplateType.CROP_CENTER, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.{height}.file/sample_image_215x102.png");
  }

  @Test
  void testGetUriTemplate_ScaleWidth() {
    Media media = mediaHandler.get(inlineImage).build();

    assertUriTemplate(media, UriTemplateType.SCALE_WIDTH, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample_image_215x102.jpg");
  }

  @Test
  void testGetUriTemplate_ScaleHeight() {
    Media media = mediaHandler.get(inlineImage).build();

    assertUriTemplate(media, UriTemplateType.SCALE_HEIGHT, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample_image_215x102.jpg");
  }

  Asset createSampleAsset(String classpathResource, String contentType) {
    String fileName = FilenameUtils.getName(classpathResource);
    return context.create().asset("/content/dam/" + fileName, classpathResource, contentType,
        Scene7Constants.PN_S7_FILE, "DummyFolder/" + fileName);
  }

  static void assertUriTemplate(Media media, UriTemplateType type,
      long expectedMaxWith, long expectedMaxHeight, String expectedTemplate) {
    assertTrue(media.isValid(), "media valid");
    UriTemplate uriTemplate = media.getAsset().getUriTemplate(type);
    assertEquals(type, uriTemplate.getType(), "uriTemplateType");
    assertEquals(expectedMaxWith, uriTemplate.getMaxWidth(), "maxWidth");
    assertEquals(expectedMaxHeight, uriTemplate.getMaxHeight(), "maxHeight");
    assertEquals(expectedTemplate, uriTemplate.getUriTemplate(), "uriTemplate");
  }

}
