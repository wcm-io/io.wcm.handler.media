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

import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.UriTemplate;
import io.wcm.handler.media.UriTemplateType;
import io.wcm.handler.mediasource.ngdm.impl.ImageQualityPercentage;
import io.wcm.handler.mediasource.ngdm.impl.MediaArgsDimension;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaImageDeliveryParams;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaUrlBuilder;

/**
 * {@link UriTemplate} implementation for Next Gen. Dynamic Media remote assets.
 */
final class NextGenDynamicMediaUriTemplate implements UriTemplate {

  private final UriTemplateType type;
  private final String uriTemplate;

  NextGenDynamicMediaUriTemplate(@NotNull NextGenDynamicMediaContext context,
      @NotNull UriTemplateType type) {
    this.type = type;

    NextGenDynamicMediaImageDeliveryParams params = new NextGenDynamicMediaImageDeliveryParams()
        .widthPlaceholder(MediaNameConstants.URI_TEMPLATE_PLACEHOLDER_WIDTH)
        .rotation(context.getMedia().getRotation())
        .quality(ImageQualityPercentage.getAsInteger(context.getDefaultMediaArgs(), context.getMediaHandlerConfig()));

    Dimension ratio = MediaArgsDimension.getRequestedRatioAsWidthHeight(context.getDefaultMediaArgs());
    if (ratio != null) {
      params.cropSmartRatio(ratio);
    }

    this.uriTemplate = new NextGenDynamicMediaUrlBuilder(context).build(params);
  }

  @Override
  public @NotNull String getUriTemplate() {
    return uriTemplate;
  }

  @Override
  public @NotNull UriTemplateType getType() {
    return type;
  }

  @Override
  public long getMaxWidth() {
    return 0; // unknown
  }

  @Override
  public long getMaxHeight() {
    return 0; // unknown
  }

}
