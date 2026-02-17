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
package io.wcm.handler.media;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptive streaming manifest formats for video delivery.
 *
 * <p>
 * These formats represent the <em>delivery</em> manifest that references multiple encoded qualities of the original
 * video asset. They are distinct from the original binary file types (MP4, WEBM, MOV) that are still tracked via
 * {@link io.wcm.handler.media.MediaFileType}.
 * </p>
 */
@ProviderType
public enum VideoManifestFormat {

  /**
   * HTTP Live Streaming manifest (<code>.m3u8</code>).
   */
  HLS("m3u8", "application/vnd.apple.mpegurl"),

  /**
   * MPEG-DASH manifest (<code>.mpd</code>).
   */
  DASH("mpd", "application/dash+xml");

  private static final Logger log = LoggerFactory.getLogger(VideoManifestFormat.class);

  private final String extension;
  private final String mimeType;

  VideoManifestFormat(@NotNull String extension, @NotNull String mimeType) {
    this.extension = extension;
    this.mimeType = mimeType;
  }

  /**
   * File extension of the manifest without leading dot.
   * @return File extension
   */
  public @NotNull String getExtension() {
    return extension;
  }

  /**
   * MIME type for playlist delivery.
   * @return MIME type
   */
  public @NotNull String getMimeType() {
    return mimeType;
  }

  /**
   * Parses a format string to enum, falling back to {@link #HLS} if invalid.
   * @param value Format string (e.g. "HLS" or "DASH"), case-insensitive
   * @return Parsed format or {@link #HLS} as fallback
   */
  public static @NotNull VideoManifestFormat fromString(@NotNull String value) {
    try {
      return valueOf(StringUtils.upperCase(value));
    }
    catch (IllegalArgumentException ex) {
      log.warn("Unsupported video manifest format '{}', falling back to HLS.", value);
      return HLS;
    }
  }

}
