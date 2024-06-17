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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
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
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;

/**
 * Fetches metadata for Next Gen Dynamic Media assets via the HTTP API.
 */
@Component(service = NextGenDynamicMediaMetadataService.class, immediate = true)
@Designate(ocd = NextGenDynamicMediaMetadataServiceImpl.Config.class)
public class NextGenDynamicMediaMetadataServiceImpl implements NextGenDynamicMediaMetadataService {

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Dynamic Media with OpenAPI Metadata Service",
      description = "Fetches metadata for Dynamic Media with OpenAPI remote assets.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "When enabled, metadata is fetched for each resolved remote asset. This checks for validity/existence of "
            + "the asset and for the maximum supported resolution of the original image.")
    boolean enabled() default false;

    @AttributeDefinition(
        name = "HTTP Headers",
        description = "HTTP headers to be send with the asset metadata request. "
            + "Format: 'header1:value1'.")
    String[] httpHeaders() default { "X-Adobe-Accept-Experimental:1" };

    @AttributeDefinition(
        name = "Connect Timeout",
        description = "HTTP Connect timeout in milliseconds.")
    int connectTimeout() default 5000;

    @AttributeDefinition(
        name = "Connection Request Timeout",
        description = "HTTP connection request timeout in milliseconds.")
    int connectionRequestTimeout() default 5000;

    @AttributeDefinition(
        name = "Socket Timeout",
        description = "HTTP socket timeout in milliseconds.")
    int socketTimeout() default 5000;

    @AttributeDefinition(
        name = "Proxy Host",
        description = "Proxy host name")
    String proxyHost();

    @AttributeDefinition(
        name = "Proxy Port",
        description = "Proxy port")
    int proxyPort();

  }

  @Reference
  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;

  private boolean enabled;
  private CloseableHttpClient httpClient;

  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaMetadataServiceImpl.class);

  @Activate
  private void activate(Config config) {
    this.enabled = config.enabled();
    if (enabled) {
      httpClient = createHttpClient(config);
    }
  }

  private static CloseableHttpClient createHttpClient(Config config) {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(config.connectTimeout())
        .setConnectionRequestTimeout(config.connectionRequestTimeout())
        .setSocketTimeout(config.socketTimeout())
        .build();
    HttpClientBuilder builder = HttpClientBuilder.create()
        .setDefaultRequestConfig(requestConfig)
        .setDefaultHeaders(convertHeaders(config.httpHeaders()));
    if (StringUtils.isNotBlank(config.proxyHost()) && config.proxyPort() > 0) {
      builder.setProxy(new HttpHost(config.proxyHost(), config.proxyPort()));
    }
    return builder.build();
  }

  private static Collection<Header> convertHeaders(String[] headers) {
    List<Header> result = new ArrayList<>();
    for (String header : headers) {
      String[] parts = header.split(":", 2);
      if (parts.length == 2) {
        result.add(new BasicHeader(parts[0], parts[1]));
      }
    }
    return result;
  }

  @Deactivate
  private void deactivate() throws IOException {
    if (httpClient != null) {
      httpClient.close();
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Fetch asset metadata.
   * @param reference Asset reference
   * @return Valid asset metadata or null if not available or metadata is invalid
   */
  @Override
  public @Nullable NextGenDynamicMediaMetadata fetchMetadata(@NotNull NextGenDynamicMediaReference reference) {
    if (!enabled) {
      return null;
    }
    String metadataUrl = new NextGenDynamicMediaMetadataUrlBuilder(nextGenDynamicMediaConfig).build(reference);
    if (metadataUrl == null) {
      return null;
    }

    HttpGet httpGet = new HttpGet(metadataUrl);
    try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
      return processResponse(response, metadataUrl);
    }
    catch (IOException ex) {
      log.warn("Unable to fetch NGDM asset metadata from URL {}", metadataUrl, ex);
      return null;
    }
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
