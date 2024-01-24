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
package io.wcm.handler.media;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Describes an URI template returned for a given asset.
 */
@ProviderType
public interface UriTemplate {

  /**
   * @return URI template string containing placeholders like <code>{width}</code> or <code>{height}</code>.
   */
  @NotNull
  String getUriTemplate();

  /**
   * @return URI template type
   */
  @NotNull
  UriTemplateType getType();

  /**
   * @return Maximum width that can be requested for the given asset. Returns 0 if no maximum width is defined.
   */
  long getMaxWidth();

  /**
   * @return Maximum height that can be requested for the given asset. Returns 0 if no maximum height is defined.
   */
  long getMaxHeight();

}
