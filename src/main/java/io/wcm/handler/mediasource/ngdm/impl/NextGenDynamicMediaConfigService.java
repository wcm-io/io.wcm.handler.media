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

import org.jetbrains.annotations.Nullable;

/**
 * Service to access Next Generation Dynamic Media configuration.
 */
public interface NextGenDynamicMediaConfigService {

  /**
   * Placeholder for Asset ID used in delivery base paths.
   */
  String PLACEHOLDER_ASSET_ID = "{asset-id}";

  /**
   * Placeholder for SEO name used in delivery base paths.
   */
  String PLACEHOLDER_SEO_NAME = "{seo-name}";

  /**
   * Placeholder for format (image file extension) used in delivery base paths.
   */
  String PLACEHOLDER_FORMAT = "{format}";

  /**
   * Checks if enabled for remote assets, and the appropriate configuration is present.
   * @return true if enabled for remote assets
   */
  boolean isEnabledRemoteAssets();

  /**
   * Enable Next Generation Dynamic Media for local assets in this AEMaaCS instance.
   * @return true if enabled for local assets.
   */
  boolean isEnabledLocalAssets();

  /**
   * Gets the absolute URL for the javascript which contains the microfrontend for the remote asset selector.
   * @return the absolute URL for the javascript which contains the microfrontend for the remote asset selector
   */
  @Nullable
  String getAssetSelectorsJsUrl();

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
  @Nullable
  String getImageDeliveryBasePath();

  /**
   * Gets the path expression for the adaptive video manifest/player path. The
   * following placeholders with the below meaning are contained
   * within that path:
   * <ul>
   * <li><code>{asset-id}</code> - the uuid of the asset in the format 'urn:aaid:aem:UUID'
   * along with optional format
   * e.g. urn:aaid:aem:1a034bee-ebda-4787-bad3-f924d0772b75 OR
   * urn:aaid:aem:1a034bee-ebda-4787-bad3-f924d0772b75.mp4</li>
   * </ul>
   * @return the path expression for the video delivery path
   */
  @Nullable
  String getVideoDeliveryPath();

  /**
   * Gets the path expression for the the Original Asset Delivery which delivers
   * the bitstream as-is
   * <ul>
   * <li><code>{asset-id}</code> - the uuid of the asset in the format 'urn:aaid:aem:UUID',
   * e.g. urn:aaid:aem:1a034bee-ebda-4787-bad3-f924d0772b75</li>
   * <li><code>{seo-name}</code> - any url-encoded or alphanumeric, non-whitespace set of
   * characters. may contain hyphens and dots</li>
   * </ul>
   * @return the path expression for the asset (bitstream) delivery path
   */
  @Nullable
  String getAssetOriginalBinaryDeliveryPath();

  /**
   * Gets the path expression for getting the metadata of an asset. The following
   * placeholders with the below meaning are contained within
   * that path:
   * <ul>
   * <li><code>{asset-id}</code> - the uuid of the asset in the format 'urn:aaid:aem:UUID',
   * e.g. urn:aaid:aem:1a034bee-ebda-4787-bad3-f924d0772b75</li>
   * </ul>
   * @return the path expression for the metadata path
   */
  @Nullable
  String getAssetMetadataPath();

  /**
   * Gets the Next Generation Dynamic Media tenant (also known technically as the repository ID) for remote assets.
   * @return the repository ID
   */
  @Nullable
  String getRemoteAssetsRepositoryId();

  /**
   * Repository ID for serving local assets.
   * @return the repository ID
   */
  @Nullable
  String getLocalAssetsRepositoryId();

  /**
   * Gets the API key for accessing the asset selectors UI
   * @return the API key for accessing the asset selectors UI
   */
  @Nullable
  String getApiKey();

  /**
   * Gets the environment string which should be 'PROD' or 'STAGE'
   * @return the environment string
   */
  @Nullable
  String getEnv();

  /**
   * Gets the IMS client identifier
   * @return the IMS client identifier
   */
  @Nullable
  String getImsClient();

  /**
   * Gets the default image width/height (longest edge) when requesting image renditions without explicit dimension.
   * @return default width/height
   */
  long getImageWidthHeightDefault();

}
