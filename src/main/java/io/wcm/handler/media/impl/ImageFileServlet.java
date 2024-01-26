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
package io.wcm.handler.media.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.handler.store.AssetStore;
import com.day.image.Layer;

import io.wcm.handler.media.CropDimension;
import io.wcm.handler.media.format.Ratio;
import io.wcm.handler.media.spi.MediaHandlerConfig;
import io.wcm.sling.commons.adapter.AdaptTo;
import io.wcm.wcm.commons.contenttype.ContentType;
import io.wcm.wcm.commons.contenttype.FileExtension;

/**
 * Stream resized or cropped image from binary data stored in a nt:file or nt:resource node.
 * Optional support for Content-Disposition header ("download_attachment").
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + MediaFileServletConstants.EXTENSION,
    "sling.servlet.selectors=" + ImageFileServlet.SELECTOR,
    "sling.servlet.resourceTypes=" + JcrConstants.NT_FILE,
    "sling.servlet.resourceTypes=" + JcrConstants.NT_RESOURCE,
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public final class ImageFileServlet extends AbstractMediaFileServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Selector
   */
  public static final String SELECTOR = "image_file";

  @Reference
  private AssetStore assetStore;

  @Override
  @SuppressWarnings("java:S3776") // ignore complexity
  protected byte @Nullable [] getBinaryData(@NotNull Resource resource, @NotNull SlingHttpServletRequest request) throws IOException {
    // get media app config
    MediaHandlerConfig config = AdaptTo.notNull(request, MediaHandlerConfig.class);

    // parse selectors
    ImageFileServletSelector params = new ImageFileServletSelector(request.getRequestPathInfo().getSelectors());
    int width = params.getWidth();
    int height = params.getHeight();
    CropDimension cropDimension = params.getCropDimension();
    int rotation = params.getRotation();
    int quality = params.getQuality();

    // ensure valid image size
    if (width < 0 || height < 0 || (width == 0 && height == 0)) {
      return null;
    }

    Layer layer = ResourceLayerUtil.toLayer(resource, assetStore);
    if (layer == null) {
      return null;
    }

    // if only width or only height is given - derive other value from ratio
    double originalRatio;
    if (cropDimension != null) {
      originalRatio = Ratio.get(cropDimension);
    }
    else {
      originalRatio = Ratio.get(layer.getWidth(), layer.getHeight());
    }
    if (width == 0) {
      width = (int)Math.round(height * originalRatio);
    }
    else if (height == 0) {
      height = (int)Math.round(width / originalRatio);
    }

    // if required: crop image
    if (cropDimension != null) {
      layer.crop(cropDimension.getRectangle());
    }
    else {
      // if image ratio that is requested does not match with the given ratio apply a center-crop here
      double requestedRatio = Ratio.get(width, height);
      if (!Ratio.matches(originalRatio, requestedRatio)) {
        cropDimension = ImageTransformation.calculateAutoCropDimension(layer.getWidth(), layer.getHeight(), requestedRatio);
        layer.crop(cropDimension.getRectangle());
      }
    }

    // if required: rotate image
    if (rotation != 0) {
      layer.rotate(rotation);
    }

    // resize layer
    if (width <= layer.getWidth() && height <= layer.getHeight()) {
      layer.resize(width, height);
    }

    // determine layer quality with fallback to default image quality if not set
    String contentType = getContentType(resource, request);
    double layerQuality;
    if (quality > 0) {
      layerQuality = quality / 100d;
    }
    else {
      layerQuality = config.getDefaultImageQuality(contentType);
    }

    // stream to byte array
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    layer.write(contentType, layerQuality, bos);
    bos.flush();
    return bos.toByteArray();
  }

  @Override
  protected @NotNull String getContentType(@NotNull Resource resource, @NotNull SlingHttpServletRequest request) {

    // get filename from suffix to get extension
    String fileName = request.getRequestPathInfo().getSuffix();
    if (StringUtils.isNotEmpty(fileName)) {
      // if extension is PNG use PNG content type, otherwise fallback to JPEG
      String fileExtension = StringUtils.substringAfterLast(fileName, ".");
      if (StringUtils.equalsIgnoreCase(fileExtension, FileExtension.PNG)) {
        return ContentType.PNG;
      }
    }

    // for rendered images use JPEG mime type as default fallback
    return ContentType.JPEG;
  }

  /**
   * Get image filename to be used for the URL with file extension matching the image format which is produced by this
   * servlet.
   * @param originalFilename Original filename of the image to render.
   * @return Filename to be used for URL.
   */
  public static String getImageFileName(@NotNull String originalFilename) {
    return getImageFileName(originalFilename, null);
  }

  /**
   * Get image filename to be used for the URL with file extension matching the image format which is produced by this
   * servlet.
   * @param originalFilename Original filename of the image to render.
   * @param enforceOutputFileExtension Enforced output file extensions from media args
   * @return Filename to be used for URL.
   */
  public static String getImageFileName(@NotNull String originalFilename,
      @Nullable String enforceOutputFileExtension) {
    String namePart = StringUtils.substringBeforeLast(originalFilename, ".");
    String extensionPart = StringUtils.substringAfterLast(originalFilename, ".");
    if (enforceOutputFileExtension != null) {
      extensionPart = enforceOutputFileExtension;
    }

    // use PNG format if requested format is PNG, otherwise always use JPEG
    if (StringUtils.equalsIgnoreCase(extensionPart, FileExtension.PNG)) {
      extensionPart = FileExtension.PNG;
    }
    else {
      extensionPart = FileExtension.JPEG;
    }
    return namePart + "." + extensionPart;
  }

}
