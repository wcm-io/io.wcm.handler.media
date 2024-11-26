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
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReferenceSample.SAMPLE_REFERENCE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE;
import static io.wcm.handler.mediasource.ngdm.impl.metadata.MetadataSample.METADATA_JSON_IMAGE_FULL;
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
class NextGenDynamicMediaMetadataServiceImpl_ImsAccessTokenTest {

  private static final NextGenDynamicMediaReference REFERENCE = NextGenDynamicMediaReference.fromReference(SAMPLE_REFERENCE);

  private static final String AUTHENTICATION_CLIENT_ID = "testClientId";
  private static final String AUTHENTICATION_CLIENT_SECRET = "testClientSecret";
  private static final String AUTHENTICATION_SCOPE = "testScope";
  private static final String ACCESS_TOKEN = "testToken";
  private static final long ACCESS_TOKEN_EXPIRES_SEC = 100;

  private static final String ACCESS_TOKEN_RESPONSE = "{"
      + "  \"access_token\": \"" + ACCESS_TOKEN + "\","
      + "  \"token_type\": \"Bearer\","
      + "  \"expires_in\": " + ACCESS_TOKEN_EXPIRES_SEC
      + "}";

  private final AemContext context = AppAemContext.newAemContext();

  private NextGenDynamicMediaMetadataService underTest;

  @BeforeEach
  void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
    context.registerInjectActivateService(MockNextGenDynamicMediaConfig.class)
        .setRepositoryId("localhost:" + wmRuntimeInfo.getHttpPort());
    context.registerInjectActivateService(NextGenDynamicMediaConfigServiceImpl.class);
    underTest = context.registerInjectActivateService(NextGenDynamicMediaMetadataServiceImpl.class,
        "enabled", true,
        "imsTokenApiUrl", "http://localhost:" + wmRuntimeInfo.getHttpPort() + "/ims/token/v3",
        "authenticationClientId", AUTHENTICATION_CLIENT_ID,
        "authenticationClientSecret", AUTHENTICATION_CLIENT_SECRET,
        "authenticationScope", AUTHENTICATION_SCOPE);

    // without auth
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_IMAGE)));

    // with auth
    stubFor(get("/adobe/assets/" + SAMPLE_ASSET_ID + "/metadata")
        .withHeader("Authorization", equalTo("Bearer " + ACCESS_TOKEN))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(METADATA_JSON_IMAGE_FULL)));
  }

  @Test
  void testValidToken() {
    stubFor(post("/ims/token/v3")
        .withFormParam("grant_type", equalTo("client_credentials"))
        .withFormParam("client_id", equalTo(AUTHENTICATION_CLIENT_ID))
        .withFormParam("client_secret", equalTo(AUTHENTICATION_CLIENT_SECRET))
        .withFormParam("scope", equalTo(AUTHENTICATION_SCOPE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_OK)
            .withHeader("Content-Type", ContentType.JSON)
            .withBody(ACCESS_TOKEN_RESPONSE)));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNotNull(metadata);
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1500, dimension.getWidth());
    assertEquals(900, dimension.getHeight());
    assertEquals("image/jpeg", metadata.getMimeType());
    assertEquals("Test Image", metadata.getProperties().get("dc:title", String.class));
    assertEquals("Test Description", metadata.getProperties().get("dc:description", String.class));
  }

  @Test
  void testInvalidToken_FallbackNoAuth() {
    stubFor(post("/ims/token/v3")
        .withFormParam("grant_type", equalTo("client_credentials"))
        .withFormParam("client_id", equalTo(AUTHENTICATION_CLIENT_ID))
        .withFormParam("client_secret", equalTo(AUTHENTICATION_CLIENT_SECRET))
        .withFormParam("scope", equalTo(AUTHENTICATION_SCOPE))
        .willReturn(aResponse()
            .withStatus(HttpStatus.SC_UNAUTHORIZED)));

    NextGenDynamicMediaMetadata metadata = underTest.fetchMetadata(REFERENCE);
    assertNotNull(metadata);
    Dimension dimension = metadata.getDimension();
    assertNotNull(dimension);
    assertEquals(1200, dimension.getWidth());
    assertEquals(800, dimension.getHeight());
    assertEquals("image/jpeg", metadata.getMimeType());
    assertNull(metadata.getProperties().get("dc:title", String.class));
    assertNull(metadata.getProperties().get("dc:description", String.class));
  }

}
