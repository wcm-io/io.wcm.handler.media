/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2021 wcm.io
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
package io.wcm.handler.mediasource.dam.impl.dynamicmedia;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.api.s7dam.utils.PublishUtils;

import io.wcm.handler.media.Dimension;
import io.wcm.handler.url.SiteConfig;
import io.wcm.handler.url.UrlHandler;
import io.wcm.handler.url.UrlMode;
import io.wcm.handler.url.UrlModes;
import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Implements {@link DynamicMediaSupportService}.
 */
@Component(service = DynamicMediaSupportService.class, immediate = true)
@Designate(ocd = DynamicMediaSupportServiceImpl.Config.class)
public class DynamicMediaSupportServiceImpl implements DynamicMediaSupportService {

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Dynamic Media Support",
      description = "Configures dynamic media support in media handling.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "Enable support for dynamic media. "
            + "Only gets active when dynamic media is actually enabled for the instance.")
    boolean enabled() default true;

    @AttributeDefinition(
        name = "Dynamic Media Capability",
        description = "Whether to detect automatically if Dynamic Media is actually for a given asset by looking for existing DM metadata. "
            + "Setting to ON disables the auto-detection and forces it to enabled for all asssets, setting to OFF forced it to disabled.")
    DynamicMediaCapabilityDetection dmCapabilityDetection() default DynamicMediaCapabilityDetection.AUTO;

    @AttributeDefinition(
        name = "Author Preview Mode",
        description = "Loads dynamic media images via author instance - to allow previewing unpublished images. "
            + "Must not be enabled on publish instances.")
    boolean authorPreviewMode() default false;

    @AttributeDefinition(
        name = "Disable AEM Fallback",
        description = "Disable the automatic fallback to AEM-based rendering of renditions (via Media Handler) "
            + "if Dynamic Media is enabled, but the asset has not the appropriate Dynamic Media metadata.")
    boolean disableAemFallback() default false;

    @AttributeDefinition(
        name = "Validate Smart Crop Rendition Sizes",
        description = "Validates that the renditions defined via smart cropping fulfill the requested image width/height to avoid upscaling or white borders.")
    boolean validateSmartCropRenditionSizes() default true;

    @AttributeDefinition(
        name = "Image width limit",
        description = "The configured width value for 'Reply Image Size Limit'.")
    long imageSizeLimitWidth() default 2000;

    @AttributeDefinition(
        name = "Image height limit",
        description = "The configured height value for 'Reply Image Size Limit'.")
    long imageSizeLimitHeight() default 2000;

    @AttributeDefinition(
        name = "Set Image Quality",
        description = "Control image quality for lossy output formats for each media request via 'qlt' URL parameter (instead of relying on default setting within Dynamic Media).")
    boolean setImageQuality() default true;

    @AttributeDefinition(
        name = "Default Format",
        description = "Default response image format. "
            + "If empty, the default setting that is configured on the Dynamic Media server environment is used. "
            + "Accepts the same values as the 'fmt' parameter from the Dynamic Media Image Service API.")
    String defaultFmt() default "";

    @AttributeDefinition(
        name = "Default Format Alpha Channel",
        description = "Default response image format for source images that may have an alpha channel (e.g. for PNG). "
            + "Accepts the same values as the 'fmt' parameter from the Dynamic Media Image Service API.")
    String defaultFmtAlpha() default "webp-alpha";

  }

  @Reference
  private PublishUtils dynamicMediaPublishUtils;
  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  private boolean enabled;
  private DynamicMediaCapabilityDetection dmCapabilityDetection;
  private boolean authorPreviewMode;
  private boolean disableAemFallback;
  private boolean validateSmartCropRenditionSizes;
  private Dimension imageSizeLimit;
  private boolean setImageQuality;
  private String defaultFmt;
  private String defaultFmtAlpha;

  private static final String SERVICEUSER_SUBSERVICE = "dynamic-media-support";
  private static final Pattern DAM_PATH_PATTERN = Pattern.compile("^/content/dam(/.*)?$");

  private static final Logger log = LoggerFactory.getLogger(DynamicMediaSupportServiceImpl.class);

  @Activate
  private void activate(Config config) {
    this.enabled = config.enabled();
    this.dmCapabilityDetection = config.dmCapabilityDetection();
    this.authorPreviewMode = config.authorPreviewMode();
    this.disableAemFallback = config.disableAemFallback();
    this.validateSmartCropRenditionSizes = config.validateSmartCropRenditionSizes();
    this.imageSizeLimit = new Dimension(config.imageSizeLimitWidth(), config.imageSizeLimitHeight());
    this.setImageQuality = config.setImageQuality();
    this.defaultFmt = StringUtils.trim(config.defaultFmt());
    this.defaultFmtAlpha = StringUtils.trim(config.defaultFmtAlpha());

    if (this.enabled) {
      log.info("DynamicMediaSupport: enabled={}, capabilityEnabled={}, capabilityDetection={}, "
          + "authorPreviewMode={}, disableAemFallback={}, imageSizeLimit={}",
          this.enabled, this.dmCapabilityDetection, this.dmCapabilityDetection,
          this.authorPreviewMode, this.disableAemFallback, this.imageSizeLimit);
    }
  }

  @Override
  public boolean isDynamicMediaEnabled() {
    return this.enabled;
  }

  @Override
  public boolean isDynamicMediaCapabilityEnabled(boolean isDynamicMediaAsset) {
    switch (dmCapabilityDetection) {
      case AUTO:
        return isDynamicMediaAsset;
      case ON:
        return true;
      case OFF:
      default:
        return false;
    }
  }

  @Override
  public boolean isAemFallbackDisabled() {
    return disableAemFallback;
  }

  @Override
  public boolean isValidateSmartCropRenditionSizes() {
    return validateSmartCropRenditionSizes;
  }

  @Override
  public @NotNull Dimension getImageSizeLimit() {
    return this.imageSizeLimit;
  }

  @Override
  public boolean isSetImageQuality() {
    return this.setImageQuality;
  }


  @Override
  public @NotNull String getDefaultFmt() {
    return this.defaultFmt;
  }

  @Override
  public @NotNull String getDefaultFmtAlpha() {
    return this.defaultFmtAlpha;
  }

  @Override
  public @Nullable ImageProfile getImageProfile(@NotNull String profilePath) {
    try (ResourceResolver resourceResolver = resourceResolverFactory
        .getServiceResourceResolver(Map.of(ResourceResolverFactory.SUBSERVICE, SERVICEUSER_SUBSERVICE))) {
      Resource profileResource = resourceResolver.getResource(profilePath);
      if (profileResource != null) {
        log.debug("Loaded image profile: {}", profilePath);
        return new ImageProfileImpl(profileResource);
      }
    }
    catch (LoginException ex) {
      log.error("Missing service user mapping for 'io.wcm.handler.media:dynamic-media-support' - see https://wcm.io/handler/media/configuration.html", ex);
    }
    log.debug("Image profile not found: {}", profilePath);
    return null;
  }

  @Override
  public @Nullable ImageProfile getImageProfileForAsset(@NotNull Asset asset) {
    Resource assetResource = AdaptTo.notNull(asset, Resource.class);
    Resource folderResource = assetResource.getParent();
    if (folderResource != null) {
      return getImageProfileForAssetFolder(folderResource);
    }
    return null;
  }

  private @Nullable ImageProfile getImageProfileForAssetFolder(@NotNull Resource folderResource) {
    if (!DAM_PATH_PATTERN.matcher(folderResource.getPath()).matches()) {
      return null;
    }
    Resource folderContentResource = folderResource.getChild(JCR_CONTENT);
    if (folderContentResource != null) {
      String imageProfilePath = folderContentResource.getValueMap().get(DamConstants.IMAGE_PROFILE, String.class);
      if (imageProfilePath != null) {
        return getImageProfile(imageProfilePath);
      }
    }
    Resource parentFolderResource = folderResource.getParent();
    if (parentFolderResource != null) {
      return getImageProfileForAssetFolder(parentFolderResource);
    }
    else {
      return null;
    }
  }

  @Override
  public @Nullable String getDynamicMediaServerUrl(@NotNull Asset asset, @Nullable UrlMode urlMode, @NotNull Adaptable adaptable) {
    Resource assetResource = AdaptTo.notNull(asset, Resource.class);
    if (authorPreviewMode && !forcePublishMode(urlMode)) {
      // route dynamic media requests through author instance for preview
      // return configured author URL, or empty string if none configured
      SiteConfig siteConfig = AdaptTo.notNull(adaptable, SiteConfig.class);
      String siteUrlAUthor = StringUtils.defaultString(siteConfig.siteUrlAuthor());
      UrlHandler urlHandler = AdaptTo.notNull(adaptable, UrlHandler.class);
      return urlHandler.applySiteUrlAutoDetection(siteUrlAUthor);
    }
    try {
      String[] productionAssetUrls = dynamicMediaPublishUtils.externalizeImageDeliveryAsset(assetResource);
      if (productionAssetUrls != null && productionAssetUrls.length > 0) {
        return productionAssetUrls[0];
      }
    }
    catch (RepositoryException ex) {
      log.warn("Unable to get dynamic media production asset URLs for {}", assetResource.getPath(), ex);
    }
    log.warn("Unable to get dynamic media production asset URLs for {}", assetResource.getPath());
    return null;
  }

  /**
   * If URL mode is target for publish instance, use dynamic media production URL.
   * @param urlMode URL mode
   * @return true if publish mode should be forced
   */
  private boolean forcePublishMode(@Nullable UrlMode urlMode) {
    return urlMode != null && (urlMode.equals(UrlModes.FULL_URL_PUBLISH)
        || urlMode.equals(UrlModes.FULL_URL_PUBLISH_FORCENONSECURE)
        || urlMode.equals(UrlModes.FULL_URL_PUBLISH_FORCESECURE)
        || urlMode.equals(UrlModes.FULL_URL_PUBLISH_PROTOCOLRELATIVE));
  }

}
