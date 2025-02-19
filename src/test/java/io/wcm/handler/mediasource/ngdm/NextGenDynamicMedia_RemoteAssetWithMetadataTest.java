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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_PDF;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_SVG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.wcm.handler.media.Asset;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.media.testcontext.DummyMediaFormats;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadataServiceImpl;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
@WireMockTest
class NextGenDynamicMedia_RemoteAssetWithMetadataTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig;
  private MediaHandler mediaHandler;
  private Resource resource;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("localhost:" + wmRuntimeInfo.getHttpPort());
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class,
        "enabledRemoteAssets", true);
    context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true);

    resource = context.create().resource(context.currentPage(), "test",
        MediaNameConstants.PN_MEDIA_REF, SAMPLE_REFERENCE);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);

    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_IMAGE)));
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
    assertNull(asset.getDescription());
    assertEquals("my-image.jpg", asset.getAltText());
    assertNull(asset.getDescription());
    assertEquals(SAMPLE_REFERENCE, asset.getPath());
    assertEquals("approved", asset.getProperties().get("dam:assetStatus"));
    assertNull(asset.adaptTo(Resource.class));

    assertUrl(asset.getDefaultRendition(), "preferwebp=true&quality=85", "jpg");

    // default rendition
    Rendition defaultRendition = asset.getDefaultRendition();
    assertNotNull(defaultRendition);
    assertEquals(ContentType.JPEG, defaultRendition.getMimeType());
    assertEquals(1200, defaultRendition.getWidth());
    assertEquals(800, defaultRendition.getHeight());
    assertUrl(defaultRendition, "preferwebp=true&quality=85", "jpg");

    // fixed rendition
    Rendition fixedRendition = asset.getRendition(new MediaArgs().fixedDimension(100, 50));
    assertNotNull(fixedRendition);
    assertEquals(ContentType.JPEG, fixedRendition.getMimeType());
    assertEquals(100, fixedRendition.getWidth());
    assertEquals(50, fixedRendition.getHeight());
    assertUrl(fixedRendition, "crop=0%2C100%2C1200%2C600&preferwebp=true&quality=85&width=100", "jpg");

    // avoid upscaling
    Rendition tooLargeRendition = asset.getRendition(new MediaArgs().fixedDimension(2048, 1024));
    assertNull(tooLargeRendition);
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
    assertEquals(80, rendition.getHeight());
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
    assertEquals(120, rendition.getWidth());
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
    assertUrl(rendition, "preferwebp=true&quality=85&smartcrop=Landscape&width=1024", "jpg");

    assertNull(rendition.getPath());
    assertEquals(SAMPLE_FILENAME, rendition.getFileName());
    assertEquals("jpg", rendition.getFileExtension());
    assertEquals(250467, rendition.getFileSize());
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
  void testRendition_16_9_PNG() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.RATIO_16_9)
        .enforceOutputFileExtension("png")
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "preferwebp=true&quality=85&smartcrop=Landscape&width=2048", "png");

    assertEquals("png", rendition.getFileExtension());
  }

  @Test
  void testRendition_FixedDimension() {
    Media media = mediaHandler.get(resource)
        .fixedDimension(100, 50)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=0%2C100%2C1200%2C600&preferwebp=true&quality=85&width=100", "jpg");
  }

  @Test
  void testRendition_FixedMediaFormat() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.EDITORIAL_1COL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=0%2C116%2C1200%2C569&preferwebp=true&quality=85&width=215", "jpg");
  }

  @Test
  void testRendition_NonFixedSmallMediaFormat() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.NONFIXED_SMALL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "height=20&preferwebp=true&quality=85&width=164", "jpg");
  }

  @Test
  void testRendition_NonFixedMinWidthHeightMediaFormat() {
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
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_PDF)));

    Resource downloadResource = context.create().resource(context.currentPage(), "download",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/myfile.pdf");

    Media media = mediaHandler.get(downloadResource)
        .args(new MediaArgs().download(true))
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(ContentType.PDF, rendition.getMimeType());
    assertEquals("https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID + "/original/as/myfile.pdf",
        rendition.getUrl());
  }

  @Test
  @SuppressWarnings("null")
  void testSVG() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_SVG)));

    Resource downloadResource = context.create().resource(context.currentPage(), "image",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/myfile.svg");

    Media media = mediaHandler.get(downloadResource)
        .fixedWidth(1200)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals(ContentType.SVG, rendition.getMimeType());
    assertEquals(900, rendition.getWidth());
    assertEquals(600, rendition.getHeight());
    assertEquals("https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID + "/original/as/myfile.svg",
        rendition.getUrl());
  }

  private void assertUrl(Media media, String urlParams, String extension) {
    assertEquals(buildUrl(urlParams, extension), media.getUrl());
  }

  private void assertUrl(Rendition rendition, String urlParams, String extension) {
    assertEquals(buildUrl(urlParams, extension), rendition.getUrl());
  }

  private String buildUrl(String urlParams, String extension) {
    return "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image."
        + extension + "?" + urlParams;
  }

}
