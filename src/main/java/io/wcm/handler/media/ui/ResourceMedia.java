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
package io.wcm.handler.media.ui;

import static io.wcm.handler.media.MediaNameConstants.PROP_CSS_CLASS;
import static io.wcm.handler.media.impl.WidthUtils.parseWidths;
import static io.wcm.handler.media.impl.WidthUtils.hasDensityDescriptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.MediaBuilder;
import io.wcm.handler.media.MediaHandler;
import io.wcm.handler.media.format.MediaFormatHandler;

/**
 * Generic resource-based media model.
 *
 * <p>
 * Optional use parameters when referencing model from Sightly template:
 * </p>
 * <ul>
 * <li><code>mediaFormat</code>: Media format name to restrict the allowed media items</li>
 * <li><code>refProperty</code>: Name of the property from which the media reference path is read, or node name for
 * inline media.</li>
 * <li><code>cropProperty</code>: Name of the property which contains the cropping parameters</li>
 * <li><code>rotationProperty</code>: Name of the property which contains the rotation parameter</li>
 * <li><code>cssClass</code>: CSS classes to be applied on the generated media element (most cases img element)</li>
 * <li><code>autoCrop</code>: Sets the auto cropping behavior of the media handler. This will override the component
 * property
 * {@value io.wcm.handler.media.MediaNameConstants#PN_COMPONENT_MEDIA_AUTOCROP}.</li>
 * <li><code>imageWidths</code>: Responsive rendition widths for image. Example:
 * "{@literal 2560?,1920,?1280,640,320}".<br>
 * Appending the suffix "{@literal ?}" makes the width optional, e.g. "1440?"<br>
 * Pixel density descriptors can be added with a colon separator, e.g. "1440:2x?". Default density is 1x.<br>
 * Use always 'imageWidths' together with 'imageSizes' property unless you add pixel density descriptors.<br>
 * Cannot be used together with the picture source parameters.</li>
 * <li><code>imageSizes</code>: "Sizes" string for img element. Example:
 * "{@literal (min-width: 400px) 400px, 100vw}".<br>
 * Cannot be used together with the picture source parameters.</li>
 * <li><code>pictureSourceMediaFormat</code>: List of media formats for the picture source elements. Example:
 * "{@literal ['mf_16_9','mf_4_3']}"<br>
 * You have to define the same number of array items in all pictureSource* properties.<br>
 * Cannot be used together with image sizes.</li>
 * <li><code>pictureSourceMedia</code>: List of media expressions for the picture source elements.
 * Example: "{@literal ['(max-width: 799px)', '(min-width: 800px)']}"<br>
 * You have to define the same number of array items in all pictureSource* properties.<br>
 * Cannot be used together with image sizes.</li>
 * <li><code>pictureSourceWidths</code>: List of widths for the picture source elements.
 * Example: "{@literal ['479,719,959,1279,1439?,1440?','640,1200:2x?']}".<br>
 * Appending the suffix "{@literal ?}" makes the width optional, e.g. "1440?"<br>
 * Pixel density descriptors can be added with a colon separator, e.g. "1440:2x?". Default density is 1x.<br>
 * You have to define the same number of array items in all pictureSource* properties.<br>
 * Cannot be used together with image sizes.</li>
 * <li><code>property:&lt;propertyname&gt;</code>: Custom properties for MediaArgs can be added with the name prefix
 * {@value #RA_PROPERTY_PREFIX},
 * e.g. {@literal "property:myprop1"="value1"} which adds property {@literal "myprop1"} to the MediaArgs. Custom
 * properties with null value will be ignored.</li>
 * </ul>
 */
@Model(adaptables = SlingHttpServletRequest.class)
public class ResourceMedia {

  /**
   * Name prefix for request attributes that will be put into the media builder properties
   */
  private static final String RA_PROPERTY_PREFIX = "property:";

  /**
   * Regex pattern that matches request attribute names with the prefix {@value #RA_PROPERTY_PREFIX}
   */
  private static final Pattern PROPERTY_NAME_PATTERN = Pattern.compile("^" + RA_PROPERTY_PREFIX + ".+$");

  /**
   * Optional: Media format to be used.
   * By default, the media formats are read from the component properties of the component and this
   * parameter should not be set. But for components that allow to choose one from the allowed media
   * formats via their edit dialog the format can be set here.
   * To be used together with 'imageSizes' and 'widths'.<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String mediaFormat;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String refProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String cropProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String rotationProperty;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String cssClass;

  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Boolean autoCrop;

  /**
   * Defines responsive rendition widths for image.
   * To be used together with 'imageSizes' property.
   * Example: "{@literal 2560?,1920,?1280,640,320}" <br>
   * Example with density descriptor: "{@literal 100,200:1.5x,200:2x?}"<br>
   * Widths are by default required. To declare an optional width append the "{@literal ?}" suffix, eg. "1440?"<br>
   * Density descriptor is separated by a colon e.g. "1440:2x?". You can eliminate 1x descriptor. Density descriptors
   * are considered when at least one width has a density descriptor and image sizes is not set.<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String imageWidths;

  /**
   * "Sizes" string for img element.
   * Example: "{@literal (min-width: 400px) 400px, 100vw}"<br>
   * Cannot be used together with the picture source parameters.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private String imageSizes;

  /**
   * List of media formats for the picture source elements.
   * Example: "{@literal ['mf_16_9']}"<br>
   * You have to define the same number of array items in all pictureSource* properties.<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceMediaFormat;

  /**
   * List of media expressions for the picture source elements.
   * Example: "{@literal ['(max-width: 799px)', '(min-width: 800px)']}"<br>
   * You have to define the same number of array items in all pictureSource* properties.<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceMedia;

  /**
   * List of widths for the picture source elements.
   * Example: "{@literal 479,719,959,1279,1439?,1440?}"<br>
   * Example with density descriptor: "{@literal 100,200:1.5x,200:2x?}"<br>
   * You have to define the same number of array items in all pictureSource* properties.
   * Widths are by default required. To declare an optional width append the "{@literal ?}" suffix, eg. "1440?"<br>
   * Density descriptor is separated by a colon e.g. "1440:2x?". You can eliminate 1x descriptor. Density descriptors
   * are considered when at least one width has a density descriptor and image sizes is not set.<br>
   * Cannot be used together with image sizes.
   */
  @RequestAttribute(injectionStrategy = InjectionStrategy.OPTIONAL)
  private Object[] pictureSourceWidths;

