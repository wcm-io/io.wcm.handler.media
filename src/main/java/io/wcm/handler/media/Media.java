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
package io.wcm.handler.media;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.commons.dom.Span;
import io.wcm.handler.media.imagemap.ImageMapArea;
import io.wcm.handler.media.spi.MediaSource;

/**
 * Holds information about a media request processed and resolved by {@link MediaHandler}.
 */
@ProviderType
@JsonInclude(Include.NON_EMPTY)
public final class Media {

  private final @NotNull MediaSource mediaSource;
  private @NotNull MediaRequest mediaRequest;
  private HtmlElement<?> element;
  private Function<Media, HtmlElement<?>> elementBuilder;
  private String url;
  private Asset asset;
  private Collection<Rendition> renditions;
  private CropDimension cropDimension;
  private Integer rotation;
  private List<ImageMapArea> map;
  private MediaInvalidReason mediaInvalidReason;
  private String mediaInvalidReasonCustomMessage;
  private String markup;

  /**
   * @param mediaSource Media source
   * @param mediaRequest Processed media request
   */
  public Media(@NotNull MediaSource mediaSource, @NotNull MediaRequest mediaRequest) {
    this.mediaSource = mediaSource;
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Media source
   */
  @JsonIgnore
  public @NotNull MediaSource getMediaSource() {
    return this.mediaSource;
  }

  /**
   * @return Media handling request
   */
  @JsonIgnore
  public @NotNull MediaRequest getMediaRequest() {
    return this.mediaRequest;
  }

  /**
   * @param mediaRequest Media handling request
   */
  public void setMediaRequest(@NotNull MediaRequest mediaRequest) {
    this.mediaRequest = mediaRequest;
  }

  /**
   * @return Html element
   */
  @JsonIgnore
  public HtmlElement<?> getElement() {
    if (this.element == null && this.elementBuilder != null) {
      this.element = this.elementBuilder.apply(this);
      this.elementBuilder = null;
    }
    return this.element;
  }

  /**
   * @return Media HTML element serialized to string. Returns null if media element is null.
   */
  @JsonIgnore
  public String getMarkup() {
    HtmlElement<?> el = getElement();
    if (markup == null && el != null) {
      if (el instanceof Span) {
        // in case of span get inner HTML markup, do not include span element itself
        StringBuilder result = new StringBuilder();
        for (Element child : el.getChildren()) {
          result.append(child.toString());
        }
        markup = result.toString();
      }
      else {
        markup = el.toString();
      }
    }
    return markup;
  }

  /**
   * @param value Html element
   * @deprecated Use {@link #setElementBuilder(Function)} to build anchor on-demand
   */
  @Deprecated
  public void setElement(HtmlElement<?> value) {
    this.element = value;
    this.markup = null;
  }

  /**
   * @param value Function that builds the HTML element representation on demand
   */
  public void setElementBuilder(Function<Media, HtmlElement<?>> value) {
    this.elementBuilder = value;
    this.markup = null;
  }

  /**
   * @return Media URL
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * @param value Media URL
   */
  public void setUrl(String value) {
    this.url = value;
  }

  /**
   * Get media item info that was resolved during media handler processing
   * @return Media item
   */
  public Asset getAsset() {
    return this.asset;
  }

  /**
   * Set media item that was resolved during media handler processing
   * @param asset Media item
   */
  public void setAsset(Asset asset) {
    this.asset = asset;
  }

  /**
   * Get first (and best-match) rendition that was resolved during media handler processing
   * @return Rendition
   */
  @JsonIgnore
  public Rendition getRendition() {
    if (this.renditions == null || this.renditions.isEmpty()) {
      return null;
    }
    return this.renditions.iterator().next();
  }

  /**
   * Get all renditions that were resolved during media handler processing
   * @return Renditions
   */
  public Collection<Rendition> getRenditions() {
    if (this.renditions == null) {
      return Collections.emptyList();
    }
    else {
      return this.renditions;
    }
  }

  /**
   * Set all renditions that was resolved during media handler processing
   * @param renditions Renditions
   */
  public void setRenditions(Collection<Rendition> renditions) {
    this.renditions = renditions;
  }

  /**
   * @return Crop dimensions (optional)
   */
  @JsonIgnore
  public @Nullable CropDimension getCropDimension() {
    return this.cropDimension;
  }

  /**
   * @param cropDimension Crop dimensions (optional)
   */
  public void setCropDimension(@Nullable CropDimension cropDimension) {
    this.cropDimension = cropDimension;
  }

  /**
   * @return Image rotation (optional)
   */
  @JsonIgnore
  public @Nullable Integer getRotation() {
    return this.rotation;
  }

  /**
   * @param rotation Image Rotation (optional)
   */
  public void setRotation(@Nullable Integer rotation) {
    this.rotation = rotation;
  }

  /**
   * @return Image map (optional)
   */
  @JsonIgnore
  public @Nullable List<ImageMapArea> getMap() {
    return this.map;
  }

  /**
   * @param map Image map (optional)
   */
  public void setMap(@Nullable List<ImageMapArea> map) {
    this.map = map;
  }

  /**
   * @return true if link is valid and was resolved successfully
   */
  public boolean isValid() {
    return (mediaInvalidReason == null);
  }

  /**
   * @return Reason why the requested media could not be resolved and is invalid
   */
  @JsonIgnore
  public MediaInvalidReason getMediaInvalidReason() {
    return this.mediaInvalidReason;
  }

  /**
   * @param mediaInvalidReason Reason why the requested media could not be resolved and is invalid
   */
  public void setMediaInvalidReason(MediaInvalidReason mediaInvalidReason) {
    this.mediaInvalidReason = mediaInvalidReason;
  }

  /**
   * @return Custom message when {@link #getMediaInvalidReason()} is set to {@link MediaInvalidReason#CUSTOM}.
   *         Message is interpreted as i18n key.
   */
  public String getMediaInvalidReasonCustomMessage() {
    return this.mediaInvalidReasonCustomMessage;
  }

  /**
   * @param mediaInvalidReasonCustomMessage Custom message when {@link #getMediaInvalidReason()} is set to
   *          {@link MediaInvalidReason#CUSTOM}. Message is interpreted as i18n key.
   */
  public void setMediaInvalidReasonCustomMessage(String mediaInvalidReasonCustomMessage) {
    this.mediaInvalidReasonCustomMessage = mediaInvalidReasonCustomMessage;
  }

  @Override
  public String toString() {
    ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
    if (isValid()) {
      sb.append("url", getUrl());
    }
    else {
      sb.append("mediaInvalidReason", this.mediaInvalidReason);
    }
    sb.append("mediaSource", mediaSource.getId());
    if (asset != null) {
      sb.append("asset", asset.getPath());
    }
    if (renditions != null) {
      sb.append("renditions", renditions);
    }
    if (cropDimension != null) {
      sb.append("cropDimension", cropDimension);
    }
    if (rotation != null) {
      sb.append("rotation", rotation);
    }
    if (map != null) {
      sb.append("map", map);
    }
    sb.append("mediaRequest", mediaRequest);
    return sb.build();
  }

}
