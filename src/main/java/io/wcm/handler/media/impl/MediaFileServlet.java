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

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.commons.jcr.JcrConstants;

/**
 * Stream binary data stored in a nt:file or nt:resource node.
 * Optional support for Content-Disposition header ("download_attachment").
 */
@Component(service = Servlet.class, immediate = true, property = {
    "sling.servlet.extensions=" + MediaFileServletConstants.EXTENSION,
    "sling.servlet.selectors=" + MediaFileServletConstants.SELECTOR,
    "sling.servlet.resourceTypes=" + JcrConstants.NT_FILE,
    "sling.servlet.resourceTypes=" + JcrConstants.NT_RESOURCE,
    "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
@Designate(ocd = MediaFileServlet.Config.class)
public final class MediaFileServlet extends AbstractMediaFileServlet {
  private static final long serialVersionUID = 1L;

  private boolean svgContentSecurityPolicy;

  @ObjectClassDefinition(
      name = "wcm.io Media Handler Media File Servlet",
      description = "Configures delivery of media file binaries.")
  @interface Config {

    @AttributeDefinition(
        name = "SVG Content Security Policy",
        description = "Apply XSS protection when serving SVG files by setting Content-Security-Policy to 'sandbox'.")
    boolean svgContentSecurityPolicy() default true;

  }

  @Activate
  private void activate(Config config) {
    this.svgContentSecurityPolicy = config.svgContentSecurityPolicy();
  }

  @Override
  protected void setSVGContentSecurityPolicy(@NotNull SlingHttpServletResponse response) {
    if (this.svgContentSecurityPolicy) {
      super.setSVGContentSecurityPolicy(response);
    }
  }

}
