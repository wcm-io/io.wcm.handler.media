/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import org.apache.sling.api.adapter.Adaptable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.url.UrlMode;

/**
 * Read image profiles stored in /conf resources.
 * Image profiles are usually stored at /conf/global/settings/dam/adminui-extension/imageprofile.
 */
public interface DynamicMediaSupportService {

  /**
   * @return Whether dynamic media is enabled on this AEM instance
   */
  boolean isDynamicMediaEnabled();

  /**
   * @param isDynamicMediaAsset true if given asset has DM metadata properties available.
   * @return Whether dynamic media capability is enabled for the given asset
   */
  boolean isDynamicMediaCapabilityEnabled(boolean isDynamicMediaAsset);

  /**
   * @return Whether a transparent fallback to Media Handler-based rendering of renditions is allowed
   *         if the appropriate Dynamic Media metadata is not preset for an asset.
   */
  boolean isAemFallbackDisabled();

  /**
   * @return Whether to validate that the renditions defined via smart cropping fulfill the requested image width/height
   *         to avoid upscaling or white borders.
   */
  boolean isValidateSmartCropRenditionSizes();

  /**
   * @return Reply image size limit as configured in dynamic media.
   */
  @NotNull
  Dimension getImageSizeLimit();

  /**
   * @return Whether to control image quality for lossy output formats for each media request via 'qlt' URL parameter
   *         (instead of relying on default setting within Dynamic Media).
   */
  boolean isSetImageQuality();

  /**
   * @return Default response image format. If empty, the default setting that is configured on the Dynamic Media server
   *         environment is used. Accepts the same values as the 'fmt' parameter from the Dynamic Media Image Service
   *         API.
   */
  @NotNull
  String getDefaultFmt();

  /**
   * @return Default response image format for source images that may have an alpha channel (e.g. for PNG). Accepts the
   *         same values as the 'fmt' parameter from the Dynamic Media Image Service API.
   */
  @NotNull
  String getDefaultFmtAlpha();

  /**
   * Get image profile.
   * @param profilePath Full profile path
   * @return Profile or null if no profile found
   */
  @Nullable
  ImageProfile getImageProfile(@NotNull String profilePath);

  /**
   * Get image profile for given asset.
   * @param asset DAM asset
   * @return Profile or null if no profile found
   */
  @Nullable
  ImageProfile getImageProfileForAsset(@NotNull Asset asset);

  /**
   * Get scene7 host/URL prefix for publish environment.
   * @param asset DAM asset
   * @param urlMode URL mode
   * @param adaptable Adaptable
   * @return Protocol and hostname of scene7 host or null.
   *         If author preview mode is enabled, returns empty string.
   */
  @Nullable
  String getDynamicMediaServerUrl(@NotNull Asset asset, @Nullable UrlMode urlMode, @NotNull Adaptable adaptable);

}
