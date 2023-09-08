/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2019 wcm.io
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

import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES;
import static io.wcm.handler.media.MediaNameConstants.NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_AUTOCROP;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES;
import static io.wcm.handler.media.MediaNameConstants.PN_COMPONENT_MEDIA_RESPONSIVE_TYPE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.media.MediaArgs.ImageSizes;
import io.wcm.handler.media.MediaArgs.MediaFormatOption;
import io.wcm.handler.media.MediaArgs.PictureSource;
import io.wcm.handler.media.MediaArgs.WidthOption;
import io.wcm.handler.media.impl.WidthUtils;
import io.wcm.wcm.commons.component.ComponentPropertyResolution;
import io.wcm.wcm.commons.component.ComponentPropertyResolver;
import io.wcm.wcm.commons.component.ComponentPropertyResolverFactory;

/**
 * Resolves Media Handler component properties for the component associated
 * with the given resource from content policies and properties defined in the component resource.
 * Please make sure to {@link #close()} instances of this class after usage.
 * <p>
 * Alternatively, it's possible to use the resolver on a ValueMap. In this case, the properties
 * are directly read from the provided value map. Picture Sources are not supported for that option.
 * </p>
 */
@ProviderType
@SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
public final class MediaComponentPropertyResolver implements AutoCloseable {

  static final String RESPONSIVE_TYPE_IMAGE_SIZES = "imageSizes";
  static final String RESPONSIVE_TYPE_PICTURE_SOURCES = "pictureSources";

  static final String PN_IMAGES_SIZES_SIZES = "sizes";
  static final String PN_IMAGES_SIZES_WIDTHS = "widths";

  static final String PN_PICTURE_SOURCES_MEDIAFORMAT = "mediaFormat";
  static final String PN_PICTURE_SOURCES_MEDIA = "media";
  static final String PN_PICTURE_SOURCES_SIZES = "sizes";
  static final String PN_PICTURE_SOURCES_WIDTHS = "widths";

  private final @Nullable ComponentPropertyResolver resolver;
  private final PropertyAccessor propertyAccessor;

