/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2024 wcm.io
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
package io.wcm.handler.mediasource.ngdm.impl;

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.sling.commons.mime.MimeTypeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaImageUrlBuilderTest {

  private final AemContext context = AppAemContext.newAemContext();

  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;
  private MediaHandlerConfig mediaHandlerConfig;
  private MimeTypeService mimeTypeService;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class)
        .setRepositoryId("repo1");
    nextGenDynamicMediaConfig = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);

    mediaHandlerConfig = AdaptTo.notNull(context.request(), MediaHandlerConfig.class);
    mimeTypeService = context.getService(MimeTypeService.class);
  }

  @Test
  void testDefaultParams() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams();

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?preferwebp=true",
        underTest.build(params));
  }

  @Test
  void testForceOutputExtension() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder(new MediaArgs().enforceOutputFileExtension("png"), null);
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams();

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.png"
        + "?preferwebp=true",
        underTest.build(params));
  }

  @Test
  void testAllParams() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .width(100L)
        .height(50L)
        .ratio(new Dimension(16, 9))
        .rotation(90)
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?height=50&preferwebp=true&quality=60&rotate=90&width=100",
        underTest.build(params));
  }

  @Test
  void testOnlyRatio_16_9() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .ratio(new Dimension(16, 9))
        .rotation(90)
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?height=1152&preferwebp=true&quality=60&rotate=90&width=2048",
        underTest.build(params));
  }

  @Test
  void testOnlyRatio_1_2() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .ratio(new Dimension(1, 2))
        .rotation(90)
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?height=2048&preferwebp=true&quality=60&rotate=90&width=1024",
        underTest.build(params));
  }

  @Test
  void testOnlyRatio_1_1() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .ratio(new Dimension(1, 1))
        .rotation(90)
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?height=2048&preferwebp=true&quality=60&rotate=90&width=2048",
        underTest.build(params));
  }

  @Test
  void testAllParams_NamedSmartCrop() throws Exception {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder(new MediaArgs(), metadata);
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .width(100L)
        .ratio(new Dimension(16, 9))
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?preferwebp=true&quality=60&smartcrop=Landscape&width=100",
        underTest.build(params));
  }

  @Test
  void testAllParams_NamedSmartCrop_DefaultWidth() throws Exception {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder(new MediaArgs(), metadata);
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .ratio(new Dimension(16, 9))
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?preferwebp=true&quality=60&smartcrop=Landscape&width=2048",
        underTest.build(params));
  }

  @Test
  void testAllParams_NamedSmartCrop_DefaultHeight() throws Exception {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder(new MediaArgs(), metadata);
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .ratio(new Dimension(1, 2))
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?height=2048&preferwebp=true&quality=60&smartcrop=Portrait",
        underTest.build(params));
  }

  @Test
  void testAllParams_AutoCropping() throws Exception {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(METADATA_JSON_IMAGE);
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder(new MediaArgs(), metadata);
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .width(100L)
        .ratio(new Dimension(1, 1))
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?crop=200%2C0%2C800%2C800&preferwebp=true&quality=60&width=100",
        underTest.build(params));
  }

  @Test
  void testAllParams_EmptyOsgiConfig() {
    nextGenDynamicMediaConfig = context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "imageDeliveryBasePath", "",
        "assetOriginalBinaryDeliveryPath", "",
        "assetMetadataPath", "",
        "assetMetadataHeaders", new String[0]);
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .width(100L)
        .height(50L)
        .ratio(new Dimension(16, 9))
        .rotation(90)
        .quality(60);

    assertEquals("https://repo1/adobe/dynamicmedia/deliver/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/my-image.jpg"
        + "?height=50&preferwebp=true&quality=60&rotate=90&width=100",
        underTest.build(params));
  }

  @Test
  void testWidthPlaceholder() {
    NextGenDynamicMediaImageUrlBuilder underTest = getBuilder();
    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .widthPlaceholder("{w}")
        .quality(60);

    assertEquals("https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image.jpg"
        + "?preferwebp=true&quality=60&width={w}",
        underTest.build(params));
  }

  private NextGenDynamicMediaImageUrlBuilder getBuilder() {
    return getBuilder(new MediaArgs(), null);
  }

  @SuppressWarnings("null")
  private NextGenDynamicMediaImageUrlBuilder getBuilder(@NotNull MediaArgs mediaArgs,
      @Nullable NextGenDynamicMediaMetadata metadata) {
    NextGenDynamicMediaContext ctx = new NextGenDynamicMediaContext(
        NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE),
        metadata,
        null,
        mediaArgs,
        nextGenDynamicMediaConfig,
        mediaHandlerConfig,
        mimeTypeService);
    return new NextGenDynamicMediaImageUrlBuilder(ctx);
  }

}
