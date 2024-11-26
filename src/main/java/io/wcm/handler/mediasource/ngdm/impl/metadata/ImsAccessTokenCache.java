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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

/**
 * Manages IMS access tokens with expiration handling.
 */
class ImsAccessTokenCache {

  private static final long EXPERIATION_BUFFER_SEC = 5;

  // cache IMS access tokens until they expire
  private final Cache<String, AccessTokenResponse> tokenCache = Caffeine.newBuilder()
      .expireAfter(new Expiry<String, AccessTokenResponse>() {
        @Override
        public long expireAfterCreate(String key, AccessTokenResponse value, long currentTime) {
          // substract a few secs from expiration time to be on the safe side
          return TimeUnit.SECONDS.toNanos(value.expiresInSec - EXPERIATION_BUFFER_SEC);
        }
        @Override
        public long expireAfterUpdate(String key, AccessTokenResponse value, long currentTime, long currentDuration) {
          // not used
          return Long.MAX_VALUE;
        }
        @Override
        public long expireAfterRead(String key, AccessTokenResponse value, long currentTime, long currentDuration) {
          // not used
          return Long.MAX_VALUE;
        }
      })
      .build();

  private static final JsonMapper OBJECT_MAPPER = new JsonMapper();
  private static final Logger log = LoggerFactory.getLogger(ImsAccessTokenCache.class);

  private final CloseableHttpClient httpClient;
  private final String imsTokenApiUrl;

  ImsAccessTokenCache(@NotNull CloseableHttpClient httpClient, @NotNull String imsTokenApiUrl) {
    this.httpClient = httpClient;
    this.imsTokenApiUrl = imsTokenApiUrl;
  }

  /**
   * Get IMS OAuth access token
   * @param clientId Client ID
   * @param clientSecret Client Secret
   * @param scope Scope
   * @return Access token or null if access token could not be obtained
   */
  public @Nullable String getAccessToken(@NotNull String clientId, @NotNull String clientSecret, @NotNull String scope) {
    String key = clientId + "::" + scope;
    AccessTokenResponse accessTokenResponse = tokenCache.get(key, k -> createAccessToken(clientId, clientSecret, scope));
    if (accessTokenResponse != null) {
      return accessTokenResponse.accessToken;
    }
    return null;
  }

  private @Nullable AccessTokenResponse createAccessToken(@NotNull String clientId, @NotNull String clientSecret, @NotNull String scope) {
    List<NameValuePair> formData = new ArrayList<>();
    formData.add(new BasicNameValuePair("grant_type", "client_credentials"));
    formData.add(new BasicNameValuePair("client_id", clientId));
    formData.add(new BasicNameValuePair("client_secret", clientSecret));
    formData.add(new BasicNameValuePair("scope", scope));

    HttpPost httpPost = new HttpPost(imsTokenApiUrl);
    httpPost.setEntity(new UrlEncodedFormEntity(formData, StandardCharsets.UTF_8));

    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      return processResponse(response);
    }
    catch (IOException ex) {
      log.warn("Unable to obtain access token from URL {}", imsTokenApiUrl, ex);
      return null;
    }
  }

  @SuppressWarnings("null")
  private @Nullable AccessTokenResponse processResponse(@NotNull CloseableHttpResponse response) throws IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      String jsonResponse = EntityUtils.toString(response.getEntity());
      AccessTokenResponse accessTokenResponse = OBJECT_MAPPER.readValue(jsonResponse, AccessTokenResponse.class);
      log.trace("HTTP response for access token reqeust from {} returned a response, expires in {} sec",
          imsTokenApiUrl, accessTokenResponse.expiresInSec);
      return accessTokenResponse;
    }
    else {
      log.warn("Unexpected HTTP response for access token request from {}: {}", imsTokenApiUrl, response.getStatusLine());
      return null;
    }
  }

}
