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

import org.apache.sling.commons.mime.MimeTypeService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;

/**
 * Relevant context objects for media resolution.
 */
public final class NextGenDynamicMediaContext {

  private final NextGenDynamicMediaReference reference;
  private final NextGenDynamicMediaMetadata metadata;
  private final Media media;
  private final MediaArgs defaultMediaArgs;
  private final NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;
  private final MediaHandlerConfig mediaHandlerConfig;
  private final MimeTypeService mimeTypeService;

  /**
   * @param reference NextGen Dynamic Media reference
   * @param metadata NextGen Dynamic Media metadata (if enabled)
   * @param defaultMediaArgs Default media args
   * @param nextGenDynamicMediaConfig NextGen Dynamic Media config
   * @param mediaHandlerConfig Media handler config
   * @param mimeTypeService Mime type service
   */
  public NextGenDynamicMediaContext(@NotNull NextGenDynamicMediaReference reference,
      @Nullable NextGenDynamicMediaMetadata metadata,
      @NotNull Media media,
      @NotNull MediaArgs defaultMediaArgs,
      @NotNull NextGenDynamicMediaConfigService nextGenDynamicMediaConfig,
      @NotNull MediaHandlerConfig mediaHandlerConfig,
      @NotNull MimeTypeService mimeTypeService) {
    this.reference = reference;
    this.metadata = metadata;
    this.media = media;
    this.defaultMediaArgs = defaultMediaArgs;
    this.nextGenDynamicMediaConfig = nextGenDynamicMediaConfig;
    this.mediaHandlerConfig = mediaHandlerConfig;
    this.mimeTypeService = mimeTypeService;
  }

  public @NotNull NextGenDynamicMediaReference getReference() {
    return this.reference;
  }

  public @Nullable NextGenDynamicMediaMetadata getMetadata() {
    return this.metadata;
  }

  public @NotNull Media getMedia() {
    return this.media;
  }

  public @NotNull MediaArgs getDefaultMediaArgs() {
    return this.defaultMediaArgs;
  }

  public @NotNull NextGenDynamicMediaConfigService getNextGenDynamicMediaConfig() {
    return this.nextGenDynamicMediaConfig;
  }

  public @NotNull MediaHandlerConfig getMediaHandlerConfig() {
    return this.mediaHandlerConfig;
  }

  public @NotNull MimeTypeService getMimeTypeService() {
    return this.mimeTypeService;
  }

}
