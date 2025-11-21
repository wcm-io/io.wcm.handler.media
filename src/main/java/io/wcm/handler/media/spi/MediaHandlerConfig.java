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

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;

import io.wcm.handler.media.MediaFileType;
import io.wcm.handler.media.MediaNameConstants;
import io.wcm.handler.media.markup.DummyImageMediaMarkupBuilder;
import io.wcm.handler.media.markup.MediaMarkupBuilderUtil;
import io.wcm.handler.media.markup.SimpleImageMediaMarkupBuilder;
import io.wcm.handler.mediasource.dam.AemRenditionType;
import io.wcm.handler.mediasource.dam.DamMediaSource;
import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * {@link MediaHandlerConfig} OSGi services provide application-specific configuration for media handling.
 * Applications can set service properties or bundle headers as defined in {@link ContextAwareService} to apply this
 * configuration only for resources that match the relevant resource paths.
 */
@ConsumerType
public abstract class MediaHandlerConfig implements ContextAwareService {

  /**
   * Default image quality for images with lossy compressions (e.g. JPEG).
   */
  public static final double DEFAULT_IMAGE_QUALITY = 0.85d;

  /**
   * Default value for JPEG quality.
   * @deprecated Use {@link #DEFAULT_IMAGE_QUALITY} instead.
   */
  @Deprecated(since = "2.0.0")
  public static final double DEFAULT_JPEG_QUALITY = DEFAULT_IMAGE_QUALITY;

  private static final List<Class<? extends MediaSource>> DEFAULT_MEDIA_SOURCES = List.of(
      DamMediaSource.class);

  private static final List<Class<? extends MediaMarkupBuilder>> DEFAULT_MEDIA_MARKUP_BUILDERS = List.of(
      SimpleImageMediaMarkupBuilder.class,
      DummyImageMediaMarkupBuilder.class);

  /**
   * Get supported media sources.
   * @return Supported media sources
   */
  public @NotNull List<Class<? extends MediaSource>> getSources() {
    return DEFAULT_MEDIA_SOURCES;
  }

