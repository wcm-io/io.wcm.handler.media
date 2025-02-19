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
package io.wcm.handler.media.spi;

import static io.wcm.handler.media.MediaNameConstants.MEDIAFORMAT_PROP_PARENT_MEDIA_FORMAT;
import static io.wcm.handler.media.impl.ImageTransformation.isValidRotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Asset;
import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.Rendition;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.imagemap.ImageMapArea;
import io.wcm.handler.media.imagemap.ImageMapParser;

/**
 * Via {@link MediaSource} OSGi services applications can define additional media sources supported by
 * {@link MediaHandler}.
 *
 * <p>
 * This class has to be extended by a Sling Model class. The adaptables
 * should be {@link org.apache.sling.api.SlingHttpServletRequest} and {@link org.apache.sling.api.resource.Resource}.
 * </p>
 */
@ConsumerType
public abstract class MediaSource {

  /**
   * @return Media source ID
   */
  public abstract @NotNull String getId();

  /**
   * @return Name of the property in which the primary media request is stored
   */
  public abstract @Nullable String getPrimaryMediaRefProperty();

  private static final Logger log = LoggerFactory.getLogger(MediaSource.class);

  /**
   * Checks whether a media request can be handled by this media source
   * @param mediaRequest Media request
   * @return true if this media source can handle the given media request
   */
  public boolean accepts(@NotNull MediaRequest mediaRequest) {
    // if an explicit media request is set check this first
    if (StringUtils.isNotEmpty(mediaRequest.getMediaRef())) {
      return accepts(mediaRequest.getMediaRef());
    }
    // otherwise check resource which contains media request properties
    ValueMap props = mediaRequest.getResourceProperties();
    // check for matching media source ID in media resource
    String mediaSourceId = props.get(MediaNameConstants.PN_MEDIA_SOURCE, String.class);
    if (StringUtils.isNotEmpty(mediaSourceId)) {
      return StringUtils.equals(mediaSourceId, getId());
    }
    // if no media source ID is set at all check if media ref attribute contains a valid reference
    else {
      String refProperty = StringUtils.defaultString(mediaRequest.getMediaPropertyNames().getRefProperty(),
          getPrimaryMediaRefProperty());
      String mediaRef = props.get(refProperty, String.class);
      return accepts(mediaRef);
    }
  }

  /**
   * Checks whether a media request string can be handled by this media source
   * @param mediaRef Media request string
   * @return true if this media source can handle the given media request
   */
  public abstract boolean accepts(@Nullable String mediaRef);

  /**
   * Resolves a media request
   * @param media Media metadata
   * @return Resolved media metadata. Never null.
   */
  public abstract @NotNull Media resolveMedia(@NotNull Media media);

  /**
   * Create a drop area for given HTML element to enable drag and drop of DAM assets
   * from content finder to this element.
   * @param element Html element
   * @param mediaRequest Media request to detect media args and property names
   */
  public abstract void enableMediaDrop(@NotNull HtmlElement element, @NotNull MediaRequest mediaRequest);

  /**
   * Sets list of cropping ratios to a list matching the selected media formats.
   * @param element Html element
   * @param mediaRequest Media request to detect media args and property names
   */
  public void setCustomIPECropRatios(@NotNull HtmlElement element, @NotNull MediaRequest mediaRequest) {
    // can be implemented by subclasses
  }

  /**
   * Get media request path to media library
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config (can be null, but should not be null)
   * @return Path or null if not present
   */
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected final @Nullable String getMediaRef(@NotNull MediaRequest mediaRequest,
      @Nullable MediaHandlerConfig mediaHandlerConfig) {
    if (StringUtils.isNotEmpty(mediaRequest.getMediaRef())) {
      return mediaRequest.getMediaRef();
    }
    else if (mediaRequest.getResource() != null) {
      String refProperty = getMediaRefProperty(mediaRequest, mediaHandlerConfig);
      return mediaRequest.getResource().getValueMap().get(refProperty, String.class);
    }
    else {
      return null;
    }
  }

