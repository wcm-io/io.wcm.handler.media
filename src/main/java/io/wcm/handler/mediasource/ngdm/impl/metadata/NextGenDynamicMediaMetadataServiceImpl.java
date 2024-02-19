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
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;

/**
 * Fetches metadata for Next Gen Dynamic Media assets via the HTTP API.
 */
@Component(service = NextGenDynamicMediaMetadataService.class, immediate = true)
public class NextGenDynamicMediaMetadataServiceImpl implements NextGenDynamicMediaMetadataService {

  @Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;

  private CloseableHttpClient httpClient;

  private static final int HTTP_TIMEOUT_SEC = 5;

  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaMetadataServiceImpl.class);


  @Activate
  private void activate() {
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(HTTP_TIMEOUT_SEC * 1000)
        .setConnectionRequestTimeout(HTTP_TIMEOUT_SEC * 1000)
        .setSocketTimeout(HTTP_TIMEOUT_SEC * 1000)
        .build();
    httpClient = HttpClientBuilder.create()
        .setDefaultRequestConfig(config)
        .setDefaultHeaders(nextGenDynamicMediaConfig.getAssetMetadataHeaders().entrySet().stream()
            .map(header -> new BasicHeader(header.getKey(), header.getValue()))
            .collect(Collectors.toList()))
        .build();
  }

  @Deactivate
  private void deactivate() throws IOException {
    httpClient.close();
  }

  /**
   * Fetch asset metadata.
   * @param reference Asset reference
   * @return Valid asset metadata or null if not available or metadata is invalid
   */
  @Override
  public @Nullable NextGenDynamicMediaMetadata fetchMetadata(@NotNull NextGenDynamicMediaReference reference) {
    String metadataUrl = new NextGenDynamicMediaMetadataUrlBuilder(nextGenDynamicMediaConfig).build(reference);

    if (metadataUrl != null) {
      HttpGet httpGet = new HttpGet(metadataUrl);
      try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
        return processResponse(response, metadataUrl);
      }
      catch (IOException ex) {
        log.warn("Unable to fetch NGDM asset metadata from URL {}", metadataUrl, ex);
      }
    }

    // fetch metadata failed
    return null;
  }

  private @Nullable NextGenDynamicMediaMetadata processResponse(@NotNull CloseableHttpResponse response,
      @NotNull String metadataUrl) throws IOException {
    switch (response.getStatusLine().getStatusCode()) {
      case HttpStatus.SC_OK:
        String jsonResponse = EntityUtils.toString(response.getEntity());
        NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromJson(jsonResponse);
        log.trace("HTTP response for NGDM asset metadata {} returns: {}", metadataUrl, metadata);
        if (metadata.isValid()) {
          return metadata;
        }
        break;
      case HttpStatus.SC_NOT_FOUND:
        log.trace("HTTP response for NGDM asset metadata {} returns HTTP 404", metadataUrl);
        break;
      default:
        log.warn("Unexpected HTTP response for NGDM asset metadata {}: {}", metadataUrl, response.getStatusLine());
        break;
    }
    return null;
  }

}
