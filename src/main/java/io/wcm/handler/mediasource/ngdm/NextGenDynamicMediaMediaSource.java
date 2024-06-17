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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.dam.api.Asset;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;

import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaInvalidReason;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaSource;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaConfigService;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaContext;
import io.wcm.handler.mediasource.ngdm.impl.NextGenDynamicMediaReference;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadata;
import io.wcm.handler.mediasource.ngdm.impl.metadata.NextGenDynamicMediaMetadataService;
import io.wcm.sling.models.annotations.AemObject;

/**
 * Handles remote asset referenced via Next Generation Dynamic Media.
 */
@Model(adaptables = {
    SlingHttpServletRequest.class, Resource.class
})
@ProviderType
public final class NextGenDynamicMediaMediaSource extends MediaSource {

  /**
   * Media source ID
   */
  public static final @NotNull String ID = "nextGenDynamicMedia";

  @Self
  private Adaptable adaptable;
  @Self
  private MediaHandlerConfig mediaHandlerConfig;
  @SlingObject
  private ResourceResolver resourceResolver;
  @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
  private NextGenDynamicMediaConfigService nextGenDynamicMediaConfig;
  @OSGiService(injectionStrategy = InjectionStrategy.OPTIONAL)
  private NextGenDynamicMediaMetadataService metadataService;
  @OSGiService
  private MimeTypeService mimeTypeService;

  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private WCMMode wcmMode;
  @AemObject(injectionStrategy = InjectionStrategy.OPTIONAL)
  private ComponentContext componentContext;

  @Override
  public @NotNull String getId() {
    return ID;
  }

  @Override
  public boolean accepts(@Nullable String mediaRef) {
    if (nextGenDynamicMediaConfig == null) {
      return false;
    }
    return (nextGenDynamicMediaConfig.isEnabledRemoteAssets() && NextGenDynamicMediaReference.isReference(mediaRef))
        || (nextGenDynamicMediaConfig.isEnabledLocalAssets() && isDamAssetReference(mediaRef));
  }

  private boolean isDamAssetReference(@Nullable String mediaRef) {
    return StringUtils.startsWith(mediaRef, "/content/dam/");
  }

  @Override
  public @Nullable String getPrimaryMediaRefProperty() {
    return mediaHandlerConfig.getMediaRefProperty();
  }

  @Override
  public @NotNull Media resolveMedia(@NotNull Media media) {
    String mediaRef = getMediaRef(media.getMediaRequest(), mediaHandlerConfig);
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();

    // check reference and enabled status
    NextGenDynamicMediaReference reference = toNextGenDynamicMediaReference(mediaRef);
    if (reference == null || nextGenDynamicMediaConfig == null) {
      if (StringUtils.isEmpty(mediaRef)) {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_MISSING);
      }
      else {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
      }
      return media;
    }

    // If enabled: Fetch asset metadata to validate existence and get original dimensions
    NextGenDynamicMediaMetadata metadata = null;
    Asset localAsset = reference.getAsset();
    if (localAsset != null) {
      metadata = getMetadataFromAsset(localAsset);
    }
    else if (metadataService != null && metadataService.isEnabled()) {
      metadata = metadataService.fetchMetadata(reference);
      if (metadata == null) {
        media.setMediaInvalidReason(MediaInvalidReason.MEDIA_REFERENCE_INVALID);
        return media;
      }
    }

    // Update media args settings from resource (e.g. alt. text setings)
    Resource referencedResource = media.getMediaRequest().getResource();
    if (referencedResource != null) {
      updateMediaArgsFromResource(mediaArgs, referencedResource, mediaHandlerConfig);
    }

    NextGenDynamicMediaContext context = new NextGenDynamicMediaContext(reference, metadata, media, mediaArgs,
        nextGenDynamicMediaConfig, mediaHandlerConfig, mimeTypeService);
    NextGenDynamicMediaAsset asset = new NextGenDynamicMediaAsset(context);
    media.setAsset(asset);

    // resolve rendition
    boolean renditionsResolved = resolveRenditions(media, asset, mediaArgs);

    // set media invalid reason
    if (!renditionsResolved) {
      if (media.getRenditions().isEmpty()) {
        media.setMediaInvalidReason(MediaInvalidReason.NO_MATCHING_RENDITION);
      }
      else {
        media.setMediaInvalidReason(MediaInvalidReason.NOT_ENOUGH_MATCHING_RENDITIONS);
      }
    }

    return media;
  }

  private @Nullable NextGenDynamicMediaReference toNextGenDynamicMediaReference(@Nullable String mediaRef) {
    if (nextGenDynamicMediaConfig != null) {
      if (nextGenDynamicMediaConfig.isEnabledRemoteAssets() && NextGenDynamicMediaReference.isReference(mediaRef)) {
        return NextGenDynamicMediaReference.fromReference(mediaRef);
      }
      else if (nextGenDynamicMediaConfig.isEnabledLocalAssets() && isDamAssetReference(mediaRef)) {
        return NextGenDynamicMediaReference.fromDamAssetReference(mediaRef, resourceResolver);
      }
    }
    return null;
  }

  private @Nullable NextGenDynamicMediaMetadata getMetadataFromAsset(@NotNull Asset asset) {
    NextGenDynamicMediaMetadata metadata = NextGenDynamicMediaMetadata.fromAsset(asset);
    if (metadata.isValid()) {
      return metadata;
    }
    return null;
  }

  @Override
  public void enableMediaDrop(@NotNull HtmlElement element, @NotNull MediaRequest mediaRequest) {
    if (wcmMode == WCMMode.DISABLED || wcmMode == null) {
      return;
    }
    if (componentContext != null && componentContext.getEditContext() != null) {
      if (MediaMarkupBuilderUtil.canApplyDragDropSupport(mediaRequest, componentContext)) {
        // check for this class is hard-coded in smartcropaction.js from core components
        element.addCssClass("cq-dd-image");
      }
      EditConfig editConfig = componentContext.getEditContext().getEditConfig();
      if (editConfig != null) {
        // inline editing is not supported for NGDM asset references
        editConfig.setInplaceEditingConfig(null);
      }
    }
  }

}
