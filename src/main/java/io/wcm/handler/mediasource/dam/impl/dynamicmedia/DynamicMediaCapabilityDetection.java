/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
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

/**
 * Modes to detect if Dynamic Media capability is available on a author/publish instance.
 */
enum DynamicMediaCapabilityDetection {

  /**
   * Default: Auto-detect if Dynamic Media capability is available on author/publish instance by
   * checking for the feature flag com.adobe.dam.asset.scene7.feature.flag.
   */
  AUTO,

  /**
   * Assume Dynamic Media capability is not available without doing any auto-detection.
   */
  OFF,

  /**
   * Assume Dynamic Media capability is available without doing any auto-detection.
   */
  ON

}
