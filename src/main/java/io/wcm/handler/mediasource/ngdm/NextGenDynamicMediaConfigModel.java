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

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;

/**
 * Prepares Dynamic Media with OpenAPI Remote Assets configuration for GraniteUI components (fileupload, pathfield).
 */
@Model(adaptables = SlingHttpServletRequest.class)
@ProviderType
public final class NextGenDynamicMediaConfigModel {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final Logger log = LoggerFactory.getLogger(NextGenDynamicMediaConfigModel.class);

  @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
  private NextGenDynamicMediaConfigService config;

  private boolean enabled;
  private String assetSelectorsJsUrl;
  private String configJson;

  @PostConstruct
  private void activate() {
    if (config != null) {
      enabled = config.isEnabledRemoteAssets();
      assetSelectorsJsUrl = config.getAssetSelectorsJsUrl();
      configJson = buildConfigJsonString(config);
    }
  }

  private static String buildConfigJsonString(@NotNull NextGenDynamicMediaConfigService config) {
    Map<String, Object> map = new TreeMap<>();
    map.put("repositoryId", config.getRemoteAssetsRepositoryId());
    map.put("apiKey", config.getApiKey());
    map.put("env", config.getEnv());
    try {
      return MAPPER.writeValueAsString(map);
    }
    catch (JsonProcessingException ex) {
      log.warn("Unable to serialize Dynamic Media with OpenAPI config to JSON.", ex);
      return "{}";
    }
  }

  /**
   * Check if Next Gen Dynamic Media is enabled.
   * @return true if Dynamic Media with OpenAPI for remote assets is available and enabled.
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Get Asset Selectors JavaScript URL.
   * @return Asset Selectors URL
   */
  public @Nullable String getAssetSelectorsJsUrl() {
    return this.assetSelectorsJsUrl;
  }

  /**
   * @return JSON string with configuration data required on the client-side.
   */
  public @Nullable String getConfigJson() {
    return this.configJson;
  }

}
