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
import org.apache.sling.models.annotations.Model;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaMarkupBuilder;

/**
 * Generates an iframe for Next Gen. Dynamic Media hosted player.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ConsumerType
public class NextGenDynamicMediaHostedPlayerMarkupBuilder implements MediaMarkupBuilder {

  @Override
  public boolean accepts(@NotNull Media media) {
    Rendition rendition = media.getRendition();
    if (!media.isValid() || rendition == null || !media.getMediaRequest().getMediaArgs().isHostedVideoPlayer()) {
      return false;
    }
    return rendition.isVideo();
  }

  @Override
  public HtmlElement build(@NotNull Media media) {
    Rendition rendition = media.getRendition();
    if (rendition == null) {
      return null;
    }
    Dimension dimension = MediaMarkupBuilderUtil.getMediaformatDimension(media);

    HtmlElement iframe = new HtmlElement("iframe");
    iframe.setAttribute("src", rendition.getUrl());
    iframe.setAttribute("width", String.valueOf(dimension.getWidth()));
    iframe.setAttribute("height", String.valueOf(dimension.getHeight()));
    iframe.setAttribute("allowfullscreen", "true");
    iframe.setAttribute("frameborder", "0");

    return iframe;
  }

  @Override
  public boolean isValidMedia(@NotNull HtmlElement element) {
    return "iframe".equalsIgnoreCase(element.getName());
  }

}
