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
package io.wcm.handler.mediasource.ngdm.impl;

/**
 * Service to access Next Generation Dynamic Media configuration.
 */
public interface NextGenDynamicMediaConfigService {

  /**
   * Checks if the configuration/feature is enabled.
   * @return true if enabled and false otherwise
   */
  boolean enabled();

  /**
   * Gets the path expression for the image delivery path. The following placeholders with the below meaning are
   * contained within that path:
   * <ul>
   * <li><code>{asset-id}</code> - the uuid of the asset in the format 'urn:aaid:aem:UUID', e.g.
   * urn:aaid:aem:1a034bee-ebda-4787-bad3-f924d0772b75</li>
   * <li><code>{seo-name}</code> - any url-encoded or alphanumeric, non-whitespace set of characters. may contain
   * hyphens and
   * dots</li>
   * <li><code>{format}</code> - output format</li>
   * </ul>
   * @return the path expression for the image delivery path
   */
  String getImageDeliveryBasePath();

  /**
   * Gets the Next Generation Dynamic Media tenant (also known technically as the repository ID).
   * @return the repository ID
   */
  String getRepositoryId();

}
