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
package io.wcm.handler.media.markup;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.commons.DiffInfo;
import com.day.cq.commons.DiffService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.ComponentContext;
import com.day.cq.wcm.api.components.EditConfig;
import com.day.cq.wcm.api.components.InplaceEditingConfig;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.wcm.handler.commons.dom.HtmlElement;
import io.wcm.handler.media.Dimension;
import io.wcm.handler.media.Media;
import io.wcm.handler.media.MediaArgs;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.MediaRequest;
import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.handler.media.spi.MediaMarkupBuilder;
import io.wcm.sling.commons.request.RequestParam;

/**
 * Helper methods for {@link MediaMarkupBuilder} implementations.
 */
@ProviderType
public final class MediaMarkupBuilderUtil {

  /**
   * List of OOTB IPE editor types for images.
   */
  public static final Set<String> DEFAULT_ALLOWED_IPE_EDITOR_TYPES = Set.of("image");

  private MediaMarkupBuilderUtil() {
    // static methods only
  }

  /**
   * Adds CSS classes that denote the changes to the media element when compared to a different version.
   * If no diff has been requested by the WCM UI, there won't be any changes to the element.
   * @param mediaElement Element to be decorated
   * @param resource Resource pointing to JCR node
   * @param refProperty Name of property for media library item reference. If null, default name is used.
   * @param request Servlet request
   * @param mediaHandlerConfig Media handler config (can be null, but should not be null)
   */
  @SuppressWarnings("null")
  @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
  public static void addDiffDecoration(@NotNull HtmlElement<?> mediaElement, @NotNull Resource resource,
      @NotNull String refProperty, @NotNull SlingHttpServletRequest request, @Nullable MediaHandlerConfig mediaHandlerConfig) {

    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    Page currentPage = pageManager.getContainingPage(request.getResource());
    Page resourcePage = pageManager.getContainingPage(resource);

    String versionLabel = RequestParam.get(request, DiffService.REQUEST_PARAM_DIFF_TO);
    // Only try to diff when the resource is contained within the current page as the version number requested always
    // refers to the version history of the current page. So chances a resource on another page doesn't have a matching
    // version, and even if it has, it's comparing apples and oranges
    if (StringUtils.isNotEmpty(versionLabel)
        && currentPage != null && currentPage.equals(resourcePage)) {
      Resource versionedResource = DiffInfo.getVersionedResource(resource, versionLabel);
      if (versionedResource != null) {
        ValueMap currentProperties = resource.getValueMap();
        ValueMap oldProperties = versionedResource.getValueMap();

        String currentMediaRef = currentProperties.get(refProperty, String.class);
        String oldMediaRef = oldProperties.get(refProperty, String.class);
        if (!StringUtils.equals(currentMediaRef, oldMediaRef)) {
          if (StringUtils.isEmpty(currentMediaRef)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_REMOVED);
          }
          else if (StringUtils.isEmpty(oldMediaRef)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_ADDED);
          }
          else {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_UPDATED);
          }
        }
        else {
          String cropProperty;
          if (mediaHandlerConfig != null) {
            cropProperty = mediaHandlerConfig.getMediaCropProperty();
          }
          else {
            cropProperty = MediaNameConstants.PN_MEDIA_CROP;
          }

          // If the mediaRef itself hasn't changed, check the cropping coordinates
          String currentMediaCrop = currentProperties.get(cropProperty, String.class);
          String oldMediaCrop = oldProperties.get(cropProperty, String.class);
          if (!StringUtils.equals(currentMediaCrop, oldMediaCrop)) {
            mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_UPDATED);
          }

