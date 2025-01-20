/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
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
package io.wcm.handler.media.format;

import java.util.Comparator;
import java.util.SortedSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Media format handling.
 *
 * <p>
 * The interface is implemented by a Sling Model. You can adapt from
 * {@link org.apache.sling.api.SlingHttpServletRequest} or {@link org.apache.sling.api.resource.Resource} to get a
 * context-specific handler instance.
 * </p>
 */
@ProviderType
public interface MediaFormatHandler {

  /**
   * When comparing the ratio's of different media formats for automatic rendition generation, use this
   * value as tolerance barrier when comparing ratios of different media formats.
   */
  double RATIO_TOLERANCE = 0.05d;

  /**
   * Resolves media format name to media format object.
   * @param mediaFormatName Media format name
   * @return Media format or null if no match found
   */
  @Nullable
  MediaFormat getMediaFormat(@NotNull String mediaFormatName);

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @return Media formats sorted by combined title
   */
  @NotNull
  SortedSet<MediaFormat> getMediaFormats();

  /**
   * Get media formats defined by a CMS application that is responsible for the given media library path.
   * @param comparator Comparator for set
   * @return Media formats
   */
  @NotNull
  SortedSet<MediaFormat> getMediaFormats(@NotNull Comparator<MediaFormat> comparator);

  /**
   * Get list of media formats that have the same (or bigger) resolution as the requested media format
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Requested media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  @NotNull
  SortedSet<MediaFormat> getSameBiggerMediaFormats(@NotNull MediaFormat mediaFormatRequested, boolean filterRenditionGroup);

  /**
   * Get list of possible media formats that can be rendered from the given media format, i.e. same size or smaller
   * and (nearly) the same aspect ratio.
   * @param mediaFormatRequested Available media format
   * @param filterRenditionGroup Only check media formats of the same rendition group.
   * @return Matching media formats, sorted by size (biggest first), ranking, name
   */
  @NotNull
  SortedSet<MediaFormat> getSameSmallerMediaFormats(@NotNull MediaFormat mediaFormatRequested, boolean filterRenditionGroup);

  /**
   * Detect matching media format.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Media format or null if no matching media format found
   */
  @Nullable
  MediaFormat detectMediaFormat(@Nullable String extension, long fileSize, long width, long height);
  /**
   * Detect all matching media formats.
   * @param extension File extension
   * @param fileSize File size
   * @param width Image width (or 0 if not image)
   * @param height Image height (or 0 if not image)
   * @return Matching media formats sorted by their ranking or an empty list if no matching format was found
   */
  @NotNull
  SortedSet<MediaFormat> detectMediaFormats(@Nullable String extension, long fileSize, long width, long height);

}
