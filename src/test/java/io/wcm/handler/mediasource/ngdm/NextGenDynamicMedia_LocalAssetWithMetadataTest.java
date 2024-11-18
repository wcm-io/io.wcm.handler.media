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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_UUID;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.commons.jcr.JcrConstants;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
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
class NextGenDynamicMedia_LocalAssetWithMetadataTest {

  private static final String NOT_FOUND_ASSET_UUID = "99999999-abcd-abcd-abcd-abcd99999999";

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
        "enabledLocalAssets", true,
        "localAssetsRepositoryId", "repo1");
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
    stubFor(get("/adobe/assets/urn:aaid:aem:" + NOT_FOUND_ASSET_UUID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_NOT_FOUND)));
  }

  @Test
  @SuppressWarnings("null")
  void testLocalAsset() {
    Media media = mediaHandler.get(prepareResourceWithApprovedLocalAsset())
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85", "jpg");

    // validate URI template
    Rendition rendition = media.getRendition();

    UriTemplate uriTemplateScaleWidth = rendition.getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/as/my-image.jpg?preferwebp=true&quality=85&width={width}",
        uriTemplateScaleWidth.getUriTemplate());
    assertEquals(UriTemplateType.SCALE_WIDTH, uriTemplateScaleWidth.getType());
    assertEquals(1200, uriTemplateScaleWidth.getMaxWidth());
    assertEquals(800, uriTemplateScaleWidth.getMaxHeight());

    UriTemplate uriTemplateScaleHeight = rendition.getUriTemplate(UriTemplateType.SCALE_HEIGHT);
    assertEquals("https://repo1/adobe/assets/" + SAMPLE_ASSET_ID + "/as/my-image.jpg?height={height}&preferwebp=true&quality=85",
        uriTemplateScaleHeight.getUriTemplate());
    assertEquals(UriTemplateType.SCALE_HEIGHT, uriTemplateScaleHeight.getType());
    assertEquals(1200, uriTemplateScaleHeight.getMaxWidth());
    assertEquals(800, uriTemplateScaleHeight.getMaxHeight());
  }

  @Test
  void testRendition_SetWidth() {
    Media media = mediaHandler.get(prepareResourceWithApprovedLocalAsset())
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
    Media media = mediaHandler.get(prepareResourceWithApprovedLocalAsset())
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
    Media media = mediaHandler.get(prepareResourceWithApprovedLocalAsset())
        .mediaFormat(DummyMediaFormats.RATIO_16_9)
        .fixedWidth(1024)
        .build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85&smartcrop=Landscape&width=1024", "jpg");

    Rendition rendition = media.getRendition();
    assertNotNull(rendition);

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
  void testLocalAsset_NonExistingUUID() {
    com.day.cq.dam.api.Asset asset = context.create().asset("/content/dam/my-image.jpg", 10, 10, ContentType.JPEG);
    ModifiableValueMap props = AdaptTo.notNull(asset, ModifiableValueMap.class);
    props.put(JcrConstants.JCR_UUID, NOT_FOUND_ASSET_UUID);

    resource = context.create().resource(context.currentPage(), "local-asset",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath());

    Media media = mediaHandler.get(resource)
        .build();
    assertFalse(media.isValid());
    assertEquals(MediaInvalidReason.MEDIA_REFERENCE_INVALID, media.getMediaInvalidReason());
  }

  @Test
  @SuppressWarnings("null")
  void testLocalAsset_NoUUID() {
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

  private static String buildUrl(String urlParams, String extension) {
    return "https://repo1/adobe/assets/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/as/my-image."
        + extension + "?" + urlParams;
  }

  @SuppressWarnings("null")
  private Resource prepareResourceWithApprovedLocalAsset() {
    com.day.cq.dam.api.Asset asset = context.create().asset("/content/dam/my-image.jpg", 1200, 800, ContentType.JPEG,
        ASSET_STATUS_PROPERTY, ASSET_STATUS_APPROVED);
    ModifiableValueMap props = AdaptTo.notNull(asset, ModifiableValueMap.class);
    props.put(JcrConstants.JCR_UUID, SAMPLE_UUID);

    return context.create().resource(context.currentPage(), "local-asset",
        MediaNameConstants.PN_MEDIA_REF, asset.getPath());
  }

}
