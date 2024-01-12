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
 * Modes to detect if Dynamic Media is available for a given asset.
 */
enum DynamicMediaCapabilityDetection {

  /**
   * Default: Auto-detect if Dynamic Media is available for a given asset.
   * If a property <code>dam:scene7File</code> exists in the metadata of the asset, Dynamic Media is considered
   * available. If the property does not exist, the asset is treated as non-DM asset and renditions
   * are rendered within AEM.
   */
  AUTO,

  /**
   * Disables the detection of Dynamic Media. Dynamic Media is never used.
   * All renditions are rendered within AEM.
   */
  OFF,

  /**
   * Configures that Dynamic Media is available for the environment and should be used for all assets.
   */
  ON

}
