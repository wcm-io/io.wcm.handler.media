/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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

import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_ASSET_ID;
import static io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService.PLACEHOLDER_FORMAT;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Builds URLs for delivering videos via Dynamic Media with OpenAPI.
 */
public final class NextGenDynamicMediaVideoUrlBuilder {

  private final NextGenDynamicMediaContext context;

  /**
   * @param context Context used for placeholder replacement and configuration access
   */
  public NextGenDynamicMediaVideoUrlBuilder(@NotNull NextGenDynamicMediaContext context) {
    this.context = context;
  }

  /**
   * Build adaptive streaming manifest URL (HLS/DASH).
   * @param requestedFormat Requested format, falls back to configured default if {@code null}
   * @return Manifest URL or {@code null} if configuration is incomplete
   */
  @SuppressWarnings("null")
  public @Nullable String buildManifestUrl(@Nullable String requestedFormat) {
    String repositoryId = getRepositoryId();
    String videoDeliveryPath = context.getNextGenDynamicMediaConfig().getVideoDeliveryPath();

    if (StringUtils.isAnyBlank(repositoryId, videoDeliveryPath)) {
      return null;
    }

    String targetFormat = StringUtils.defaultIfBlank(requestedFormat,
        context.getNextGenDynamicMediaConfig().getDefaultVideoManifestFormat());

    String resolvedPath = StringUtils.replace(videoDeliveryPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    resolvedPath = StringUtils.replace(resolvedPath, PLACEHOLDER_FORMAT, targetFormat);

    return buildBaseUrl(repositoryId, resolvedPath);
  }

  /**
   * Build thumbnail/poster URL for a video (leverages the image delivery endpoint).
   * @return Thumbnail URL or {@code null} if configuration is incomplete
   */
  public @Nullable String buildThumbnailUrl() {
    // reuse image URL builder - it will automatically use "jpg" format since video file extensions
    // are not in SUPPORTED_FORMATS, causing getFileExtension() to fall back to JPEG
    return new NextGenDynamicMediaImageUrlBuilder(context).build(new NextGenDynamicMediaImageDeliveryParams());
  }

  /**
   * Build remote player URL (HTML micro-frontend hosted by Dynamic Media).
   * @return Player URL or {@code null} if configuration is incomplete
   */
  @SuppressWarnings("null")
  public @Nullable String buildPlayerUrl() {
    String repositoryId = getRepositoryId();
    String videoPlayerPath = context.getNextGenDynamicMediaConfig().getVideoPlayerPath();
    if (StringUtils.isAnyBlank(repositoryId, videoPlayerPath)) {
      return null;
    }
    String resolvedPath = StringUtils.replace(videoPlayerPath, PLACEHOLDER_ASSET_ID, context.getReference().getAssetId());
    return buildBaseUrl(repositoryId, resolvedPath);
  }

  private @Nullable String getRepositoryId() {
    if (context.getReference().getAsset() != null) {
      return context.getNextGenDynamicMediaConfig().getLocalAssetsRepositoryId();
    }
    return context.getNextGenDynamicMediaConfig().getRemoteAssetsRepositoryId();
  }

  private static String buildBaseUrl(@NotNull String repositoryId, @NotNull String path) {
    return "https://" + repositoryId + path;
  }

}

