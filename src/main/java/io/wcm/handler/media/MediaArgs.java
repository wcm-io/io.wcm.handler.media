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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.handler.media.format.MediaFormat;
import io.wcm.handler.media.markup.DragDropSupport;
import io.wcm.handler.media.markup.IPERatioCustomize;
import io.wcm.handler.mediasource.dam.AemRenditionType;
import io.wcm.handler.url.UrlMode;
import io.wcm.wcm.commons.contenttype.FileExtension;
import io.wcm.wcm.commons.util.AemObjectReflectionToStringBuilder;

/**
 * Holds parameters to influence the media resolving process.
 */
@ProviderType
public final class MediaArgs implements Cloneable {

  private MediaFormatOption[] mediaFormatOptions;
  private boolean autoCrop;
  private String[] fileExtensions;
  private String enforceOutputFileExtension;
  private UrlMode urlMode;
  private long fixedWidth;
  private long fixedHeight;
  private boolean download;
  private boolean contentDispositionAttachment;
  private String altText;
  private boolean forceAltValueFromAsset;
  private boolean decorative;
  private boolean dummyImage = true;
  private String dummyImageUrl;
  private Set<AemRenditionType> includeAssetAemRenditions;
  private Boolean includeAssetThumbnails;
  private Boolean includeAssetWebRenditions;
  private ImageSizes imageSizes;
  private PictureSource[] pictureSourceSets;
  private Double imageQualityPercentage;
  private DragDropSupport dragDropSupport = DragDropSupport.AUTO;
  private IPERatioCustomize ipeRatioCustomize = IPERatioCustomize.AUTO;
  private boolean dynamicMediaDisabled;
  private boolean webOptimizedImageDeliveryDisabled;
  private ValueMap properties;

  private static final Set<String> ALLOWED_FORCED_FILE_EXTENSIONS = Set.of(
      FileExtension.JPEG, FileExtension.PNG);

  /**
   * Default constructor
   */
  public MediaArgs() {
    // default constructor
  }

  /**
   * @param mediaFormats Media formats
   */
  @SuppressWarnings("null")
  public MediaArgs(@NotNull MediaFormat @NotNull... mediaFormats) {
    mediaFormats(mediaFormats);
  }

  /**
   * @param mediaFormatNames Media format names
   */
  public MediaArgs(@NotNull String @NotNull... mediaFormatNames) {
    mediaFormatNames(mediaFormatNames);
  }

  /**
   * Returns list of media formats to resolve to.
   * @return Media formats
   */
  public MediaFormat @Nullable [] getMediaFormats() {
    if (this.mediaFormatOptions != null) {
      MediaFormat[] result = Arrays.stream(this.mediaFormatOptions)
          .filter(option -> option.getMediaFormatName() == null)
          .map(MediaFormatOption::getMediaFormat)
          .toArray(size -> new MediaFormat[size]);
      if (result.length > 0) {
        return result;
      }
    }
    return null;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  public @NotNull MediaArgs mediaFormats(@Nullable MediaFormat @Nullable... values) {
    if (values == null || values.length == 0) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = Arrays.stream(values)
          .map(mediaFormat -> new MediaFormatOption(mediaFormat, false))
          .toArray(size -> new MediaFormatOption[size]);
    }
    return this;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats
   * @return this
   */
  public @NotNull MediaArgs mandatoryMediaFormats(@NotNull MediaFormat @Nullable... values) {
    if (values == null || values.length == 0) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = Arrays.stream(values)
          .map(mediaFormat -> new MediaFormatOption(mediaFormat, true))
          .toArray(size -> new MediaFormatOption[size]);
    }
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param value Media format
   * @return this
   */
  public @NotNull MediaArgs mediaFormat(MediaFormat value) {
    if (value == null) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = new MediaFormatOption[] {
          new MediaFormatOption(value, false)
      };
    }
    return this;
  }

  /**
   * The "mandatory" flag of all media format options is set to to the given value.
   * @param value Resolving of all media formats is mandatory.
   * @return this
   */
  public @NotNull MediaArgs mediaFormatsMandatory(boolean value) {
    if (this.mediaFormatOptions != null) {
      this.mediaFormatOptions = Arrays.stream(this.mediaFormatOptions)
          .map(option -> option.withMandatory(value))
          .toArray(size -> new MediaFormatOption[size]);
    }
    return this;
  }

