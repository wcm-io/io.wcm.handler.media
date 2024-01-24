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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_FILENAME;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NextGenDynamicMediaTest {

  private final AemContext context = AppAemContext.newAemContext();

  private MediaHandler mediaHandler;
  private Resource resource;

  @BeforeEach
  @SuppressWarnings("null")
  void setUp() {
    MockNextGenDynamicMediaConfig nextGenDynamicMediaConfig = context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class);
    nextGenDynamicMediaConfig.setEnabled(true);
    nextGenDynamicMediaConfig.setRepositoryId("repo1");

    resource = context.create().resource(context.currentPage(), "test",
        MediaNameConstants.PN_MEDIA_REF, SAMPLE_REFERENCE);

    mediaHandler = AdaptTo.notNull(context.request(), MediaHandler.class);
  }

  @Test
  void testAsset() {
    Media media = mediaHandler.get(resource).build();
    assertTrue(media.isValid());
    assertUrl(media, "preferwebp=true&quality=85");

    Asset asset = media.getAsset();
    assertNotNull(asset);
    assertEquals(SAMPLE_FILENAME, asset.getTitle());
    assertNull(asset.getAltText());
    assertNull(asset.getDescription());
    assertEquals(SAMPLE_REFERENCE, asset.getPath());
    assertEquals(ValueMap.EMPTY, asset.getProperties());
    assertNull(asset.adaptTo(Resource.class));

    assertUrl(asset.getDefaultRendition(), "preferwebp=true&quality=85");

    Rendition fixedRendition = asset.getRendition(new MediaArgs().fixedDimension(100, 50));
    assertNotNull(fixedRendition);
    assertUrl(fixedRendition, "crop=100%3A50%2Csmart&preferwebp=true&quality=85&width=100");

    assertNotNull(asset.getImageRendition(new MediaArgs()));
    assertNull(asset.getDownloadRendition(new MediaArgs().download(true)));

    UriTemplate uriTemplate = asset.getUriTemplate(UriTemplateType.SCALE_WIDTH);
    assertUrl(uriTemplate, "preferwebp=true&quality=85&width={width}");
    assertEquals(-1, uriTemplate.getMaxWidth());
    assertEquals(-1, uriTemplate.getMaxHeight());
  }

  private static void assertUrl(Media media, String urlParams) {
    assertEquals(buildUrl(urlParams), media.getUrl());
  }

  private static void assertUrl(Rendition rendition, String urlParams) {
    assertEquals(buildUrl(urlParams), rendition.getUrl());
  }

  private static void assertUrl(UriTemplate uriTemplate, String urlParams) {
    assertEquals(buildUrl(urlParams), uriTemplate.getUriTemplate());
  }

  private static String buildUrl(String urlParams) {
    return "https://repo1/adobe/dynamicmedia/deliver/urn:aaid:aem:12345678-abcd-abcd-abcd-abcd12345678/my-image.jpg?" + urlParams;
  }

}
