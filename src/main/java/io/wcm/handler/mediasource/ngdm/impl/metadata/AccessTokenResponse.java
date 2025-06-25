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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used for Jackson Object mapping of JSON response from IMS Token v3 API.
 */
@SuppressWarnings({ "checkstyle:VisibilityModifierCheck", "checkstyle:JavadocVariable", "java:S1104" })
@JsonIgnoreProperties(ignoreUnknown = true)
final class AccessTokenResponse {

  @JsonProperty("access_token")
  public String accessToken;

  @JsonProperty("token_type")
  public String tokenType;

  @JsonProperty("expires_in")
  public long expiresInSec;

}
