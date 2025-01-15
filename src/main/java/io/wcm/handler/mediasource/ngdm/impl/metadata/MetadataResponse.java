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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Used for Jackson Object mapping of JSON response from NGDM HTTP API.
 */
@SuppressWarnings({ "checkstyle:VisibilityModifierCheck", "java:S1104" })
@SuppressFBWarnings("UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD")
@JsonIgnoreProperties(ignoreUnknown = true)
final class MetadataResponse {

  public RepositoryMetadata repositoryMetadata;
  public Map<String, Object> assetMetadata;

  @JsonIgnoreProperties(ignoreUnknown = true)
  static final class RepositoryMetadata {
    @JsonProperty("dc:format")
    public String dcFormat;
    @JsonProperty("repo:size")
    public Long repoSize;
    @JsonProperty("smartcrops")
    public Map<String, SmartCrop> smartCrops;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static final class SmartCrop {
    public double left;
    public double top;
    public double normalizedWidth;
    public double normalizedHeight;
  }

}
