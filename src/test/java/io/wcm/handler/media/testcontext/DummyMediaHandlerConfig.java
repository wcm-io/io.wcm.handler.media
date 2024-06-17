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
package io.wcm.handler.media.testcontext;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.mediasource.dam.DamMediaSource;
import io.wcm.handler.mediasource.inline.InlineMediaSource;
import io.wcm.handler.mediasource.ngdm.NextGenDynamicMediaMediaSource;

/**
 * Dummy media configuration
 */
public class DummyMediaHandlerConfig extends MediaHandlerConfig {

  private static final List<Class<? extends MediaSource>> MEDIA_SOURCES = List.of(
      NextGenDynamicMediaMediaSource.class,
      DamMediaSource.class,
      InlineMediaSource.class);

  private boolean enforceVirtualRenditions;

  @Override
  public @NotNull List<Class<? extends MediaSource>> getSources() {
    return MEDIA_SOURCES;
  }

  @Override
  public boolean includeAssetWebRenditionsByDefault() {
    // unit tests rely on old behavior
    return true;
  }

  @Override
  public boolean enforceVirtualRenditions() {
    return enforceVirtualRenditions;
  }

  public void setEnforceVirtualRenditions(boolean enforceVirtualRenditions) {
    this.enforceVirtualRenditions = enforceVirtualRenditions;
  }

}
