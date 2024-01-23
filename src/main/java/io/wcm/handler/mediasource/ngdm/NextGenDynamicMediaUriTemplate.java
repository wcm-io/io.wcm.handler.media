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
package io.wcm.handler.mediasource.ngdm;

import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;

/**
 * {@link UriTemplate} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaUriTemplate implements UriTemplate {

  @Override
  public @NotNull String getUriTemplate() {
    // TODO: implement uri template support
    return null;
  }

  @Override
  public @NotNull UriTemplateType getType() {
    // TODO: implement uri template support
    return null;
  }

  @Override
  public long getMaxWidth() {
    // TODO: implement uri template support
    return 0;
  }

  @Override
  public long getMaxHeight() {
    // TODO: implement uri template support
    return 0;
  }

}