  /**
   * Get property name containing the media request path
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config (can be null, but should not be null)
   * @return Property name
   */
  @SuppressWarnings("null")
  protected final @NotNull String getMediaRefProperty(@NotNull MediaRequest mediaRequest,
      @Nullable MediaHandlerConfig mediaHandlerConfig) {
    String refProperty = mediaRequest.getMediaPropertyNames().getRefProperty();
    if (StringUtils.isEmpty(refProperty)) {
      if (mediaHandlerConfig != null) {
        refProperty = mediaHandlerConfig.getMediaRefProperty();
      }
      else {
        refProperty = MediaNameConstants.PN_MEDIA_REF;
      }
    }
    return refProperty;
  }

  /**
   * Get (optional) crop dimensions from resource
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config (can be null, but should not be null)
   * @return Crop dimension or null if not set or invalid
   */
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected final @Nullable CropDimension getMediaCropDimension(@NotNull MediaRequest mediaRequest,
      @Nullable MediaHandlerConfig mediaHandlerConfig) {
    if (mediaRequest.getResource() != null) {
      String cropProperty = getMediaCropProperty(mediaRequest, mediaHandlerConfig);
      String cropString = mediaRequest.getResource().getValueMap().get(cropProperty, String.class);
      if (StringUtils.isNotEmpty(cropString)) {
        try {
          return CropDimension.fromCropString(cropString);
        }
        catch (IllegalArgumentException ex) {
          // ignore
        }
      }
    }
    return null;
  }

  /**
   * Get property name containing the cropping parameters
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config (can be null, but should not be null)
   * @return Property name
   */
  @SuppressWarnings("null")
  protected final @NotNull String getMediaCropProperty(@NotNull MediaRequest mediaRequest,
      @Nullable MediaHandlerConfig mediaHandlerConfig) {
    String cropProperty = mediaRequest.getMediaPropertyNames().getCropProperty();
    if (StringUtils.isEmpty(cropProperty)) {
      if (mediaHandlerConfig != null) {
        cropProperty = mediaHandlerConfig.getMediaCropProperty();
      }
      else {
        cropProperty = MediaNameConstants.PN_MEDIA_CROP;
      }
    }
    return cropProperty;
  }

  /**
   * Get (optional) rotation from resource
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config
   * @return Rotation value or null if not set or invalid
   */
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected final @Nullable Integer getMediaRotation(@NotNull MediaRequest mediaRequest,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    if (mediaRequest.getResource() != null) {
      String rotationProperty = getMediaRotationProperty(mediaRequest, mediaHandlerConfig);
      String stringValue = mediaRequest.getResource().getValueMap().get(rotationProperty, String.class);
      if (StringUtils.isNotEmpty(stringValue)) {
        int rotationValue = NumberUtils.toInt(stringValue);
        if (isValidRotation(rotationValue)) {
          return rotationValue;
        }
      }
    }
    return null;
  }

  /**
   * Get property name containing the rotation parameter
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config
   * @return Property name
   */
  @SuppressWarnings("null")
  protected final @NotNull String getMediaRotationProperty(@NotNull MediaRequest mediaRequest,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    String rotationProperty = mediaRequest.getMediaPropertyNames().getRotationProperty();
    if (StringUtils.isEmpty(rotationProperty)) {
      rotationProperty = mediaHandlerConfig.getMediaRotationProperty();
    }
    return rotationProperty;
  }

  /**
   * Get (optional) image map areas from resource
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config
   * @return Rotation value or null if not set or invalid
   */
  @SuppressWarnings({ "null", "PMD.ReturnEmptyCollectionRatherThanNull" })
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  protected final @Nullable List<ImageMapArea> getMediaMap(@NotNull MediaRequest mediaRequest,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    if (mediaRequest.getResource() != null) {
      String mapProperty = getMediaMapProperty(mediaRequest, mediaHandlerConfig);
      String stringValue = mediaRequest.getResource().getValueMap().get(mapProperty, String.class);
      if (StringUtils.isNotEmpty(stringValue)) {
        ImageMapParser imageMapParser = mediaRequest.getResource().adaptTo(ImageMapParser.class);
        if (imageMapParser != null) {
          return imageMapParser.parseMap(stringValue);
        }
      }
    }
    return null;
  }

