/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2026 wcm.io
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
package io.wcm.handler.mediasource.ngdm.markup;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Video;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaMarkupBuilder;

/**
 * Generates an HTML5 video element for Next Gen. Dynamic Media video renditions.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class NextGenDynamicMediaVideoMarkupBuilder implements MediaMarkupBuilder {

  @Override
  public boolean accepts(@NotNull Media media) {
    if (!media.isValid() || media.getRendition() == null || media.getMediaRequest().getMediaArgs().isHostedVideoPlayer()) {
      return false;
    }
    return media.getRendition().isVideo();
  }

  @Override
  public HtmlElement build(@NotNull Media media) {
    Rendition rendition = media.getRendition();
    if (rendition == null) {
      return null;
    }
    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);

    Video video = new Video();
    video.setWidth(dimension.getWidth());
    video.setHeight(dimension.getHeight());
    video.setControls(true);

    ValueMap properties = rendition.getProperties();
    String posterUrl = properties.get("posterUrl", String.class);
    if (posterUrl != null) {
      video.setPoster(posterUrl);
    }

    // add source
    video.createSource()
        .setType(rendition.getMimeType())
        .setSrc(rendition.getUrl());

    return video;
  }

  @Override
  public boolean isValidMedia(@NotNull HtmlElement element) {
    return element instanceof Video;
  }

}
