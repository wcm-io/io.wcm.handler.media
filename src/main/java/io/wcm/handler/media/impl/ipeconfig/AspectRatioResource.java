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
package io.wcm.handler.media.impl.ipeconfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.AbstractResource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.sling.commons.resource.ImmutableValueMap;

/**
 * Virtual resource returning name and ratio of media format.
 */
class AspectRatioResource extends AbstractResource {

  private static final String RESOURCE_TYPE = "wcm-io/handler/media/synthetic/resource/aspectRatio";

  private final ResourceResolver resolver;
  private final String path;
  private final ResourceMetadata resourceMetadata;
  private final ValueMap properties;

  AspectRatioResource(ResourceResolver resolver, MediaFormat mediaFormat, String path) {
    this.resolver = resolver;
    this.path = path;
    this.resourceMetadata = buildMetadata(path);

    double ratio = 0d;
    if (mediaFormat.getRatio() > 0d) {
      ratio = 1d / mediaFormat.getRatio();
    }

    this.properties = ImmutableValueMap.of(
        "name", getDisplayString(mediaFormat),
        "ratio", ratio);
  }

  private static String getDisplayString(MediaFormat mf) {
    if (StringUtils.contains(mf.getName(), ":")) {
      return mf.getName();
    }
    else {
      String ratioDisplayString = mf.getRatioDisplayString();
      if (ratioDisplayString != null) {
        return mf.getLabel() + " (" + mf.getRatioDisplayString() + ")";
      }
      else {
        return mf.getLabel();
      }
    }
  }

  private static ResourceMetadata buildMetadata(String path) {
    ResourceMetadata metadata = new ResourceMetadata();
    metadata.setResolutionPath(path);
    return metadata;
  }

  @Override
  public @NotNull String getPath() {
    return path;
  }

  @Override
  public @NotNull ResourceMetadata getResourceMetadata() {
    return resourceMetadata;
  }

  @Override
  public @NotNull ResourceResolver getResourceResolver() {
    return this.resolver;
  }

  @Override
  public @NotNull String getResourceType() {
    return RESOURCE_TYPE;
  }

  @Override
  public @Nullable String getResourceSuperType() {
    return null;
  }

  @Override
  @SuppressWarnings({ "unchecked", "null" })
  public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
    if (type == ValueMap.class) {
      return (AdapterType)properties;
    }
    return super.adaptTo(type);
  }

}
