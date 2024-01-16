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
package io.wcm.handler.mediasource.dam.impl.ngdm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.dam.api.Asset;

/**
 * Supports rendering asset renditions from stored in AEMaaCS sites instance via Next Generation Dynamic Media
 * "Web-Optimized Image Delivery", rendering the renditions on the edge.
 * This is not available in AEM 6.5 or AEMaaCS SDK.
 */
public interface WebOptimizedImageDeliveryService {

  /**
   * @return Whether AEM AssetDelivery service is available and the support is enabled.
   */
  boolean isEnabled();

  /**
   * Get delivery URL for a rendition of an asset.
   * @param asset Asset
   * @param params Parameters
   * @return Delivery URL or null if not supported or not enabled
   */
  @Nullable
  String getDeliveryUrl(@NotNull Asset asset, @NotNull WebOptimizedImageDeliveryParams params);

}
