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
package io.wcm.handler.mediasource.dam.impl.weboptimized;

/**
 * Choose how to calculate/apply cropping parameter when using Web-Optimized image delivery.
 */
public enum WebOptimizedImageDeliveryCropOption {

  /**
   * Crop renditions using relative percentage values as parameters (e.g. crop=0.0p,5.0p,100.0p,80.0p),
   * based on the original image dimensions.
   */
  RELATIVE_PARAMETERS,

  /**
   * Crop renditions using absolute pixel values as parameters (e.g. crop=0,10,200,100),
   * based on the original image dimensions.
   */
  ABSOLUTE_PARAMETERS

}
