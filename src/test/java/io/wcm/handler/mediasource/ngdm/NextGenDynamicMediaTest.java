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
package io.wcm.handler.mediasource.ngdm;

import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_APPROVED;
import static com.day.cq.dam.api.DamConstants.ASSET_STATUS_PROPERTY;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.commons.jcr.JcrConstants;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaTest {

  private final AemContext context = AppAemContext.newAemContext();

  private Resource resource;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    resource = context.create().resource(context.currentPage(), "test",
        MediaNameConstants.PN_MEDIA_REF, SAMPLE_REFERENCE);
  }

  @Test
  void testAsset() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85", "jpg");

    Asset asset = media.getAsset();
    assertNotNull(asset);
    assertEquals(SAMPLE_FILENAME, asset.getTitle());
    assertNull(asset.getAltText());
    assertNull(asset.getDescription());
    assertEquals(SAMPLE_REFERENCE, asset.getPath());
    assertEquals(ValueMap.EMPTY, asset.getProperties());
    assertNull(asset.adaptTo(Resource.class));

    assertUrl(asset.getDefaultRendition(), "preferwebp=true&quality=85", "jpg");

    // default rendition
    Rendition defaultRendition = asset.getDefaultRendition();
    assertNotNull(defaultRendition);
    assertEquals(ContentType.JPEG, defaultRendition.getMimeType());
    assertEquals(0, defaultRendition.getWidth());
    assertEquals(0, defaultRendition.getHeight());
    assertUrl(defaultRendition, "preferwebp=true&quality=85", "jpg");

    // fixed rendition
    Rendition fixedRendition = asset.getRendition(new MediaArgs().fixedDimension(100, 50));
    assertNotNull(fixedRendition);
    assertEquals(ContentType.JPEG, fixedRendition.getMimeType());
    assertUrl(fixedRendition, "crop=100%3A50%2Csmart&preferwebp=true&quality=85&width=100", "jpg");

    assertNotNull(asset.getImageRendition(new MediaArgs()));
    assertNull(asset.getDownloadRendition(new MediaArgs().download(true)));

    UriTemplate uriTemplate = asset.getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertUriTemplate(uriTemplate, "preferwebp=true&quality=85&width={width}", "jpg");
  }

  @Test
  void testRendition_16_10() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.RATIO_16_10)
        .fixedWidth(2048)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=16%3A10%2Csmart&preferwebp=true&quality=85&width=2048", "jpg");

    assertNull(rendition.getPath());
    assertEquals(SAMPLE_FILENAME, rendition.getFileName());
    assertEquals("jpg", rendition.getFileExtension());
    assertEquals(-1, rendition.getFileSize());
    assertEquals(ContentType.JPEG, rendition.getMimeType());
    assertEquals(DummyMediaFormats.RATIO_16_10, rendition.getMediaFormat());
    assertEquals(ValueMap.EMPTY, rendition.getProperties());
    assertTrue(rendition.isImage());
    assertTrue(rendition.isBrowserImage());
    assertFalse(rendition.isVectorImage());
    assertFalse(rendition.isDownload());
    assertEquals(2048, rendition.getWidth());
    assertEquals(1280, rendition.getHeight());
    assertNull(rendition.getModificationDate());
    assertFalse(rendition.isFallback());
    assertNull(rendition.adaptTo(Resource.class));
    assertNotNull(rendition.toString());

    UriTemplate uriTemplate = rendition.getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertUriTemplate(uriTemplate, "crop=16%3A10%2Csmart&preferwebp=true&quality=85&width={width}", "jpg");
  }

  @Test
  void testRendition_16_10_PNG() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.RATIO_16_10)
        .enforceOutputFileExtension("png")
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=16%3A10%2Csmart&preferwebp=true&quality=85", "png");

    assertEquals("png", rendition.getFileExtension());
  }

  @Test
  void testRendition_FixedDimension() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .fixedDimension(100, 50)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=100%3A50%2Csmart&preferwebp=true&quality=85&width=100", "jpg");
  }

  @Test
  void testRendition_FixedMediaFormat() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.EDITORIAL_1COL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=210784%3A100000%2Csmart&preferwebp=true&quality=85&width=215", "jpg");
  }

  @Test
  void testRendition_NonFixedSmallMediaFormat() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.NONFIXED_SMALL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "preferwebp=true&quality=85&width=164", "jpg");
  }

  @Test
  void testRendition_NonFixedMinWidthHeightMediaFormat() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.NONFIXED_MINWIDTHHEIGHT)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "preferwebp=true&quality=85", "jpg");
  }

  @Test
  @SuppressWarnings("null")
  void testPDFDownload() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Resource downloadResource = context.create().resource(context.currentPage(), "download",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/myfile.pdf");

    Media media = mediaHandler.get(downloadResource)
        .args(new MediaArgs().download(true))
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(ContentType.PDF, rendition.getMimeType());
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/original/as/myfile.pdf", rendition.getUrl());
  }

  @Test
  void testImageDownload() {
    setupNGDM(false);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    Media media = mediaHandler.get(resource)
        .args(new MediaArgs().download(true))
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(ContentType.JPEG, rendition.getMimeType());
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/original/as/my-image.jpg", rendition.getUrl());
  }

  @Test
  @SuppressWarnings("null")
  void testLocalAsset() {
    setupNGDM(true);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    com.day.cq.dam.api.Asset asset = context.create().asset("/content/dam/my-image.jpg", 20, 10, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_APPROVED);
    ModifiableValueMap props = AdaptTo.notNull(asset, ModifiableValueMap.class);
    props.put(JcrConstants.JCR_UUID, SAMPLE_UUID);

    resource = context.create().resource(context.currentPage(), "local-asset",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath());

    Media media = mediaHandler.get(resource)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85", "jpg");

    // validate URI template
    Rendition rendition = media.getRendition();
    UriTemplate uriTemplate = rendition.getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/as/my-image.jpg?preferwebp=true&quality=85&width={width}",
        uriTemplate.getUriTemplate());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
    assertEquals(20, uriTemplate.getMaxWidth());
    assertEquals(10, uriTemplate.getMaxHeight());
  }

  @Test
  @SuppressWarnings("null")
  void testLocalAsset_NotApproved() {
    setupNGDM(true);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    com.day.cq.dam.api.Asset asset = context.create().asset("/content/dam/my-image.jpg", 10, 10, ContentType.JPEG);
    ModifiableValueMap props = AdaptTo.notNull(asset, ModifiableValueMap.class);
    props.put(JcrConstants.JCR_UUID, SAMPLE_UUID);

    resource = context.create().resource(context.currentPage(), "local-asset",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath());

    Media media = mediaHandler.get(resource)
        .build();
    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.NOT_APPROVED, media.getMediaInvalidReason());
  }

  @Test
  @SuppressWarnings("null")
  void testLocalAsset_NoUUID() {
    setupNGDM(true);
    MediaHandler mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    com.day.cq.dam.api.Asset asset = context.create().asset("/content/dam/my-image.jpg", 10, 10, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_APPROVED);

    resource = context.create().resource(context.currentPage(), "local-asset",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath());

    Media media = mediaHandler.get(resource)
        .build();
    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason());
  }

  private static void assertUrl(Media media, String urlParams, String extension) {
    assertEquals(buildUrl(urlParams, extension), media.getUrl());
  }

  private static void assertUrl(Rendition rendition, String urlParams, String extension) {
    assertEquals(buildUrl(urlParams, extension), rendition.getUrl());
  }

  private static void assertUriTemplate(UriTemplate uriTemplate, String urlParams, String extension) {
    assertEquals(buildUrl(urlParams, extension), uriTemplate.getUriTemplate());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplate.getType());
    assertEquals(0, uriTemplate.getMaxWidth());
    assertEquals(0, uriTemplate.getMaxHeight());
  }

  private static String buildUrl(String urlParams, String extension) {
    return "https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image."
        + extension + "?" + urlParams;
  }

  private void setupNGDM(boolean localAssets) {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");

    if (localAssets) {
      context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
          "enabledLocalAssets", true,
          "localAssetsRepositoryId", "repo1");
    }
    else {
      context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
    }
  }

}
