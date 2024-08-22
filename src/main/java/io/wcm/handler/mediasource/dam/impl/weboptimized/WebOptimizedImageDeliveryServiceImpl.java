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

import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.adobe.cq.wcm.spi.AssetDelivery;
import com.day.cq.dam.api.Asset;

import io.wcm.sling.commons.adapter.AdaptTo;

/**
 * Implements {@link WebOptimizedImageDeliveryService}.
 */
@Component(service = WebOptimizedImageDeliveryService.class, immediate = true)
@Designate(ocd = WebOptimizedImageDeliveryServiceImpl.Config.class)
public class WebOptimizedImageDeliveryServiceImpl implements WebOptimizedImageDeliveryService {

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Web-Optimized Image Delivery Support",
      description = "Support for AEMaaCS Web-Optimized Image Delivery capabilites.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "Enable support for Web-Optimized Image Delivery (if available).")
    boolean enabled() default true;

    @AttributeDefinition(
        name = "Cropping Option",
        description = "Use relative cropping parameters (e.g. crop=0.0p,5.0p,100.0p,80.0p) "
            + "or absolute cropping paremters (e.g. crop=0,10,200,100), both based on the original image dimensions.")
    WebOptimizedImageDeliveryCropOption cropOption() default WebOptimizedImageDeliveryCropOption.RELATIVE_PARAMETERS;

  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
  private AssetDelivery assetDelivery;

  private boolean enabled;
  private WebOptimizedImageDeliveryCropOption cropOption;

  @Activate
  private void activate(Config config) {
    this.enabled = config.enabled();
    this.cropOption = config.cropOption();
  }

  @Override
  public boolean isEnabled() {
    return enabled && this.assetDelivery != null;
  }

  @Override
  public WebOptimizedImageDeliveryCropOption getCropOption() {
    return cropOption;
  }

  @Override
  public @Nullable String getDeliveryUrl(@NotNull Asset asset, @NotNull WebOptimizedImageDeliveryParams params) {
    if (!isEnabled()) {
      return null;
    }
    Resource resource = AdaptTo.notNull(asset, Resource.class);
    Map<String, Object> parameterMap = ParameterMap.build(asset, params, cropOption);
    return assetDelivery.getDeliveryURL(resource, parameterMap);
  }

}