  /**
   * Get property name containing the image map parameter
   * @param mediaRequest Media request
   * @param mediaHandlerConfig Media handler config
   * @return Property name
   */
  @SuppressWarnings("null")
  protected final @NotNull String getMediaMapProperty(@NotNull MediaRequest mediaRequest,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {
    String mapProperty = mediaRequest.getMediaPropertyNames().getMapProperty();
    if (StringUtils.isEmpty(mapProperty)) {
      mapProperty = mediaHandlerConfig.getMediaMapProperty();
    }
    return mapProperty;
  }

  /**
   * Updates media args settings that have default default values with values defined in the current
   * resource that defines the media reference (e.g. alt. text settings).
   * @param mediaArgs Media args
   * @param resource Resource with media reference
   * @param mediaHandlerConfig Media handler config
   */
  protected final void updateMediaArgsFromResource(@NotNull MediaArgs mediaArgs, @NotNull Resource resource,
      @NotNull MediaHandlerConfig mediaHandlerConfig) {

    // Get alt. text-relevant properties from current resource - if not set in media args
    ValueMap props = resource.getValueMap();
    if (StringUtils.isEmpty(mediaArgs.getAltText())) {
      mediaArgs.altText(props.get(mediaHandlerConfig.getMediaAltTextProperty(), String.class));
    }
    if (!mediaArgs.isDecorative()) {
      mediaArgs.decorative(props.get(mediaHandlerConfig.getMediaIsDecorativeProperty(), false));
    }
    if (mediaArgs.isForceAltValueFromAsset()) {
      mediaArgs.forceAltValueFromAsset(props.get(mediaHandlerConfig.getMediaForceAltTextFromAssetProperty(), true));
    }
  }

  /**
   * Resolves single rendition (or multiple renditions if any of the {@link MediaFormatOption#isMandatory()} is set to
   * true and sets the resolved rendition and the URL of the first (best-matching) rendition in the media object.
   * @param media Media object
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if all requested mandatory renditions could be resolved (at least one or all if none was set to
   *         mandatory)
   */
  protected final boolean resolveRenditions(Media media, Asset asset, MediaArgs mediaArgs) {
    boolean anyMandatory = mediaArgs.getMediaFormatOptions() != null
        && Arrays.stream(mediaArgs.getMediaFormatOptions())
        .anyMatch(MediaFormatOption::isMandatory);
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();
    if (mediaFormats != null && mediaFormats.length > 1
        && (anyMandatory || mediaArgs.getImageSizes() != null || mediaArgs.getPictureSources() != null)) {
      return resolveAllRenditions(media, asset, mediaArgs);
    }
    else {
      return resolveFirstMatchRenditions(media, asset, mediaArgs);
    }
  }

  /**
   * Check if a matching rendition is found for any of the provided media formats and other media args.
   * The first match is returned.
   * @param media Media
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if a rendition was found
   */
  private boolean resolveFirstMatchRenditions(Media media, Asset asset, MediaArgs mediaArgs) {
    Rendition rendition = asset.getRendition(mediaArgs);
    boolean renditionFound = false;
    if (rendition != null) {
      media.setRenditions(List.of(rendition));
      media.setUrl(rendition.getUrl());
      renditionFound = true;
    }
    log.trace("ResolveFirstMatchRenditions: renditionFound={}, rendition={}", renditionFound, rendition);
    return renditionFound;
  }

  /**
   * Iterates over all defined media format and tries to find a matching rendition for each of them
   * in combination with the other media args.
   * @param media Media
   * @param asset Asset
   * @param mediaArgs Media args
   * @return true if for all mandatory or for at least one media formats a rendition could be found.
   */
  private boolean resolveAllRenditions(@NotNull Media media, @NotNull Asset asset, @NotNull final MediaArgs mediaArgs) {
    boolean anyResolved = false;
    boolean allMandatoryResolved;
    final List<Rendition> resolvedRenditions = new ArrayList<>();

    // resolve main media formats (ignore responsive child formats)
    List<MediaFormatOption> parentMediaFormatOptions = getParentMediaFormats(mediaArgs);
    allMandatoryResolved = resolveRenditionsWithMediaFormats(asset, mediaArgs, parentMediaFormatOptions, resolvedRenditions);

    if (allMandatoryResolved) {
      final List<MediaFormat> resolvedMediaFormats;

      if (!resolvedRenditions.isEmpty()) {
        resolvedMediaFormats = resolvedRenditions.stream()
            .map(Rendition::getMediaFormat)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }
      else {
        // parent formats didn't match any rendition, but they are all optional.
        // try to resolve their child formats
        resolvedMediaFormats = parentMediaFormatOptions.stream()
            .map(MediaFormatOption::getMediaFormat)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
      }

      for (MediaFormat mediaFormat : resolvedMediaFormats) {
        List<MediaFormatOption> childMediaFormatOptions = getChildMediaFormats(mediaArgs, mediaFormat);
        allMandatoryResolved = resolveRenditionsWithMediaFormats(asset, mediaArgs, childMediaFormatOptions, resolvedRenditions) && allMandatoryResolved;
      }
    }

    media.setRenditions(resolvedRenditions);
    if (!resolvedRenditions.isEmpty()) {
      anyResolved = true;
      media.setUrl(resolvedRenditions.get(0).getUrl());
    }

    log.trace("ResolveAllRenditions: anyResolved={}, allMandatoryResolved={}, resolvedRenditions={}", anyResolved, allMandatoryResolved, resolvedRenditions);
    return anyResolved && allMandatoryResolved;
  }

  private boolean resolveRenditionsWithMediaFormats(@NotNull Asset asset, @NotNull MediaArgs mediaArgs,
      @NotNull List<MediaFormatOption> mediaFormatOptions, @NotNull List<Rendition> resolvedRenditions) {

    // collect "fallback" renditions that do not fully fulfill the media request (e.g. ignored explicit cropping)
    // separately and add them last in the returned list
    List<Rendition> fallbackRenditions = new ArrayList<>();

    boolean allMandatoryResolved = true;
    for (MediaFormatOption mediaFormatOption : mediaFormatOptions) {
      MediaArgs renditionMediaArgs = mediaArgs.clone();
      renditionMediaArgs.mediaFormat(mediaFormatOption.getMediaFormat());
      Rendition rendition = asset.getRendition(renditionMediaArgs);
      if (rendition != null) {
        if (rendition.isFallback()) {
          fallbackRenditions.add(rendition);
        }
        else {
          resolvedRenditions.add(rendition);
        }
      }
      else if (mediaFormatOption.isMandatory()) {
        allMandatoryResolved = false;
      }
    }
    resolvedRenditions.addAll(fallbackRenditions);
    return allMandatoryResolved;
  }

  @NotNull
  private List<MediaFormatOption> getParentMediaFormats(@NotNull MediaArgs mediaArgs) {
    return Arrays.stream(mediaArgs.getMediaFormatOptions())
        .filter(this::isParentMediaFormat)
        .collect(Collectors.toList());
  }

  @NotNull
  private List<MediaFormatOption> getChildMediaFormats(@NotNull MediaArgs mediaArgs, @NotNull final MediaFormat parentMediaFormat) {
    return Arrays.stream(mediaArgs.getMediaFormatOptions())
        .filter(this::isChildMediaFormat)
        .filter(childMediaFormat -> hasParent(childMediaFormat, parentMediaFormat))
        .collect(Collectors.toList());
  }

  private boolean isParentMediaFormat(@NotNull MediaFormatOption mediaFormatOption) {
    return Objects.isNull(getParentMediaFormat(mediaFormatOption.getMediaFormat()));
  }

  private boolean isChildMediaFormat(@NotNull MediaFormatOption mediaFormatOption) {
    return Objects.nonNull(getParentMediaFormat(mediaFormatOption.getMediaFormat()));
  }

  private boolean hasParent(@NotNull MediaFormatOption childMediaFormat, @NotNull MediaFormat parentMediaFormat) {
    return parentMediaFormat.equals(getParentMediaFormat(childMediaFormat.getMediaFormat()));
  }

  @Nullable
  private MediaFormat getParentMediaFormat(@Nullable MediaFormat mediaFormat) {
    if (mediaFormat == null) {
      return null;
    }
    return mediaFormat.getProperties().get(MEDIAFORMAT_PROP_PARENT_MEDIA_FORMAT, MediaFormat.class);
  }

}
