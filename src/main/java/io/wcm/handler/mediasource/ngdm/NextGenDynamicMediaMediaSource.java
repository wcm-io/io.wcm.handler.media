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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.adobe.cq.ui.wcm.commons.config.NextGenDynamicMediaConfig;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;

/**
 * Handles remote asset referenced via Next Generation Dynamic Media.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public class NextGenDynamicMediaMediaSource extends MediaSource {

  /**
   * Media source ID
   */
  public static final @NotNull String ID = "nextGenDynamicMedia";

  @Self
  private Adaptable adaptable;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
  private NextGenDynamicMediaConfig nextGenDynamicMediaConfig;

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public boolean accepts(@Nullable String mediaRef) {
    return isNextGenDynamicMediaEnabled() && NextGenDynamicMediaReference.isReference(mediaRef);
  }

  private boolean isNextGenDynamicMediaEnabled() {
    return nextGenDynamicMediaConfig != null && nextGenDynamicMediaConfig.enabled();
  }

  @Override
  public @Nullable String getPrimaryMediaRefProperty() {
    return mediaHandlerConfig.getMediaRefProperty();
  }

  @Override
  public @NotNull Media resolveMedia(@NotNull Media media) {
    String mediaRef = getMediaRef(media.getMediaRequest(), mediaHandlerConfig);
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();

    // check reference and enabled status
    NextGenDynamicMediaReference ref = NextGenDynamicMediaReference.fromReference(mediaRef);
    if (ref == null || !isNextGenDynamicMediaEnabled()) {
      if (StringUtils.isEmpty(mediaRef)) {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_MISSING);
      }
      else {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
      }
      return media;
    }

    // TODO: get asset metadata
    // TODO resolve media
    // TODO: check smart cropping params
    // TODO: set result/invalid reason

    return media;
  }

  @Override
  public void enableMediaDrop(@NotNull HtmlElement element, @NotNull MediaRequest mediaRequest) {
    // not supported
  }

}