  /**
   * Returns list of media formats to resolve to. See {@link #getMediaFormatNames()} for details.
   * @return Media format names
   */
  public String @Nullable [] getMediaFormatNames() {
    if (this.mediaFormatOptions != null) {
      String[] result = Arrays.stream(this.mediaFormatOptions)
          .filter(option -> option.getMediaFormatName() != null)
          .map(MediaFormatOption::getMediaFormatName)
          .toArray(size -> new String[size]);
      if (result.length > 0) {
        return result;
      }
    }
    return null;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param names Media format names.
   * @return this
   */
  public @NotNull MediaArgs mediaFormatNames(@NotNull String @Nullable... names) {
    if (names == null || names.length == 0) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = Arrays.stream(names)
          .map(name -> new MediaFormatOption(name, false))
          .toArray(size -> new MediaFormatOption[size]);
    }
    return this;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param names Media format names.
   * @return this
   */
  public @NotNull MediaArgs mandatoryMediaFormatNames(@NotNull String @Nullable... names) {
    if (names == null || names.length == 0) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = Arrays.stream(names)
          .map(name -> new MediaFormatOption(name, true))
          .toArray(size -> new MediaFormatOption[size]);
    }
    return this;
  }

  /**
   * Sets a single media format to resolve to.
   * @param name Media format name
   * @return this
   */
  public @NotNull MediaArgs mediaFormatName(String name) {
    if (name == null) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = new MediaFormatOption[] {
          new MediaFormatOption(name, false)
      };
    }
    return this;
  }

  /**
   * Gets list of media formats to resolve to.
   * @return Media formats with mandatory flag
   */
  public MediaFormatOption @Nullable [] getMediaFormatOptions() {
    return this.mediaFormatOptions;
  }

  /**
   * Sets list of media formats to resolve to.
   * @param values Media formats with mandatory flag
   * @return this
   */
  public @NotNull MediaArgs mediaFormatOptions(@NotNull MediaFormatOption @Nullable... values) {
    if (values == null || values.length == 0) {
      this.mediaFormatOptions = null;
    }
    else {
      this.mediaFormatOptions = values;
    }
    return this;
  }

  /**
   * @return Enables "auto-cropping" mode. If no matching rendition is found
   *         it is tried to generate one by automatically cropping another one.
   */
  public boolean isAutoCrop() {
    return this.autoCrop;
  }

  /**
   * @param value Enables "auto-cropping" mode. If no matching rendition is found
   *          it is tried to generate one by automatically cropping another one.
   * @return this
   */
  public @NotNull MediaArgs autoCrop(boolean value) {
    this.autoCrop = value;
    return this;
  }

  /**
   * @return Accepted file extensions
   */
  public String @Nullable [] getFileExtensions() {
    return this.fileExtensions;
  }

  /**
   * @param values Accepted file extensions
   * @return this
   */
  public @NotNull MediaArgs fileExtensions(@NotNull String @Nullable... values) {
    if (values == null || values.length == 0) {
      this.fileExtensions = null;
    }
    else {
      this.fileExtensions = values;
    }
    return this;
  }

  /**
   * @param value Accepted file extension
   * @return this
   */
  public @NotNull MediaArgs fileExtension(@Nullable String value) {
    if (value == null) {
      this.fileExtensions = null;
    }
    else {
      this.fileExtensions = new String[] {
          value
      };
    }
    return this;
  }

  /**
   * Enforces image file type for renditions.
   *
   * <p>
   * By default, renditions are rendered with the same file type as the original rendition (except if the
   * original renditions uses a file type not directly supported in browser, e.g. a TIFF image).
   * With this parameter, it is possible to enforce generating renditions with this file type.
   * </p>
   *
   * <p>
   * Supported file types: JPEG, PNG
   * </p>
   * @return File extension to be used for returned renditions
   */
  public @Nullable String getEnforceOutputFileExtension() {
    return this.enforceOutputFileExtension;
  }

  /**
   * Enforces image file type for renditions.
   *
   * <p>
   * By default, renditions are rendered with the same file type as the original rendition (except if the
   * original renditions uses a file type not directly supported in browser, e.g. a TIFF image).
   * With this parameter, it is possible to enforce generating renditions with this file type.
   * </p>
   *
   * <p>
   * Supported file types: JPEG, PNG
   * </p>
   * @param value File extension to be used for returned renditions
   * @return this
   */
  public @NotNull MediaArgs enforceOutputFileExtension(@Nullable String value) {
    if (!ALLOWED_FORCED_FILE_EXTENSIONS.contains(value)) {
      throw new IllegalArgumentException("Allowed enforced output file extensions: "
          + StringUtils.join(ALLOWED_FORCED_FILE_EXTENSIONS, ","));
    }
    this.enforceOutputFileExtension = value;
    return this;
  }

  /**
   * @return URL mode
   */
  public @Nullable UrlMode getUrlMode() {
    return this.urlMode;
  }

  /**
   * @param value URS mode
   * @return this
   */
  public @NotNull MediaArgs urlMode(@Nullable UrlMode value) {
    this.urlMode = value;
    return this;
  }

  /**
   * Use fixed width instead of width from media format or original image
   * @return Fixed width
   */
  public long getFixedWidth() {
    return this.fixedWidth;
  }

  /**
   * Use fixed width instead of width from media format or original image
   * @param value Fixed width
   * @return this
   */
  public @NotNull MediaArgs fixedWidth(long value) {
    this.fixedWidth = value;
    return this;
  }

  /**
   * Use fixed height instead of width from media format or original image
   * @return Fixed height
   */
  public long getFixedHeight() {
    return this.fixedHeight;
  }

  /**
   * Use fixed height instead of width from media format or original image
   * @param value Fixed height
   * @return this
   */
  public @NotNull MediaArgs fixedHeight(long value) {
    this.fixedHeight = value;
    return this;
  }

  /**
   * Use fixed dimensions instead of width from media format or original image
   * @param widthValue Fixed width
   * @param heightValue Fixed height
   * @return this
   */
  public @NotNull MediaArgs fixedDimension(long widthValue, long heightValue) {
    this.fixedWidth = widthValue;
    this.fixedHeight = heightValue;
    return this;
  }

  /**
   * @return Accept only media formats that have the download flag set.
   */
  public boolean isDownload() {
    return this.download;
  }

  /**
   * @param value Accept only media formats that have the download flag set.
   * @return this
   */
  public @NotNull MediaArgs download(boolean value) {
    this.download = value;
    return this;
  }

  /**
   * @return Whether to set a "Content-Disposition" header to "attachment" for forcing a "Save as" dialog on the client
   */
  public boolean isContentDispositionAttachment() {
    return this.contentDispositionAttachment;
  }

  /**
   * @param value Whether to set a "Content-Disposition" header to "attachment" for forcing a "Save as" dialog on the
   *          client
   * @return this
   */
  public @NotNull MediaArgs contentDispositionAttachment(boolean value) {
    this.contentDispositionAttachment = value;
    return this;
  }

  /**
   * @return The custom alternative text that is to be used instead of the one defined in the the asset metadata.
   */
  public @Nullable String getAltText() {
    return this.altText;
  }

  /**
   * Allows to specify a custom alternative text that is to be used instead of the one defined in the the asset
   * metadata.
   * @param value Custom alternative text. If null or empty, the default alt text from media library is used.
   * @return this
   */
  public @NotNull MediaArgs altText(@Nullable String value) {
    this.altText = value;
    return this;
  }

  /**
   * @return Whether to force to read alt. text from DAM asset description.
   */
  public boolean isForceAltValueFromAsset() {
    return this.forceAltValueFromAsset;
  }

  /**
   * @param value Whether to force to read alt. text from DAM asset description.
   *          If not set, the asset description is used as fallback value of no custom alt. text is defined.
   * @return this
   */
  public @NotNull MediaArgs forceAltValueFromAsset(boolean value) {
    this.forceAltValueFromAsset = value;
    return this;
  }

  /**
   * @return Marks this image as "decorative". Alt. text is then explicitly set to an empty string.
   */
  public boolean isDecorative() {
    return this.decorative;
  }

  /**
   * @param value Marks this image as "decorative". Alt. text is then explicitly set to an empty string.
   * @return this
   */
  public @NotNull MediaArgs decorative(boolean value) {
    this.decorative = value;
    return this;
  }

  /**
   * @return If set to true, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   */
  public boolean isDummyImage() {
    return this.dummyImage;
  }

  /**
   * @param value If set to false, media handler never returns a dummy image. Otherwise this can happen in edit mode.
   * @return this
   */
  public @NotNull MediaArgs dummyImage(boolean value) {
    this.dummyImage = value;
    return this;
  }

  /**
   * @return Url of custom dummy image. If null default dummy image is used.
   */
  public @Nullable String getDummyImageUrl() {
    return this.dummyImageUrl;
  }

  /**
   * @param value Url of custom dummy image. If null default dummy image is used.
   * @return this
   */
  public @NotNull MediaArgs dummyImageUrl(@Nullable String value) {
    this.dummyImageUrl = value;
    return this;
  }

  /**
   * @return Defines which types of AEM-generated renditions (with <code>cq5dam.</code> prefix) are taken into
   *         account when trying to resolve the media request.
   */
  public @Nullable Set<AemRenditionType> getIncludeAssetAemRenditions() {
    return this.includeAssetAemRenditions;
  }

  /**
   * @param value Defines which types of AEM-generated renditions (with <code>cq5dam.</code> prefix) are taken into
   *          account when trying to resolve the media request.
   * @return this
   */
  public @NotNull MediaArgs includeAssetAemRenditions(@Nullable Set<AemRenditionType> value) {
    this.includeAssetAemRenditions = value;
    return this;
  }

  /**
   * @return If set to true, thumbnail generated by AEM (with <code>cq5dam.thumbnail.</code> prefix) are taken
   *         into account as well when trying to resolve the media request. Defaults to false.
   * @deprecated Use {@link #includeAssetAemRenditions(Set)} instead.
   */
  @Deprecated(since = "2.0.0")
  public @Nullable Boolean isIncludeAssetThumbnails() {
    return this.includeAssetThumbnails;
  }

  /**
   * @param value If set to true, thumbnail generated by AEM (with <code>cq5dam.thumbnail.</code> prefix) are
   *          taken into account as well when trying to resolve the media request.
   * @return this
   * @deprecated Use {@link #includeAssetAemRenditions(Set)} instead.
   */
  @Deprecated(since = "2.0.0")
  public @NotNull MediaArgs includeAssetThumbnails(boolean value) {
    this.includeAssetThumbnails = value;
    return this;
  }

  /**
   * @return If set to true, web renditions generated by AEM (with <code>cq5dam.web.</code> prefix) are taken
   *         into account as well when trying to resolve the media request.
   *         If null, the default setting applies from the media handler configuration.
   * @deprecated Use {@link #includeAssetAemRenditions(Set)} instead.
   */
  @Deprecated(since = "2.0.0")
  public @Nullable Boolean isIncludeAssetWebRenditions() {
    return this.includeAssetWebRenditions;
  }

  /**
   * @param value If set to true, web renditions generated by AEM (with <code>cq5dam.web.</code> prefix) are
   *          taken into account as well when trying to resolve the media request.
   * @return this
   * @deprecated Use {@link #includeAssetAemRenditions(Set)} instead.
   */
  @Deprecated(since = "2.0.0")
  public @NotNull MediaArgs includeAssetWebRenditions(boolean value) {
    this.includeAssetWebRenditions = value;
    return this;
  }

  /**
   * @return Image sizes for responsive image handling
   */
  public @Nullable ImageSizes getImageSizes() {
    return this.imageSizes;
  }

  /**
   * @param value Image sizes for responsive image handling
   * @return this
   */
  public @NotNull MediaArgs imageSizes(@Nullable ImageSizes value) {
    this.imageSizes = value;
    return this;
  }

  /**
   * @return Picture sources for responsive image handling
   */
  public PictureSource @Nullable [] getPictureSources() {
    return this.pictureSourceSets;
  }

  /**
   * @param value Picture sources for responsive image handling
   * @return this
   */
  public @NotNull MediaArgs pictureSources(@NotNull PictureSource @Nullable... value) {
    this.pictureSourceSets = value;
    return this;
  }

  /**
   * @return If set to true, dynamic media support is disabled even when enabled on the instance.
   */
  public boolean isDynamicMediaDisabled() {
    return this.dynamicMediaDisabled;
  }

  /**
   * @param value If set to true, dynamic media support is disabled even when enabled on the instance.
   * @return this
   */
  public @NotNull MediaArgs dynamicMediaDisabled(boolean value) {
    this.dynamicMediaDisabled = value;
    return this;
  }

  /**
   * @return If set to true, web-optimized image delivery is disabled even when enabled on the instance.
   */
  public boolean isWebOptimizedImageDeliveryDisabled() {
    return this.webOptimizedImageDeliveryDisabled;
  }

  /**
   * @param value If set to true, web-optimized image delivery is disabled even when enabled on the instance.
   * @return this
   */
  public @NotNull MediaArgs webOptimizedImageDeliveryDisabled(boolean value) {
    this.webOptimizedImageDeliveryDisabled = value;
    return this;
  }

  /**
   * @return Image quality in percent (0..1) for images with lossy compression (e.g. JPEG).
   */
  public @Nullable Double getImageQualityPercentage() {
    return this.imageQualityPercentage;
  }

  /**
   * @param value Image quality in percent (0..1) for images with lossy compression (e.g. JPEG).
   * @return this
   */
  public @NotNull MediaArgs imageQualityPercentage(@Nullable Double value) {
    this.imageQualityPercentage = value;
    return this;
  }

  /**
   * Drag&amp;Drop support for media builder.
   * @return Drag&amp;Drop support
   */
  public @NotNull DragDropSupport getDragDropSupport() {
    return this.dragDropSupport;
  }

  /**
   * Drag&amp;Drop support for media builder.
   * @param value Drag&amp;Drop support
   * @return this
   */
  public @NotNull MediaArgs dragDropSupport(@NotNull DragDropSupport value) {
    this.dragDropSupport = value;
    return this;
  }

  /**
   * @return Whether to set customized list of IPE cropping ratios.
   */
  public IPERatioCustomize getIPERatioCustomize() {
    return this.ipeRatioCustomize;
  }

  /**
   * @param value Whether to set customized list of IPE cropping ratios.
   * @return this
   */
  public @NotNull MediaArgs ipeRatioCustomize(@Nullable IPERatioCustomize value) {
    this.ipeRatioCustomize = value;
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param map Property map. Is merged with properties already set.
   * @return this
   */
  public @NotNull MediaArgs properties(@NotNull Map<String, Object> map) {
    getProperties().putAll(map);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @param key Property key
   * @param value Property value
   * @return this
   */
  public @NotNull MediaArgs property(@NotNull String key, @Nullable Object value) {
    getProperties().put(key, value);
    return this;
  }

  /**
   * Custom properties that my be used by application-specific markup builders or processors.
   * @return Value map
   */
  @NotNull
  public ValueMap getProperties() {
    if (this.properties == null) {
      this.properties = new ValueMapDecorator(new HashMap<>());
    }
    return this.properties;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  @SuppressWarnings("java:S3776") // ignore complexity
  public String toString() {
    ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
    if (mediaFormatOptions != null && mediaFormatOptions.length > 0) {
      sb.append("mediaFormats", "[" + StringUtils.join(mediaFormatOptions, ", ") + "]");
    }
    if (autoCrop) {
      sb.append("autoCrop", autoCrop);
    }
    if (fileExtensions != null && fileExtensions.length > 0) {
      sb.append("fileExtensions", StringUtils.join(fileExtensions, ","));
    }
    if (enforceOutputFileExtension != null) {
      sb.append("enforceOutputFileExtension", enforceOutputFileExtension);
    }
    if (urlMode != null) {
      sb.append("urlMode", urlMode);
    }
    if (fixedWidth > 0) {
      sb.append("fixedWidth", fixedWidth);
    }
    if (fixedHeight > 0) {
      sb.append("fixedHeight", fixedHeight);
    }
    if (download) {
      sb.append("download", download);
    }
    if (contentDispositionAttachment) {
      sb.append("contentDispositionAttachment", contentDispositionAttachment);
    }
    if (altText != null) {
      sb.append("altText", altText);
    }
    if (forceAltValueFromAsset) {
      sb.append("forceAltValueFromAsset", forceAltValueFromAsset);
    }
    if (decorative) {
      sb.append("decorative", decorative);
    }
    if (!dummyImage) {
      sb.append("dummyImage ", dummyImage);
    }
    if (dummyImageUrl != null) {
      sb.append("dummyImageUrl", dummyImageUrl);
    }
    if (includeAssetAemRenditions != null) {
      sb.append("includeAssetAemRenditions", includeAssetAemRenditions);
    }
    if (includeAssetThumbnails != null) {
      sb.append("includeAssetThumbnails", includeAssetThumbnails);
    }
    if (includeAssetWebRenditions != null) {
      sb.append("includeAssetWebRenditions", includeAssetWebRenditions);
    }
    if (imageSizes != null) {
      sb.append("imageSizes", imageSizes);
    }
    if (pictureSourceSets != null && pictureSourceSets.length > 0) {
      sb.append("pictureSourceSets", "[" + StringUtils.join(pictureSourceSets, ",") + "]");
    }
    if (imageQualityPercentage != null) {
      sb.append("imageQualityPercentage ", imageQualityPercentage);
    }
    if (dragDropSupport != DragDropSupport.AUTO) {
      sb.append("dragDropSupport ", dragDropSupport);
    }
    if (ipeRatioCustomize != IPERatioCustomize.AUTO) {
      sb.append("ipeRatioCustomize ", ipeRatioCustomize);
    }
    if (dynamicMediaDisabled) {
      sb.append("dynamicMediaDisabled", dynamicMediaDisabled);
    }
    if (webOptimizedImageDeliveryDisabled) {
      sb.append("webOptimizedImageDeliveryDisabled", webOptimizedImageDeliveryDisabled);
    }
    if (properties != null && !properties.isEmpty()) {
      sb.append("properties", AemObjectReflectionToStringBuilder.filteredValueMap(properties));
    }
    return sb.build();
  }

  /**
   * Custom clone-method for {@link MediaArgs}
   * @return the cloned {@link MediaArgs}
   */
  @Override
  @SuppressWarnings({ "java:S2975", "java:S1182", "checkstyle:SuperCloneCheck" }) // ignore clone warnings
  public MediaArgs clone() { //NOPMD
    MediaArgs clone = new MediaArgs();

    clone.mediaFormatOptions = ArrayUtils.clone(this.mediaFormatOptions);
    clone.autoCrop = this.autoCrop;
    clone.fileExtensions = ArrayUtils.clone(this.fileExtensions);
    clone.enforceOutputFileExtension = this.enforceOutputFileExtension;
    clone.urlMode = this.urlMode;
    clone.fixedWidth = this.fixedWidth;
    clone.fixedHeight = this.fixedHeight;
    clone.download = this.download;
    clone.contentDispositionAttachment = this.contentDispositionAttachment;
    clone.altText = this.altText;
    clone.forceAltValueFromAsset = this.forceAltValueFromAsset;
    clone.decorative = this.decorative;
    clone.dummyImage = this.dummyImage;
    clone.dummyImageUrl = this.dummyImageUrl;
    clone.includeAssetAemRenditions = this.includeAssetAemRenditions;
    clone.includeAssetThumbnails = this.includeAssetThumbnails;
    clone.includeAssetWebRenditions = this.includeAssetWebRenditions;
    clone.imageSizes = this.imageSizes;
    clone.pictureSourceSets = ArrayUtils.clone(this.pictureSourceSets);
    clone.imageQualityPercentage = this.imageQualityPercentage;
    clone.dragDropSupport = this.dragDropSupport;
    clone.ipeRatioCustomize = this.ipeRatioCustomize;
    clone.dynamicMediaDisabled = this.dynamicMediaDisabled;
    clone.webOptimizedImageDeliveryDisabled = this.webOptimizedImageDeliveryDisabled;
    if (this.properties != null) {
      clone.properties = new ValueMapDecorator(new HashMap<>(this.properties));
    }

    return clone;
  }

  /**
   * Media format to be applied on media processing.
   */
  @ProviderType
  public static final class MediaFormatOption {

    private final MediaFormat mediaFormat;
    private final String mediaFormatName;
    private final boolean mandatory;

    /**
     * @param mediaFormat Media format
     * @param mandatory Resolution of this media format is mandatory
     */
    public MediaFormatOption(@Nullable MediaFormat mediaFormat, boolean mandatory) {
      this.mediaFormat = mediaFormat;
      this.mediaFormatName = null;
      this.mandatory = mandatory;
    }

    /**
     * @param mediaFormatName Media format name
     * @param mandatory Resolution of this media format is mandatory
     */
    public MediaFormatOption(@NotNull String mediaFormatName, boolean mandatory) {
      this.mediaFormat = null;
      this.mediaFormatName = mediaFormatName;
      this.mandatory = mandatory;
    }

    /**
     * @return Media format
     */
    public @Nullable MediaFormat getMediaFormat() {
      return this.mediaFormat;
    }

    /**
     * @return Media format name
     */
    public @Nullable String getMediaFormatName() {
      return this.mediaFormatName;
    }

    /**
     * @return Resolution of this media format is mandatory
     */
    public boolean isMandatory() {
      return this.mandatory;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      return mediaFormatToString(mediaFormat, mediaFormatName, mandatory);
    }

    @NotNull
    MediaFormatOption withMandatory(boolean newMandatory) {
      if (this.mediaFormat != null) {
        return new MediaFormatOption(this.mediaFormat, newMandatory);
      }
      else {
        return new MediaFormatOption(this.mediaFormatName, newMandatory);
      }
    }

    static String mediaFormatToString(MediaFormat mediaFormat, String mediaFormatName, boolean mandatory) {
      StringBuilder sb = new StringBuilder();
      if (mediaFormat != null) {
        sb.append(mediaFormat.toString());
      }
      else if (mediaFormatName != null) {
        sb.append(mediaFormatName);
      }
      if (!mandatory) {
        sb.append("[?]");
      }
      return sb.toString();
    }

  }

  /**
   * Image sizes for responsive image handling.
   */
  @ProviderType
  public static final class ImageSizes {

    private final @NotNull String sizes;
    private final @NotNull WidthOption @NotNull [] widthOptions;

    /**
     * @param sizes A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
     *          source size list</a>
     * @param widths Widths for the renditions in the <code>srcset</code> attribute (all mandatory).
     */
    public ImageSizes(@NotNull String sizes, long @NotNull... widths) {
      this.sizes = sizes;
      this.widthOptions = Arrays.stream(widths)
          .distinct()
          .mapToObj(width -> new WidthOption(width, true))
          .toArray(WidthOption[]::new);
    }

    /**
     * @param sizes A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
     *          source size list</a>
     * @param widthOptions Widths for the renditions in the <code>srcset</code> attribute.
     */
    public ImageSizes(@NotNull String sizes, @NotNull WidthOption @NotNull... widthOptions) {
      this.sizes = sizes;
      this.widthOptions = widthOptions;
    }

    /**
     * @return A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
     *         source size list</a>
     */
    public @NotNull String getSizes() {
      return this.sizes;
    }

    /**
     * @return Widths for the renditions in the <code>srcset</code> attribute.
     */
    public @NotNull WidthOption @Nullable [] getWidthOptions() {
      return this.widthOptions;
    }

    /**
     * @return whether density descriptors should be used instead of width descriptors.
     */
    public boolean hasDensityDescriptors() {
      return StringUtils.isEmpty(this.sizes) &&
              Arrays.stream(this.widthOptions).map(WidthOption::getDensity).anyMatch(Objects::nonNull);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    @SuppressWarnings("null")
    public String toString() {
      ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
      sb.append("sizes", sizes);
      if (widthOptions != null && widthOptions.length > 0) {
        sb.append("widthOptions", StringUtils.join(widthOptions, ","));
      }
      return sb.build();
    }

  }

  /**
   * Picture source for responsive image handling.
   */
  @ProviderType
  public static final class PictureSource {

    private MediaFormat mediaFormat;
    private String mediaFormatName;
    private String media;
    private String sizes;
    private WidthOption[] widthOptions;

    /**
     * @param mediaFormat Media format
     */
    public PictureSource(@NotNull MediaFormat mediaFormat) {
      this.mediaFormat = mediaFormat;
    }

    /**
     * @param mediaFormatName Media format name
     */
    public PictureSource(@Nullable String mediaFormatName) {
      this.mediaFormatName = mediaFormatName;
    }

    private static @NotNull WidthOption @NotNull [] toWidthOptions(long @NotNull... widths) {
      return Arrays.stream(widths)
          .distinct()
          .mapToObj(width -> new WidthOption(width, true))
          .toArray(WidthOption[]::new);
    }

    /**
     * @return Media format
     */
    public @Nullable MediaFormat getMediaFormat() {
      return this.mediaFormat;
    }

    /**
     * @return Media format
     */
    public @Nullable String getMediaFormatName() {
      return this.mediaFormatName;
    }

    /**
     * @param value Widths for the renditions in the <code>srcset</code> attribute.
     * @return this
     */
    public PictureSource widthOptions(@NotNull WidthOption @NotNull... value) {
      this.widthOptions = value;
      return this;
    }

    /**
     * @return Widths for the renditions in the <code>srcset</code> attribute.
     */
    public @NotNull WidthOption @Nullable [] getWidthOptions() {
      return this.widthOptions;
    }

    /**
     * @param value Widths for the renditions in the <code>srcset</code> attribute.
     * @return this
     */
    public PictureSource widths(long @NotNull... value) {
      this.widthOptions = toWidthOptions(value);
      return this;
    }

    /**
     * @param value A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid
     *          source size list</a>.
     * @return this
     */
    public PictureSource sizes(@Nullable String value) {
      this.sizes = value;
      return this;
    }

    /**
     * @return A <a href="http://w3c.github.io/html/semantics-embedded-content.html#valid-source-size-list">valid source
     *         size list</a>.
     */
    public @Nullable String getSizes() {
      return this.sizes;
    }

    /**
     * @param value A <a href="http://w3c.github.io/html/infrastructure.html#valid-media-query-list">valid media query
     *          list</a>.
     * @return this
     */
    public PictureSource media(@Nullable String value) {
      this.media = value;
      return this;
    }

    /**
     * @return A <a href="http://w3c.github.io/html/infrastructure.html#valid-media-query-list">valid media query
     *         list</a>.
     */
    public @Nullable String getMedia() {
      return this.media;
    }

    /**
     * @return whether density descriptors should be used instead of width descriptors.
     */
    public boolean hasDensityDescriptors() {
      return StringUtils.isEmpty(this.sizes) &&
          Arrays.stream(this.widthOptions).map(WidthOption::getDensity).anyMatch(Objects::nonNull);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
      sb.append("mediaFormat", MediaFormatOption.mediaFormatToString(mediaFormat, mediaFormatName, true));
      if (media != null) {
        sb.append("media", media);
      }
      if (sizes != null) {
        sb.append("sizes", sizes);
      }
      if (widthOptions != null && widthOptions.length > 0) {
        sb.append("widthOptions", StringUtils.join(widthOptions, ","));
      }
      return sb.build();
    }

  }

  /**
   * Width value with mandatory flag.
   */
  @ProviderType
  public static final class WidthOption {

    private final long width;
    private final boolean mandatory;
    private final String density;

    /**
     * @param width mandatory width value
     */
    public WidthOption(long width) {
      this(width, null, true);
    }

    /**
     * @param width mandatory width value
     * @param density pixel density, or null for default density (1x)
     */
    public WidthOption(long width, @Nullable String density) {
      this(width, density, true);
    }

    /**
     * @param width Width value
     * @param mandatory Is it mandatory to resolve a rendition with this width
     */
    public WidthOption(long width, boolean mandatory) {
      this(width, null, mandatory);
    }

    /**
     * @param width Width value
     * @param density pixel density, or null for default density (1x)
     * @param mandatory Is it mandatory to resolve a rendition with this width
     */
    public WidthOption(long width, @Nullable String density, boolean mandatory) {
      this.width = width;
      this.mandatory = mandatory;
      this.density = density;
    }

    /**
     * @return Width value
     */
    public long getWidth() {
      return this.width;
    }

    /**
     * @return Is it mandatory to resolve a rendition with this width
     */
    public boolean isMandatory() {
      return this.mandatory;
    }

    /**
     * @return density descriptor or null
     */
    public @Nullable String getDensity() {
      return density;
    }

    /**
     * @return width descriptor for srcset, e.g. 200w
     */
    public @NotNull String getWidthDescriptor() {
      return String.format("%dw", this.width);
    }

    /**
     * @return density descriptor if it is not null and is not "1x", otherwise an empty string is returned
     */
    public @NotNull String getDensityDescriptor() {
      if (StringUtils.isEmpty(this.density) || StringUtils.equalsIgnoreCase(this.density, "1x")) {
        return StringUtils.EMPTY;
      }
      return this.density;
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(Long.toString(width));
      if (density != null) {
        sb.append(":").append(density);
      }
      if (!mandatory) {
        sb.append("?");
      }
      return sb.toString();
    }

  }

}
