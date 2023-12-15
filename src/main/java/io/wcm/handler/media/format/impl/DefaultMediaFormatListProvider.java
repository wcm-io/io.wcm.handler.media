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
package io.wcm.handler.media.format.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.format.MediaFormatHandler;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Exports the list of media format available for the addressed media path in JSON format to the response.
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + FileExtension.JSON,
    "sling.servlet.selectors=wcmio_handler_media_mediaformat_list",
    "sling.servlet.resourceTypes=sling/servlet/default",
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class DefaultMediaFormatListProvider extends SlingSafeMethodsServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
    // get list of media formats for current medialib path
    Set<MediaFormat> mediaFormats = getMediaFormats(request);

    List<MediaFormatItem> mediaFormatList = new ArrayList<>();
    if (mediaFormats != null) {
      for (MediaFormat mediaFormat : mediaFormats) {
        if (!mediaFormat.isInternal()) {
          MediaFormatItem mediaFormatItem = new MediaFormatItem();
          mediaFormatItem.name = mediaFormat.getName();
          mediaFormatItem.text = mediaFormat.toString();
          mediaFormatItem.width = mediaFormat.getWidth();
          mediaFormatItem.height = mediaFormat.getHeight();
          mediaFormatItem.widthMin = mediaFormat.getMinWidth();
          mediaFormatItem.heightMin = mediaFormat.getMinHeight();
          mediaFormatItem.widthHeightMin = mediaFormat.getMinWidthHeight();
          mediaFormatItem.isImage = mediaFormat.isImage();
          mediaFormatItem.ratio = mediaFormat.getRatio();
          mediaFormatItem.ratioWidth = mediaFormat.getRatioWidthAsDouble();
          mediaFormatItem.ratioHeight = mediaFormat.getRatioHeightAsDouble();
          mediaFormatItem.ratioDisplayString = mediaFormat.getRatioDisplayString();
          mediaFormatList.add(mediaFormatItem);
        }
      }
    }

    // serialize to JSON using Jackson
    response.setContentType(ContentType.JSON);
    response.getWriter().write(new ObjectMapper().writeValueAsString(mediaFormatList));
  }

  protected Set<MediaFormat> getMediaFormats(SlingHttpServletRequest request) {
    MediaFormatHandler mediaFormatHandler = request.adaptTo(MediaFormatHandler.class);
    if (mediaFormatHandler != null) {
      return mediaFormatHandler.getMediaFormats();
    }
    else {
      return Collections.emptySet();
    }
  }

  @JsonInclude(Include.NON_NULL)
  static class MediaFormatItem {
    private String name;
    private String text;
    private long width;
    private long height;
    private long widthMin;
    private long heightMin;
    private long widthHeightMin;
    private boolean isImage;
    private double ratio;
    private double ratioWidth;
    private double ratioHeight;
    private String ratioDisplayString;

    public String getName() {
      return this.name;
    }

    public String getText() {
      return this.text;
    }

    public long getWidth() {
      return this.width;
    }

    public long getHeight() {
      return this.height;
    }

    public long getWidthMin() {
      return this.widthMin;
    }

    public long getHeightMin() {
      return this.heightMin;
    }

    public long getWidthHeightMin() {
      return this.widthHeightMin;
    }

    @JsonProperty("isImage")
    public boolean isImage() {
      return this.isImage;
    }

    public double getRatio() {
      return this.ratio;
    }

    public double getRatioWidth() {
      return this.ratioWidth;
    }

    public double getRatioHeight() {
      return this.ratioHeight;
    }

    public String getRatioDisplayString() {
      return this.ratioDisplayString;
    }

  }

}
