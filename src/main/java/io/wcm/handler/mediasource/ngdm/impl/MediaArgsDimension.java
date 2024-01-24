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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.Ratio;

/**
 * Gets requested dimension/ratio from media args (only first media format is inspected).
 */
public final class MediaArgsDimension {

  private MediaArgsDimension() {
    // static methods only
  }

  /**
   * Requested dimensions either from media format or fixed dimensions from media args.
   * @param mediaArgs Media args
   * @return Requested dimensions
   */
  public static @NotNull Dimension getRequestedDimension(@NotNull MediaArgs mediaArgs) {

    // check for fixed dimensions from media args
    if (mediaArgs.getFixedWidth() > 0 || mediaArgs.getFixedHeight() > 0) {
      return new Dimension(mediaArgs.getFixedWidth(), mediaArgs.getFixedHeight());
    }

    // check for dimensions from mediaformat (evaluate only first media format)
    MediaFormat mediaFormat = getFirstMediaFormat(mediaArgs);
    if (mediaFormat != null) {
      Dimension dimension = mediaFormat.getMinDimension();
      if (dimension != null) {
        return dimension;
      }
    }

    // fallback to 0/0 - no specific dimension requested
    return new Dimension(0, 0);
  }

  /**
   * Requested ratio either from media format or fixed dimensions from media args.
   * @param mediaArgs Media args
   * @return Requested ratio
   */
  public static double getRequestedRatio(@NotNull MediaArgs mediaArgs) {

    // check for fixed dimensions from media args
    if (mediaArgs.getFixedWidth() > 0 && mediaArgs.getFixedHeight() > 0) {
      return Ratio.get(mediaArgs.getFixedWidth(), mediaArgs.getFixedHeight());
    }

    // check for dimensions from mediaformat (evaluate only first media format)
    MediaFormat mediaFormat = getFirstMediaFormat(mediaArgs);
    if (mediaFormat != null && mediaFormat.getRatio() > 0) {
      return mediaFormat.getRatio();
    }

    // no ratio
    return 0d;
  }

  /**
   * Requested ratio either from media format or fixed dimensions from media args as width/height dimension
   * (as integers, not rounded but extrapolated).
   * @param mediaArgs Media args
   * @return Requested ratio or null if no ratio is requested
   */
  public static @Nullable Dimension getRequestedRatioAsWidthHeight(@NotNull MediaArgs mediaArgs) {

    // check for fixed dimensions from media args
    if (mediaArgs.getFixedWidth() > 0 && mediaArgs.getFixedHeight() > 0) {
      return new Dimension(mediaArgs.getFixedWidth(), mediaArgs.getFixedHeight());
    }

    // check for dimensions from mediaformat (evaluate only first media format)
    MediaFormat mediaFormat = getFirstMediaFormat(mediaArgs);
    if (mediaFormat != null && mediaFormat.getRatio() > 0) {
      double ratioWidth = mediaFormat.getRatioWidthAsDouble();
      double ratioHeight = mediaFormat.getRatioHeightAsDouble();
      if (!(ratioWidth > 0 && ratioHeight > 0)) {
        ratioWidth = mediaFormat.getRatio();
        ratioHeight = 1;
      }
      if (ratioWidth % 1 > 0 || ratioHeight % 1 > 0) {
        // extrapolate to integer values by multiplication with 100,000 so we have at least 5 digits after the comma
        ratioWidth *= 100000d;
        ratioHeight *= 100000d;
      }
      return new Dimension(Math.round(ratioWidth), Math.round(ratioHeight));
    }

    // no ratio
    return null;
  }

  /**
   * Gets first media format.
   * @param mediaArgs Media args
   * @return First media format or null
   */
  public static @Nullable MediaFormat getFirstMediaFormat(@NotNull MediaArgs mediaArgs) {
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    if (mediaFormats != null && mediaFormats.length > 0) {
      return mediaFormats[0];
    }
    return null;
  }

}
