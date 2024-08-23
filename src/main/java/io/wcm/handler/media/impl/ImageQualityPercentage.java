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
package io.wcm.handler.media.impl;

import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.spi.MediaHandlerConfig;

/**
 * Gets image quality for current media request, with fallback to default quality.
 */
public final class ImageQualityPercentage {

  private ImageQualityPercentage() {
    // static methods only
  }

  /**
   * Gets the image quality percentage value, or the default configured value from media handler config as fallback.
   * @param mediaArgs Media args
   * @param mediaHandlerConfig Media handler config
   * @return Image Quality in Percentage as double (0..1)
   */
  public static double get(@NotNull MediaArgs mediaArgs,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    Double quality = mediaArgs.getImageQualityPercentage();
    if (quality == null) {
      quality = mediaHandlerConfig.getDefaultImageQualityPercentage();
    }
    return quality;
  }

  /**
   * Gets the image quality percentage value as integer from media args, or the default configured value
   * from media handler config as fallback.
   * @param mediaArgs Media args
   * @param mediaHandlerConfig Media handler config
   * @return Image Quality in Percentage as integer (0..100)
   */
  public static int getAsInteger(@NotNull MediaArgs mediaArgs,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    double quality = get(mediaArgs, mediaHandlerConfig);
    return (int)Math.round(quality * 100d);
  }

}
