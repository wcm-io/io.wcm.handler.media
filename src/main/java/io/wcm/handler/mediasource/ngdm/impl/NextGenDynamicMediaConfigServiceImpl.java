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
package io.wcm.handler.mediasource.ngdm.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.cq.ui.wcm.commons.config.NextGenDynamicMediaConfig;

/**
 * Wraps access to NextGenDynamicMediaConfig - which is deployed but not accessible on AEM 6.5.
 */
@Component(service = NextGenDynamicMediaConfigService.class, immediate = true)
public class NextGenDynamicMediaConfigServiceImpl implements NextGenDynamicMediaConfigService {

  @Reference
  private NextGenDynamicMediaConfig nextGenDynamicMediaConfig;

  @Override
  public boolean enabled() {
    return this.nextGenDynamicMediaConfig.enabled();
  }

  @Override
  public String getImageDeliveryBasePath() {
    return this.nextGenDynamicMediaConfig.getImageDeliveryBasePath();
  }

  @Override
  public String getRepositoryId() {
    return this.nextGenDynamicMediaConfig.getRepositoryId();
  }

  @Override
  public String getApiKey() {
    return this.nextGenDynamicMediaConfig.getApiKey();
  }

  @Override
  public String getEnv() {
    return this.nextGenDynamicMediaConfig.getEnv();
  }

  @Override
  public String getImsClient() {
    return this.nextGenDynamicMediaConfig.getImsClient();
  }

}
