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
package io.wcm.handler.mediasource.inline;

import static io.wcm.handler.media.MediaNameConstants.NN_MEDIA_INLINE;
import static io.wcm.handler.media.MediaNameConstants.PN_MEDIA_CROP;
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

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

/**
 * Test Inline URI template for renditions.
 */
@ExtendWith(AemContextExtension.class)
class InlineUriTemplateRenditionTest {

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
    context.load().binaryResource("/sample_image_400x250.jpg",
        inlineImage.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);
  }

  @Test
  @SuppressWarnings("null")
  void testOriginal_CropCenter() {
    Media media = mediaHandler.get(inlineImage)
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
    Media media = mediaHandler.get(inlineImage)
        .mediaFormat(RATIO_16_10)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample.jpg");
  }

  @Test
  void test4_3() {
    Media media = mediaHandler.get(inlineImage)
        .mediaFormat(RATIO_4_3)
        .autoCrop(true)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 333, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.34,0,367,250.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 333, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.34,0,367,250.file/sample.jpg");
  }

  @Test
  void testMultiple() {
    Media media = mediaHandler.get(inlineImage)
        .pictureSource(new PictureSource(RATIO_16_10).widths(120, 96))
        .pictureSource(new PictureSource(RATIO_SQUARE).widths(100))
        .pictureSource(new PictureSource(RATIO_4_3).widths(144))
        .autoCrop(true)
        .build();

    List<Rendition> renditions = List.copyOf(media.getRenditions());
    assertEquals(4, renditions.size());

    assertUriTemplate(renditions.get(0), SCALE_WIDTH, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(renditions.get(1), SCALE_WIDTH, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.file/sample.jpg");
    assertUriTemplate(renditions.get(2), SCALE_WIDTH, 250, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.75,0,325,250.file/sample.jpg");
    assertUriTemplate(renditions.get(3), SCALE_WIDTH, 333, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.{width}.0.34,0,367,250.file/sample.jpg");

    assertUriTemplate(renditions.get(0), SCALE_HEIGHT, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample.jpg");
    assertUriTemplate(renditions.get(1), SCALE_HEIGHT, 400, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.file/sample.jpg");
    assertUriTemplate(renditions.get(2), SCALE_HEIGHT, 250, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.75,0,325,250.file/sample.jpg");
    assertUriTemplate(renditions.get(3), SCALE_HEIGHT, 333, 250,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImage/mediaInline.image_file.0.{height}.34,0,367,250.file/sample.jpg");
  }

  @Test
  @SuppressWarnings("null")
  void testManualCroppingRotation() {
    inlineImage = context.create().resource(context.currentPage(), "inlineImageCropping",
        NN_MEDIA_INLINE + "Name", "sample.jpg",
        PN_MEDIA_CROP, "5,5,80,55",
        PN_MEDIA_ROTATION, 90);
    context.load().binaryResource("/sample_image_400x250.jpg",
        inlineImage.getPath() + "/" + NN_MEDIA_INLINE, ContentType.JPEG);

    Media media = mediaHandler.get(inlineImage)
        .build();

    assertUriTemplate(media.getRendition(), SCALE_WIDTH, 50, 75,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImageCropping/mediaInline.image_file.{width}.0.5,5,80,55.90.file/sample.jpg");
    assertUriTemplate(media.getRendition(), SCALE_HEIGHT, 50, 75,
        "/content/unittest/de_test/brand/de/_jcr_content/inlineImageCropping/mediaInline.image_file.0.{height}.5,5,80,55.90.file/sample.jpg");
  }

}