          // we also could try to determine here whether it resolves to another rendition
          // or if the timestamp of the rendition has been updated (which would indicate the the binary payload has been
          // changed).
          // This however, is out of scope for this feature right now
        }
      }
      else {
        // The resource didn't exist in the old version at all
        mediaElement.addCssClass(MediaNameConstants.CSS_DIFF_ADDED);
      }
    }
  }

  /**
   * Get dimension from first media format defined in media args. Fall back to dummy min. dimension if none specified.
   * @param media Media metadata
   * @return Dimension
   */
  public static @NotNull Dimension getMediaformatDimension(@NotNull Media media) {
    // Create dummy image element to be displayed in Edit mode as placeholder.
    MediaArgs mediaArgs = media.getMediaRequest().getMediaArgs();
    MediaFormat[] mediaFormats = mediaArgs.getMediaFormats();

    // detect width/height - either from media args, or from first media format
    long width = mediaArgs.getFixedWidth();
    long height = mediaArgs.getFixedHeight();
    if ((width == 0 || height == 0) && mediaFormats != null && mediaFormats.length > 0) {
      MediaFormat firstMediaFormat = mediaFormats[0];
      Dimension dimension = firstMediaFormat.getMinDimension();
      if (dimension != null) {
        width = dimension.getWidth();
        height = dimension.getHeight();
      }
    }

    // fallback to min width/height
    if (width == 0) {
      width = MediaMarkupBuilder.DUMMY_MIN_DIMENSION;
    }
    if (height == 0) {
      height = MediaMarkupBuilder.DUMMY_MIN_DIMENSION;
    }

    return new Dimension(width, height);
  }

  /**
   * Implements check whether to apply drag&amp;drop support as described in {@link DragDropSupport}.
   * @param mediaRequest Media request
   * @param wcmComponentContext WCM component context
   * @return true if drag&amp;drop can be applied.
   */
  public static boolean canApplyDragDropSupport(@NotNull MediaRequest mediaRequest,
      @Nullable ComponentContext wcmComponentContext) {
    switch (mediaRequest.getMediaArgs().getDragDropSupport()) {
      case ALWAYS:
        return true;
      case NEVER:
        return false;
      case AUTO:
        String resourcePath = null;
        Resource mediaRequestResource = mediaRequest.getResource();
        if (mediaRequestResource != null) {
          resourcePath = mediaRequestResource.getPath();
        }
        String componentResourcePath = null;
        if (wcmComponentContext != null && wcmComponentContext.getResource() != null) {
          componentResourcePath = wcmComponentContext.getResource().getPath();
        }
        return resourcePath != null && StringUtils.equals(resourcePath, componentResourcePath);
      default:
        throw new IllegalArgumentException("Unsupported drag&drop support mode: "
            + mediaRequest.getMediaArgs().getDragDropSupport());
    }

  }

  /**
   * Implements check whether to set customized IPE cropping ratios as described in {@link IPERatioCustomize}.
   * @param mediaRequest Media request
   * @param wcmComponentContext WCM component context
   * @return true if customized IP cropping ratios can be set
   */
  public static boolean canSetCustomIPECropRatios(@NotNull MediaRequest mediaRequest,
      @Nullable ComponentContext wcmComponentContext) {
    return canSetCustomIPECropRatios(mediaRequest, wcmComponentContext, DEFAULT_ALLOWED_IPE_EDITOR_TYPES);
  }

  /**
   * Implements check whether to set customized IPE cropping ratios as described in {@link IPERatioCustomize}.
   * @param mediaRequest Media request
   * @param wcmComponentContext WCM component context
   * @param allowedIpeEditorTypes Allowed editor types for image IPE (in-place editor).
   * @return true if customized IP cropping ratios can be set
   */
  public static boolean canSetCustomIPECropRatios(@NotNull MediaRequest mediaRequest,
      @Nullable ComponentContext wcmComponentContext, @NotNull Set<String> allowedIpeEditorTypes) {

    EditConfig editConfig = null;
    InplaceEditingConfig ipeConfig = null;
    if (wcmComponentContext != null && wcmComponentContext.getEditContext() != null
        && wcmComponentContext.getEditContext().getEditConfig() != null
        && wcmComponentContext.getResource() != null) {
      editConfig = wcmComponentContext.getEditContext().getEditConfig();
      ipeConfig = editConfig.getInplaceEditingConfig();
    }
    if (editConfig == null || ipeConfig == null
        || !allowedIpeEditorTypes.contains(ipeConfig.getEditorType())) {
      // no image IPE activated - never customize crop ratios
      return false;
    }

    switch (mediaRequest.getMediaArgs().getIPERatioCustomize()) {
      case ALWAYS:
        return true;
      case NEVER:
        return false;
      case AUTO:
        if (StringUtils.isNotEmpty(ipeConfig.getConfigPath())) {
          String ratiosPath = ipeConfig.getConfigPath() + "/plugins/crop/aspectRatios";
          @SuppressWarnings("null")
          ResourceResolver resolver = wcmComponentContext.getResource().getResourceResolver();
          return resolver.getResource(ratiosPath) == null;
        }
        return true;
      default:
        throw new IllegalArgumentException("Unsupported IPE ratio customize mode: "
            + mediaRequest.getMediaArgs().getIPERatioCustomize());
    }

  }

}