  @Self
  private MediaHandler mediaHandler;
  @Self
  private MediaFormatHandler mediaFormatHandler;
  @SlingObject
  private Resource resource;
  @Self
  private SlingHttpServletRequest request;

  private Media media;

  @PostConstruct
  @SuppressWarnings("null")
  private void activate() {
    MediaBuilder builder = mediaHandler.get(resource);

    if (StringUtils.isNotEmpty(mediaFormat)) {
      builder.mediaFormatName(mediaFormat);
    }
    if (StringUtils.isNotEmpty(refProperty)) {
      builder.refProperty(refProperty);
    }
    if (StringUtils.isNotEmpty(cropProperty)) {
      builder.cropProperty(cropProperty);
    }
    if (StringUtils.isNotEmpty(rotationProperty)) {
      builder.rotationProperty(rotationProperty);
    }
    if (autoCrop != null) {
      builder.autoCrop(autoCrop);
    }
    if (StringUtils.isNotEmpty(cssClass)) {
      builder.property(PROP_CSS_CLASS, cssClass);
    }

    // apply responsive image handling - either via image sizes or picture sources
    // image sizes is applied when sizes is configured ot if image widths contain density descriptors (separated by ":")
    if (StringUtils.isNotEmpty(imageSizes) || hasDensityDescriptor(imageWidths)) {
      WidthOption[] widthOptionsArray = parseWidths(imageWidths);
      if (widthOptionsArray != null) {
        builder.imageSizes(StringUtils.defaultString(imageSizes), widthOptionsArray);
      }
    }
    else if (pictureSourceMediaFormat != null && pictureSourceMedia != null && pictureSourceWidths != null) {
      ImageUtils.applyPictureSources(mediaFormatHandler, builder,
          toStringArray(pictureSourceMediaFormat),
          toStringArray(pictureSourceMedia),
          toStringArray(pictureSourceWidths));
    }

    setCustomProperties(builder);

    media = builder.build();
  }

  /**
   * Puts all request attributes that their name starts with the prefix {@value #RA_PROPERTY_PREFIX} into the properties of the media builder
   * @param builder Media builder
   */
  private void setCustomProperties(MediaBuilder builder) {
    getCustomPropertiesFromRequestAttributes()
        .forEach(builder::property);
  }

  /**
   * Gathers all request attributes whose name begins with the prefix {@value #RA_PROPERTY_PREFIX}, strips the prefix to get the property name
   * and returns a map of property name to request attribute value
   * @return map of custom properties
   */
  @NotNull
  private Map<String, Object> getCustomPropertiesFromRequestAttributes() {
    return enumToList(request.getAttributeNames()).stream()
        .filter(this::isMediaPropAttribute)
        .filter(this::attributeValueIsNotNull)
        .collect(Collectors.toMap(this::toPropertyName, request::getAttribute));
  }

  @NotNull
  private List<String> enumToList(@Nullable Enumeration<?> enumeration) {
    List<String> list = new ArrayList<>();

    if (enumeration != null) {
      while (enumeration.hasMoreElements()) {
        list.add(String.valueOf(enumeration.nextElement()));
      }
    }

    return list;
  }

  private boolean isMediaPropAttribute(@NotNull String requestAttributeName) {
    return PROPERTY_NAME_PATTERN.matcher(requestAttributeName).matches();
  }

  private boolean attributeValueIsNotNull(String attributeName) {
    return Objects.nonNull(request.getAttribute(attributeName));
  }

  @NotNull
  private String toPropertyName(@NotNull String requestAttributeName) {
    return StringUtils.substringAfter(requestAttributeName, RA_PROPERTY_PREFIX);
  }

  /**
   * For some reason passing in arrays from HTL works only with Object[], not with String[].
   * Thus, convert it here to String[].
   *
   * @param objectArray Array of objects
   * @return Array with objects converted to strings
   */
  private static String[] toStringArray(Object... objectArray) {
    return Arrays.stream(objectArray)
        .map(obj -> obj == null ? "" : obj.toString())
        .toArray(String[]::new);
  }

  /**
   * Returns a {@link Media} object with the metadata of the resolved media.
   * Result is never null, check for validness with the {@link Media#isValid()} method.
   * @return Media
   */
  public @NotNull Media getMetadata() {
    return media;
  }

  /**
   * Returns true if the media was resolved successful.
   * @return Media is valid
   */
  public boolean isValid() {
    return media.isValid();
  }

  /**
   * Returns the XHTML markup for the resolved media object (if valid).
   * This is in most cases an img element, but may also contain other arbitrary markup.
   * @return Media markup
   */
  public @Nullable String getMarkup() {
    return media.getMarkup();
  }

}