  /**
   * Get available media markup builders.
   * @return Available media markup builders
   */
  public @NotNull List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
    return DEFAULT_MEDIA_MARKUP_BUILDERS;
  }

  /**
   * Get media metadata pre processors.
   * @return List of media metadata pre processors (optional). The processors are applied in list order.
   */
  public @NotNull List<Class<? extends MediaProcessor>> getPreProcessors() {
    // no processors
    return Collections.emptyList();
  }

  /**
   * Get media metadata post processors.
   * @return List of media metadata post processors (optional). The processors are applied in list order.
   */
  public @NotNull List<Class<? extends MediaProcessor>> getPostProcessors() {
    // no processors
    return Collections.emptyList();
  }

  /**
   * Get the default quality for images.
   * The meaning of the quality parameter for the different image formats is described in
   * {@link com.day.image.Layer#write(String, double, java.io.OutputStream)}.
   * @param contentType MIME-type of the output format
   * @return Quality factor
   */
  public double getDefaultImageQuality(@Nullable String contentType) {
    MediaFileType mediaFileType = MediaFileType.getByContentType(contentType);
    if (mediaFileType != null && mediaFileType.isImageQualityPercentage()) {
      return getDefaultImageQualityPercentage();
    }
    else if (mediaFileType == MediaFileType.GIF) {
      return 256d; // 256 colors
    }
    // return quality "1" for all other mime types
    return 1d;
  }

  /**
   * Get the default quality for images.
   * This parameter only applies to images with lossy compression (e.g. JPEG).
   * @return Quality percentage (0..1)
   */
  public double getDefaultImageQualityPercentage() {
    return DEFAULT_IMAGE_QUALITY;
  }

  /**
   * With this switch it's possible to switch all used property and node names from (legacy) wcm.io
   * Handler standard to Adobe Standard (as used e.g. in Adobe Core WCM Components) - e.g.
   * using "fileReference" instead of property name "mediaRef" for the asset reference.
   *
   * <p>
   * The benefit of the wcm.io Handler standard was that it supported storage multiple asset references
   * in one single node - but this it not well supported by the Touch UI anyway, so it's not of much
   * use nowadays.
   * </p>
   *
   * <p>
   * For new projects it is recommended to always use the Adobe standard names. But for backward compatibility
   * the default values is false.
   * </p>
   * @return If true, Adobe standard property and node names are used.
   */
  public boolean useAdobeStandardNames() {
    return false;
  }

  /**
   * Get default media reference property name.
   * @return Default property name for reference to media library item
   */
  public @NotNull String getMediaRefProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_REF_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_REF;
    }
  }

  /**
   * Get default cropping property name.
   * @return Default property name for cropping parameters
   */
  public @NotNull String getMediaCropProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_CROP_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_CROP;
    }
  }

  /**
   * Get default rotation property name.
   * @return Default property name for rotate parameter
   */
  public @NotNull String getMediaRotationProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_ROTATION_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_ROTATION;
    }
  }

  /**
   * Get default image map property name.
   * @return Default property name for map parameter
   */
  public @NotNull String getMediaMapProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_MAP_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_MAP;
    }
  }

  /**
   * Get default alt text property name.
   * @return Default property name for media alt. text
   */
  public @NotNull String getMediaAltTextProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_ALTTEXT_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_ALTTEXT;
    }
  }

  /**
   * Get default force alt from asset property name.
   * @return Default property name for forcing reading alt. text from DAM asset description
   */
  public @NotNull String getMediaForceAltTextFromAssetProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_FORCE_ALTTEXT_FROM_ASSET_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_FORCE_ALTTEXT_FROM_ASSET;
    }
  }

  /**
   * Get default decorative image property name.
   * @return Default property name for marking image as "decorative" - requiring no alt. text
   */
  public @NotNull String getMediaIsDecorativeProperty() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.PN_MEDIA_IS_DECORATIVE_STANDARD;
    }
    else {
      return MediaNameConstants.PN_MEDIA_IS_DECORATIVE;
    }
  }

  /**
   * Get default inline media node name.
   * @return Default node name for inline media item stored in node within the content page
   */
  public @NotNull String getMediaInlineNodeName() {
    if (useAdobeStandardNames()) {
      return MediaNameConstants.NN_MEDIA_INLINE_STANDARD;
    }
    else {
      return MediaNameConstants.NN_MEDIA_INLINE;
    }
  }

  /**
   * @return If set to true, web renditions generated by AEM (with <code>cq5dam.web.</code> prefix) are
   *         taken into account by default when trying to resolve the media request.
   * @deprecated Use {@link #getIncludeAssetAemRenditionsByDefault()} instead.
   */
  @Deprecated(since = "2.0.0")
  public boolean includeAssetWebRenditionsByDefault() {
    return false;
  }

  /**
   * Set of renditions auto-generated by AEM (with <code>cq5dam.</code> prefix) which are taken into account
   * by default when trying to resolve the media request.
   * @return Set or rendition types
   */
  @SuppressWarnings("java:S1874") // ignore use of deprecated method
  public @NotNull Set<AemRenditionType> getIncludeAssetAemRenditionsByDefault() {
    if (includeAssetWebRenditionsByDefault()) {
      return EnumSet.of(AemRenditionType.WEB_RENDITION, AemRenditionType.VIDEO_RENDITION);
    }
    else {
      return EnumSet.of(AemRenditionType.VIDEO_RENDITION);
    }
  }

  /**
   * Enforce to generate only virtual renditions.
   *
   * <p>
   * By default, virtual renditions (rendered on-the-fly via <code>ImageFileServet</code>) are only
   * generated if there is a need to re-scale or crop or transform an image. Otherwise direct references
   * to renditions or original stored in DAM are returned when there is an direct match with the requested ratio and
   * resolution.
   * </p>
   *
   * <p>
   * When this flag is set to <code>true</code>, even if there is a direct match a virtual rendition is returned.
   * This ensures that the default quality setting e.g. for JPEG images is always respected, regardless
   * in which quality the original images was uploaded.
   * </p>
   * @return Enforce always returning virtual renditions for images.
   */
  public boolean enforceVirtualRenditions() {
    return false;
  }

  /**
   * @return Allowed editor types for image IPE (in-place editor).
   *         By default, only the OOTB "image" editor type is supported.
   */
  public @NotNull Set<String> allowedIpeEditorTypes() {
    return MediaMarkupBuilderUtil.DEFAULT_ALLOWED_IPE_EDITOR_TYPES;
  }

  /**
   * Get root path for picking assets using path field widgets.
   * @param page Context page
   * @return DAM root path
   */
  public @NotNull String getDamRootPath(@NotNull Page page) {
    return "/content/dam";
  }

}