  /**
   * Resolves
   * @param resource Resource
   * @param componentPropertyResolverFactory Component property resolver factory
   */
  public MediaComponentPropertyResolver(@NotNull Resource resource,
      @NotNull ComponentPropertyResolverFactory componentPropertyResolverFactory) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = componentPropertyResolverFactory.get(resource, true)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    propertyAccessor = new ComponentPropertyResolverPropertyAccessor(resolver);
  }

  /**
   * @param valueMap Value map to read properties directly from
   */
  public MediaComponentPropertyResolver(@NotNull ValueMap valueMap) {
    resolver = null;
    propertyAccessor = new ValueMapPropertyAccessor(valueMap);
  }

  /**
   * @param resource Resource
   * @deprecated Please use {@link #MediaComponentPropertyResolver(Resource, ComponentPropertyResolverFactory)}
   */
  @Deprecated
  @SuppressWarnings("resource")
  public MediaComponentPropertyResolver(@NotNull Resource resource) {
    // resolve media component properties 1. from policies and 2. from component definition
    resolver = new ComponentPropertyResolver(resource, true)
        .contentPolicyResolution(ComponentPropertyResolution.RESOLVE)
        .componentPropertiesResolution(ComponentPropertyResolution.RESOLVE_INHERIT);
    propertyAccessor = new ComponentPropertyResolverPropertyAccessor(resolver);
  }

  /**
   * @return AutoCrop state
   */
  public boolean isAutoCrop() {
    return propertyAccessor.get(PN_COMPONENT_MEDIA_AUTOCROP, false);
  }

  /**
   * @return List of media formats with and without mandatory setting.
   */
  public @NotNull MediaFormatOption @Nullable [] getMediaFormatOptions() {
    Map<String, MediaFormatOption> mediaFormatOptions = new LinkedHashMap<>();

    // media formats with optional mandatory boolean flag(s)
    String[] mediaFormatNames = propertyAccessor.get(PN_COMPONENT_MEDIA_FORMATS, String[].class);
    Boolean[] mediaFormatsMandatory = propertyAccessor.get(PN_COMPONENT_MEDIA_FORMATS_MANDATORY, Boolean[].class);
    if (mediaFormatNames != null) {
      for (int i = 0; i < mediaFormatNames.length; i++) {
        boolean mandatory = false;
        if (mediaFormatsMandatory != null) {
          if (mediaFormatsMandatory.length == 1) {
            // backward compatibility: support a single flag for all media formats
            mandatory = mediaFormatsMandatory[0];
          }
          else if (mediaFormatsMandatory.length > i) {
            mandatory = mediaFormatsMandatory[i];
          }
        }
        if (StringUtils.isNotBlank(mediaFormatNames[i])) {
          mediaFormatOptions.put(mediaFormatNames[i], new MediaFormatOption(mediaFormatNames[i], mandatory));
        }
      }
    }

    // support additional property with list of media format names that are all rated as mandatory
    String[] mediaFormatsMandatoryNames = propertyAccessor.get(PN_COMPONENT_MEDIA_FORMATS_MANDATORY_NAMES, String[].class);
    if (mediaFormatsMandatoryNames != null) {
      for (String mediaFormatName : mediaFormatsMandatoryNames) {
        if (StringUtils.isNotBlank(mediaFormatName)) {
          mediaFormatOptions.put(mediaFormatName, new MediaFormatOption(mediaFormatName, true));
        }
      }
    }

    if (mediaFormatOptions.isEmpty()) {
      return null;
    }
    else {
      return mediaFormatOptions.values().stream().toArray(size -> new MediaFormatOption[size]);
    }
  }

  /**
   * @return List of media formats with and without mandatory setting.
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @NotNull String @Nullable [] getMediaFormatNames() {
    MediaFormatOption[] mediaFormatOptions = getMediaFormatOptions();
    if (mediaFormatOptions != null) {
      String[] result = Arrays.stream(mediaFormatOptions)
          .map(MediaFormatOption::getMediaFormatName)
          .filter(Objects::nonNull)
          .toArray(size -> new String[size]);
      if (result.length > 0) {
        return result;
      }
    }
    return null;
  }

  /**
   * @return List of media formats with and without mandatory setting.
   */
  @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
  public @NotNull String @Nullable [] getMandatoryMediaFormatNames() {
    MediaFormatOption[] mediaFormatOptions = getMediaFormatOptions();
    if (mediaFormatOptions != null) {
      String[] result = Arrays.stream(mediaFormatOptions)
          .filter(MediaFormatOption::isMandatory)
          .map(MediaFormatOption::getMediaFormatName)
          .filter(Objects::nonNull)
          .toArray(size -> new String[size]);
      if (result.length > 0) {
        return result;
      }
    }
    return null;
  }

  /**
   * @return Image sizes
   */
  @SuppressWarnings({ "deprecation", "null" })
  public @Nullable ImageSizes getImageSizes() {
    String responsiveType = getResponsiveType();
    if (responsiveType != null && !StringUtils.equals(responsiveType, RESPONSIVE_TYPE_IMAGE_SIZES)) {
      return null;
    }

    String sizes = StringUtils.trimToNull(propertyAccessor.get(NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES + "/" + PN_IMAGES_SIZES_SIZES, String.class));
    WidthOption[] widths = WidthUtils.parseWidths(propertyAccessor.get(NN_COMPONENT_MEDIA_RESPONSIVEIMAGE_SIZES + "/" + PN_IMAGES_SIZES_WIDTHS, String.class));
    if (sizes != null && widths != null) {
      return new ImageSizes(sizes, widths);
    }

    // try to fallback to deprecated constant with node names with typo (backward compatibility)
    sizes = StringUtils.trimToNull(propertyAccessor.get(NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES + "/" + PN_IMAGES_SIZES_SIZES, String.class));
    widths = WidthUtils.parseWidths(propertyAccessor.get(NN_COMPONENT_MEDIA_RESPONSIVE_IMAGE_SIZES + "/" + PN_IMAGES_SIZES_WIDTHS, String.class));
    if (sizes != null && widths != null) {
      return new ImageSizes(sizes, widths);
    }

    return null;
  }

  /**
   * @return List of picture sources
   */
  @SuppressWarnings({ "deprecation", "null" })
  public @NotNull PictureSource @Nullable [] getPictureSources() {
    String responsiveType = getResponsiveType();
    if (resolver == null || responsiveType != null && !StringUtils.equals(responsiveType, RESPONSIVE_TYPE_PICTURE_SOURCES)) {
      return null;
    }

    Collection<Resource> sourceResources = resolver.getResources(NN_COMPONENT_MEDIA_RESPONSIVEPICTURE_SOURCES);
    if (sourceResources == null) {
      // try to fallback to deprecated constant with node names with typo (backward compatibility)
      sourceResources = resolver.getResources(NN_COMPONENT_MEDIA_RESPONSIVE_PICTURE_SOURCES);
      if (sourceResources == null) {
        return null;
      }
    }

    List<PictureSource> sources = new ArrayList<>();
    for (Resource sourceResource : sourceResources) {
      ValueMap props = sourceResource.getValueMap();
      String mediaFormatName = StringUtils.trimToNull(props.get(PN_PICTURE_SOURCES_MEDIAFORMAT, String.class));
      String media = StringUtils.trimToNull(props.get(PN_PICTURE_SOURCES_MEDIA, String.class));
      String sizes = StringUtils.trimToNull(props.get(PN_PICTURE_SOURCES_SIZES, String.class));
      WidthOption[] widths = WidthUtils.parseWidths(props.get(PN_PICTURE_SOURCES_WIDTHS, String.class));
      if (mediaFormatName != null && widths != null) {
        sources.add(new PictureSource(mediaFormatName)
            .media(media)
            .sizes(sizes)
            .widthOptions(widths));
      }
    }

    if (sources.isEmpty()) {
      return null;
    }
    else {
      return sources.stream().toArray(size -> new PictureSource[size]);
    }
  }

  private String getResponsiveType() {
    return propertyAccessor.get(PN_COMPONENT_MEDIA_RESPONSIVE_TYPE, String.class);
  }

  @Override
  @SuppressWarnings("null")
  public void close() {
    if (resolver != null) {
      resolver.close();
    }
  }

  private interface PropertyAccessor {
    @Nullable
    <T> T get(@NotNull String name, @NotNull Class<T> type);

    <T> T get(@NotNull String name, @NotNull T defaultValue);
  }

  private static class ComponentPropertyResolverPropertyAccessor implements PropertyAccessor {
    private final ComponentPropertyResolver componentPropertyResolver;
    ComponentPropertyResolverPropertyAccessor(ComponentPropertyResolver componentPropertyResolver) {
      this.componentPropertyResolver = componentPropertyResolver;
    }
    @Override
    public <T> @Nullable T get(@NotNull String name, @NotNull Class<T> type) {
      return componentPropertyResolver.get(name, type);
    }
    @Override
    public <T> T get(@NotNull String name, @NotNull T defaultValue) {
      return componentPropertyResolver.get(name, defaultValue);
    }
  }

  private static class ValueMapPropertyAccessor implements PropertyAccessor {
    private final ValueMap valueMap;
    ValueMapPropertyAccessor(ValueMap valueMap) {
      this.valueMap = valueMap;
    }
    @Override
    public <T> @Nullable T get(@NotNull String name, @NotNull Class<T> type) {
      return valueMap.get(name, type);
    }
    @Override
    public <T> T get(@NotNull String name, @NotNull T defaultValue) {
      return valueMap.get(name, defaultValue);
    }
  }

}
