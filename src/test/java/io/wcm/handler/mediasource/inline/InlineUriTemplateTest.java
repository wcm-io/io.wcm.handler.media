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
import static io.wcm.handler.media.UriTemplateType.CROP_CENTER;
import static io.wcm.handler.media.UriTemplateType.SCALE_HEIGHT;
import static io.wcm.handler.media.UriTemplateType.SCALE_WIDTH;
import static io.wcm.handler.media.testcontext.UriTemplateAssert.assertUriTemplate;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Test Inline URI template for assets.
 */
@ExtendWith(AemContextExtension.class)
class InlineUriTemplateTest {

  final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Resource inlineImage;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    // prepare inline media object with real image binary data to test scaling
    inlineImage = context.create().resource(context.currentPage(), "inlineImage",
        NN_MEDIA_INLINE + "Name", "sample.jpg");
    context.load().binaryResource("/sample_image_215x102.jpg",
        inlineImage.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);
  }

  @Test
  void testGetUriTemplate() {
    Media media = mediaHandler.get(inlineImage).build();

    assertUriTemplate(media, CROP_CENTER, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.{height}.file/sample.jpg");
    assertUriTemplate(media, SCALE_WIDTH, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(media, SCALE_HEIGHT, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void testGetUriTemplate_EnforceOutputFileExtension() {
    Media media = mediaHandler.get(inlineImage)
        .enforceOutputFileExtension(FileExtension.PNG)
        .build();

    assertUriTemplate(media, CROP_CENTER, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.{height}.file/sample.png");
    assertUriTemplate(media, SCALE_WIDTH, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample.png");
    assertUriTemplate(media, SCALE_HEIGHT, 215, 102,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample.png");
  }

}
