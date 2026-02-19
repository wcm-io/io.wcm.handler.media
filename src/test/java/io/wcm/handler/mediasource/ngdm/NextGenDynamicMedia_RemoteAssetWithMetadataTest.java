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
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_VIDEO;
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
import io.wcm.handler.media.VideoManifestFormat;
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
    assertUrl(media, "quality=85", "jpg");

    Asset asset = media.getAsset();
    assertNotNull(asset);
    assertEquals(SAMPLE_FILENAME, asset.getTitle());
    assertNull(asset.getDescription());
    assertEquals("my-image.jpg", asset.getAltText());
    assertNull(asset.getDescription());
    assertEquals(SAMPLE_REFERENCE, asset.getPath());
    assertEquals("approved", asset.getProperties().get("dam:assetStatus"));
    assertNull(asset.adaptTo(Resource.class));

    assertUrl(asset.getDefaultRendition(), "quality=85", "jpg");

    // default rendition
    Rendition defaultRendition = asset.getDefaultRendition();
    assertNotNull(defaultRendition);
    assertEquals(ContentType.JPEG, defaultRendition.getMimeType());
    assertEquals(1200, defaultRendition.getWidth());
    assertEquals(800, defaultRendition.getHeight());
    assertUrl(defaultRendition, "quality=85", "jpg");

    // fixed rendition
    Rendition fixedRendition = asset.getRendition(new MediaArgs().fixedDimension(100, 50));
    assertNotNull(fixedRendition);
    assertEquals(ContentType.JPEG, fixedRendition.getMimeType());
    assertEquals(100, fixedRendition.getWidth());
    assertEquals(50, fixedRendition.getHeight());
    assertUrl(fixedRendition, "crop=0.0p%2C12.5p%2C100.0p%2C75.0p&quality=85&width=100", "jpg");

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
    assertUrl(media, "quality=85&width=120", "jpg");

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
    assertUrl(media, "height=80&quality=85", "jpg");

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
    assertUrl(rendition, "quality=85&smartcrop=Landscape&width=1024", "jpg");

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
    assertUrl(rendition, "quality=85&smartcrop=Landscape&width=2048", "png");

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
    assertUrl(rendition, "crop=0.0p%2C12.5p%2C100.0p%2C75.0p&quality=85&width=100", "jpg");
  }

  @Test
  void testRendition_FixedMediaFormat() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.EDITORIAL_1COL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "crop=0.0p%2C14.5p%2C100.0p%2C71.1p&quality=85&width=215", "jpg");
  }

  @Test
  void testRendition_NonFixedSmallMediaFormat() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.NONFIXED_SMALL)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "height=20&quality=85&width=164", "jpg");
  }

  @Test
  void testRendition_NonFixedMinWidthHeightMediaFormat() {
    Media media = mediaHandler.get(resource)
        .mediaFormat(DummyMediaFormats.NONFIXED_MINWIDTHHEIGHT)
        .build();
    assertTrue(media.isValid());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertUrl(rendition, "quality=85", "jpg");
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

  @Test
  @SuppressWarnings("null")
  void testVideoStreamingDefault() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "video",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    Media media = mediaHandler.get(videoResource)
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    assertEquals(expectedBaseUrl + "/manifest.m3u8", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertTrue(rendition.isVideo());
    assertFalse(rendition.isDownload());
    assertEquals("m3u8", rendition.getFileExtension());
    assertEquals("application/vnd.apple.mpegurl", rendition.getMimeType());
    assertEquals(1920, rendition.getWidth());
    assertEquals(1080, rendition.getHeight());

    assertEquals(expectedBaseUrl + "/as/video.jpg", rendition.getProperties().get("posterUrl", String.class));
  }

  @Test
  @SuppressWarnings("null")
  void testVideoStreamingDashOverride() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "videoDash",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    Media media = mediaHandler.get(videoResource)
        .videoManifestFormat(VideoManifestFormat.DASH)
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    assertEquals(expectedBaseUrl + "/manifest.mpd", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals("mpd", rendition.getFileExtension());
    assertEquals("application/dash+xml", rendition.getMimeType());
  }

  @Test
  @SuppressWarnings("null")
  void testVideoDownloadFallback() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "videoDownload",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    Media media = mediaHandler.get(videoResource)
        .args(new MediaArgs().download(true))
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    assertEquals(expectedBaseUrl + "/original/as/video.mp4", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertTrue(rendition.isVideo());
    assertFalse(rendition.isDownload());
    assertEquals("mp4", rendition.getFileExtension());
    assertEquals("video/mp4", rendition.getMimeType());
  }

  @Test
  @SuppressWarnings("null")
  void testVideoHostedPlayer() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "videoHosted",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    Media media = mediaHandler.get(videoResource)
        .hostedVideoPlayer(true)
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    assertEquals(expectedBaseUrl + "/play", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertTrue(rendition.isVideo());
    assertFalse(rendition.isDownload());
    assertEquals("html", rendition.getFileExtension());
    assertEquals(1920, rendition.getWidth());
    assertEquals(1080, rendition.getHeight());

    assertEquals(expectedBaseUrl + "/as/video.jpg", rendition.getProperties().get("posterUrl", String.class));
  }

  @Test
  @SuppressWarnings("null")
  void testVideoHostedPlayerViaMediaArgs() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "videoHostedArgs",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    Media media = mediaHandler.get(videoResource)
        .args(new MediaArgs().hostedVideoPlayer(true))
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    assertEquals(expectedBaseUrl + "/play", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals("html", rendition.getFileExtension());
  }

  @Test
  @SuppressWarnings("null")
  void testVideoDownloadTakesPrecedenceOverHostedPlayer() {
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_VIDEO)));

    Resource videoResource = context.create().resource(context.currentPage(), "videoDownloadPrecedence",
        MediaNameConstants.PN_MEDIA_REF, "/" + SAMPLE_ASSET_ID + "/video.mp4");

    // download=true should take precedence over hostedVideoPlayer=true
    Media media = mediaHandler.get(videoResource)
        .args(new MediaArgs().download(true).hostedVideoPlayer(true))
        .build();
    assertTrue(media.isValid());

    String expectedBaseUrl = "https://" + nextGenDynamicMediaConfig.getRepositoryId() + "/adobe/assets/" + SAMPLE_ASSET_ID;
    // Should be binary URL, not player URL
    assertEquals(expectedBaseUrl + "/original/as/video.mp4", media.getUrl());

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);
    assertEquals("mp4", rendition.getFileExtension());
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
