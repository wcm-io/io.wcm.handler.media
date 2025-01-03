/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.MediaArgs.WidthOption;

/**
 * Helper methods for parsing strings with responsive widths (which can be optional).
 */
public final class WidthUtils {

  // example values:
  // 800        <- width=800px, mandatory
  // 800?       <- width=800px, optional
  // 800:1.5x   <- width=800px, density 1.5x, mandatory
  // 800:1.5x?  <- width=800px, density 1.5x, optional
  static final String WIDTH_OPTION = "\\d+(:\\d+(\\.\\d+)?x)?\\??";

  // comma-separated width options; tolerates whitespaces between options.
  // example values:
  // 800,1024,2048
  // 800,1024?,2048?   <- last two are optional
  static final Pattern WIDTHS_PATTERN = Pattern.compile("^\\s*" + WIDTH_OPTION + "\\s*(,\\s*" + WIDTH_OPTION + "\\s*)*+$");

  private WidthUtils() {
    // static methods only
  }

  /**
   * Parse widths string.
   * @param widths Widths string
   * @return Width options
   */
  public static @Nullable WidthOption @Nullable [] parseWidths(@Nullable String widths) {
    if (StringUtils.isBlank(widths)) {
      return null;
    }
    if (!WIDTHS_PATTERN.matcher(widths).matches()) {
      return null;
    }
    String[] widthItems = StringUtils.split(widths, ",");
    return Arrays.stream(widthItems)
        .map(StringUtils::trim)
        .map(WidthUtils::toWidthOption)
        .toArray(WidthOption[]::new);
  }

  private static @NotNull WidthOption toWidthOption(@NotNull String widthOptionString) {
    boolean optional = StringUtils.endsWith(widthOptionString, "?");
    String[] tokens = StringUtils.substringBefore(widthOptionString, "?").split(":");
    long width = NumberUtils.toLong(tokens[0]);
    String density = tokens.length > 1 ? tokens[1] : null;
    return new WidthOption(width, density, !optional);
  }

}
