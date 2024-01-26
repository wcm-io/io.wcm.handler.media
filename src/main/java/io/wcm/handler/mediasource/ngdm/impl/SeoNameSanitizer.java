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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Sanitizes SEO names for usage in context of Next Gen. Dynamic Media
 */
public final class SeoNameSanitizer {

  static final Pattern SEO_NAME_FILTER_PATTERN = Pattern.compile("[\\W_]");

  private SeoNameSanitizer() {
    // static methods only
  }

  /**
   * Sanitizes the SEO name to avoid problems with special characters in URLs.
   * @param name Name
   * @return Sanitzed name
   */
  public static @NotNull String sanitizeSeoName(@NotNull String name) {
    Matcher matcher = SEO_NAME_FILTER_PATTERN.matcher(name);
    return StringUtils.toRootLowerCase(matcher.replaceAll("-"));
  }

}
