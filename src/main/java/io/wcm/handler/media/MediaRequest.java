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

import java.util.HashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.wcm.commons.util.AemObjectReflectionToStringBuilder;

/**
 * Holds all properties that are part of a media handling request.
 */
@ProviderType
public final class MediaRequest {

  private final Resource resource;
  private final String mediaRef;
  private final MediaArgs mediaArgs;
  private final MediaPropertyNames mediaPropertyNames;

  private ValueMap resourceProperties;

  /**
   * Create media request from resource.
   * @param resource Resource containing reference to media asset
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(@NotNull Resource resource, @Nullable MediaArgs mediaArgs) {
    this(resource, null, mediaArgs, null);
  }

  /**
   * Create media request from media reference.
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   */
  public MediaRequest(@Nullable String mediaRef, @Nullable MediaArgs mediaArgs) {
    this(null, mediaRef, mediaArgs, null);
  }

  /**
   * Create media request with all parameters.
   * @param resource Resource containing reference to media asset
   * @param mediaRef Reference to media item
   * @param mediaArgs Additional arguments affection media resolving
   * @param mediaPropertyNames Defines property names to read media parameters from for this media request.
   */
  public MediaRequest(@Nullable Resource resource, @Nullable String mediaRef, @Nullable MediaArgs mediaArgs,
      @Nullable MediaPropertyNames mediaPropertyNames) {
    this.resource = resource;
    this.mediaRef = mediaRef;
    this.mediaArgs = mediaArgs != null ? mediaArgs : new MediaArgs();
    this.mediaPropertyNames = mediaPropertyNames != null ? mediaPropertyNames : new MediaPropertyNames();
  }

  /**
   * Get resource containing media reference.
   * @return Resource containing reference to media asset
   */
  public @Nullable Resource getResource() {
    return this.resource;
  }

  /**
   * Get media reference.
   * @return Reference to media item
   */
  public @Nullable String getMediaRef() {
    return this.mediaRef;
  }

  /**
   * Get media arguments.
   * @return Additional arguments affection media resolving
   */
  public @NotNull MediaArgs getMediaArgs() {
    return this.mediaArgs;
  }

  /**
   * Get custom property names for media parameters.
   * @return Defines property names to read media parameters from for this media request.
   */
  public @NotNull MediaPropertyNames getMediaPropertyNames() {
    return this.mediaPropertyNames;
  }

  /**
   * Get properties from resource.
   * @return Properties from resource containing target link. The value map is a copy
   *         of the original map so it is safe to change the property values contained in the map.
   */
  public @NotNull ValueMap getResourceProperties() {
    if (this.resourceProperties == null) {
      // create a copy of the original map
      this.resourceProperties = new ValueMapDecorator(new HashMap<>());
      if (this.resource != null) {
        this.resourceProperties.putAll(resource.getValueMap());
      }
    }
    return this.resourceProperties;
  }

  @Override
  public String toString() {
    ToStringBuilder sb = new ToStringBuilder(this,
        io.wcm.wcm.commons.util.ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
    if (resource != null) {
      sb.append("resource", resource.getPath());
      sb.append("resourceProperties", AemObjectReflectionToStringBuilder.filteredValueMap(resource.getValueMap()));
    }
    if (mediaRef != null) {
      sb.append("mediaRef", mediaRef);
    }
    if (mediaArgs != null) {
      sb.append("mediaArgs", mediaArgs);
    }
    return sb.build();
  }


  /**
   * Defines property names to read media parameters from for this media request.
   */
  public static final class MediaPropertyNames {

    private String refProperty;
    private String cropProperty;
    private String rotationProperty;
    private String mapProperty;

    /**
     * Set media reference property name.
     * @param name Name of the property from which the media reference is read
     * @return this
     */
    public MediaPropertyNames refProperty(@Nullable String name) {
      this.refProperty = name;
      return this;
    }

    /**
     * Get media reference property name.
     * @return Name of the property from which the media reference is read
     */
    public @Nullable String getRefProperty() {
      return this.refProperty;
    }

    /**
     * Set cropping property name.
     * @param name Name of the property which contains the cropping parameters
     * @return this
     */
    public MediaPropertyNames cropProperty(@Nullable String name) {
      this.cropProperty = name;
      return this;
    }

    /**
     * Get cropping property name.
     * @return Name of the property which contains the cropping parameters
     */
    public @Nullable String getCropProperty() {
      return this.cropProperty;
    }

    /**
     * Set rotation property name.
     * @param name Name of the property which contains the rotation parameter
     * @return this
     */
    public MediaPropertyNames rotationProperty(@Nullable String name) {
      this.rotationProperty = name;
      return this;
    }

    /**
     * Get rotation property name.
     * @return Name of the property which contains the rotation parameter
     */
    public @Nullable String getRotationProperty() {
      return this.rotationProperty;
    }

    /**
     * Set image map property name.
     * @param name Name of the property which contains the image map data
     * @return this
     */
    public MediaPropertyNames mapProperty(@Nullable String name) {
      this.mapProperty = name;
      return this;
    }

    /**
     * Get image map property name.
     * @return Name of the property which contains the image map data
     */
    public @Nullable String getMapProperty() {
      return this.mapProperty;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, io.wcm.wcm.commons.util.ToStringStyle.SHORT_PREFIX_OMIT_NULL_STYLE);
    }

  }

}
