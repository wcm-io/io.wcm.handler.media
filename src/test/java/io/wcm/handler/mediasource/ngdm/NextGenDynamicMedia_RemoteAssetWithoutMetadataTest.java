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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
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
class NextGenDynamicMedia_RemoteAssetWithoutMetadataTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Resource resource;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");

    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "enabledRemoteAssets", true);

    resource = context.create().resource(context.currentPage(), "test",
        MediaNameConstants.PN_MEDIA_REF, SAMPLE_REFERENCE);
    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testAsset() {
    Media media = mediaHandler.get(resource)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85", "jpg");

    Asset asset = media.getAsset();
    assertNotNull(asset);
    assertEquals(SAMPLE_FILENAME, asset.getTitle());
    assertEquals("my-image.jpg", asset.getAltText());
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
    assertUrl(fixedRendition, "height=50&preferwebp=true&quality=85&width=100", "jpg");

    assertNotNull(asset.getImageRendition(new MediaArgs()));
    assertNull(asset.getDownloadRendition(new MediaArgs().download(true)));

    assertUriTemplate(asset.getUriTemplate(UriTemplateType.SCALE_WIDTH),
        "preferwebp=true&quality=85&width={width}", "jpg");
    assertUriTemplate(fixedRendition.getUriTemplate(UriTemplateType.SCALE_WIDTH),
        "preferwebp=true&quality=85&width={width}", "jpg");
  }

  @Test
  void testRendition_SetWidth() {
    Media media = mediaHandler.get(resource)
        .fixedWidth(120)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85&width=120", "jpg");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(120, rendition.getWidth());
    assertEquals(0, rendition.getHeight());
  }

  @Test
  void testRendition_SetHeight() {
    Media media = mediaHandler.get(resource)
        .fixedHeight(80)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "height=80&preferwebp=true&quality=85", "jpg");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(0, rendition.getWidth());
    assertEquals(80, rendition.getHeight());
  }

  @Test
  void testRendition_16_9() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.RATIO_16_9)
        .fixedWidth(1024)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "height=576&preferwebp=true&quality=85&width=1024", "jpg");

    assertNull(rendition.getPath());
    assertEquals(SAMPLE_FILENAME, rendition.getFileName());
    assertEquals("jpg", rendition.getFileExtension());
    assertEquals(-1, rendition.getFileSize());
    assertEquals(ContentType.JPEG, rendition.getMimeType());
    assertEquals(DummyMediaFormats.RATIO_16_9, rendition.getMediaFormat());
    assertEquals(ValueMap.EMPTY, rendition.getProperties());
    assertTrue(rendition.isImage());
    assertTrue(rendition.isBrowserImage());
    assertFalse(rendition.isVectorImage());
    assertFalse(rendition.isDownload());
    assertEquals(1024, rendition.getWidth());
    assertEquals(576, rendition.getHeight());
    assertNull(rendition.getModificationDate());
    assertFalse(rendition.isFallback());
    assertNull(rendition.adaptTo(Resource.class));
    assertNotNull(rendition.toString());
  }

  @Test
  @SuppressWarnings("null")
  void testPDFDownload() {
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
    Media media = mediaHandler.get(resource)
        .args(new MediaArgs().download(true))
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(ContentType.JPEG, rendition.getMimeType());
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/original/as/my-image.jpg", rendition.getUrl());
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

}
