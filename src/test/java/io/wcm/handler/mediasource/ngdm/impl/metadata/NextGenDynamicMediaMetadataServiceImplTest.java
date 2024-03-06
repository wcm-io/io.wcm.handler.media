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
package io.wcm.handler.mediasource.ngdm.impl.metadata;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.testcontext.AppAemContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigServiceImpl;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.testing.mock.aem.dam.ngdm.MockNextGenDynamicMediaConfig;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import io.wcm.wcm.commons.contenttype.ContentType;

@ExtendWith(AemContextExtension.class)
@WireMockTest
class NextGenDynamicMediaMetadataServiceImplTest {

  private static final NextGenDynamicMediaReference REFERENCE = NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE);

  private final AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
    context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class)
        .setRepositoryId("localhost:" + wmRuntimeInfo.getHttpPort());
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
  }

  @Test
  void testNotFound() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true);
    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNull(metadata);
  }

  @Test
  void testValidResponse() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true,
        "httpHeaders", new String[] { "header1:1", "header2:2" });
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .withHeader("header1", equalTo("1"))
        .withHeader("header2", equalTo("2"))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_IMAGE)));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNotNull(metadata);
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1200, dimension.getWidth());
    assertEquals(800, dimension.getHeight());
    assertEquals("image/jpeg", metadata.getMimeType());
  }

  @Test
  void testValidResponse_Disabled() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", false);
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_IMAGE)));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNull(metadata);
  }

  @Test
  void testEmptyJson() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true);
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody("{}")));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNull(metadata);
  }

  @Test
  void testInvalidResponse() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true);
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody("no json")));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNull(metadata);
  }

  @Test
  void testUnexpectdReturnCode() {
    NextGenDynamicMediaMetadataService underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true);
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_FORBIDDEN)));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNull(metadata);
  }

}
