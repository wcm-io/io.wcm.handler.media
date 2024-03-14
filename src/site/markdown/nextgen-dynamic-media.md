## Next-Generation Dynamic Media

wcm.io Media Handler optionally supports the [Next Generation Dynamic Media][aem-nextgen-dm] feature of AEM as a Cloud Service for:

* Rendering renditions including resizing and cropping
* Delivery via Next Generation Dynamic Media CDN
* AI-based Smart Cropping

Next Generation Dynamic Media support is never applied to assets stored in the AEMaaCS instance itself, but only for remote assets referenced via the Next Generation Dynamic Media asset picker. Those remote asset references typically start with `/urn:aaid:aem:...`. When Next Generation Dynamic Media is enabled for an AEMaaCS instance, the asset picker is shown automatically for Media Handler Granite UI widgets.

Remote assets are only shown in the asset picker, if they have "approval" state. The publish state of the asset inside AEM is not relevant.


### Dynamic Media concept

The integration with Next Generation Dynamic Media builds on the [general concepts][general-concepts] of the Media Handler using media formats and a unified media handling API to resolve the renditions for each use case.

If a Next Generation Dynamic Media remote asset is used, the media handler returns rendition URLs pointing to the remote asset instance using the [Assets Delivery API (DM API)][aem-dm-api]. From the [supported file formats][file-format-support] Next Generation Dynamic Media supports scaling and smart cropping for JPEG, PNG, GIF and TIFF images. All other file formats including SVG images are delivered as original binary via the Next Generation Dynamic Media CDN.

It's not required to configure anything in the component instance edit dialogs or content policies.


### Smart Cropping

Smart Cropping is used automatically if a media format with a specific ratio (e.g. 16:9) is used.

Media formats without any size restrictions, or e.g. only with a width restrictions, can be rendered, but are not cropped.


### Validating Remote Asset Metadata

By default, the Media Handler assumes that a given remote asset reference is always valid, and supports all requested resolutions.

Optionally, you can enable the metadata service. If enabled, each time a remote asset reference is resolved, the following checks are executed:

* If the remote asset does not exist, or is not approved, the reference is handled as invalid and the component can react to it (e.g. hide the image component)
* If the requested resolution of a rendition is larger than the original resolution of the binary asset, the rendition is handled as invalid. This avoid upscaling, and avoids using an asset in a context which would result in bad image quality for the user.

See system configuration how to enable the metadata service.


### System configuration

If Next Generation Dynamic Media is enabled for a AEMaaCS instance, it will work out-of-the-box with the Media Handler. In your project-specific implementation of `io.wcm.handler.media.spi.MediaHandlerConfig` you have to add the media sources implementation `io.wcm.handler.mediasource.ngdm.NextGenDynamicMediaMediaSource` to the list returned by the `getSources()` method (overwrite it from the superclass if required).

The "wcm.io Next Generation Dynamic Media Support" OSGi configuration allows to reconfigure the actual URLs used for the [Assets Delivery API (DM API)][aem-dm-api]. Usually you can stick with the default values which reflect the latest version of the DM API.

The "wcm.io Next Generation Dynamic Media Metadata Service" allows to enable the Asset Metadata support (see above). When this is enabled, for each resolved remote asset, a HTTP request is send from the server to the DM API, so make sure this is allowed in the network infrastructure (should work by default in AEMaaCS instances). Optionally, you can configure an proxy server and timeouts.


### Known Limitations (as of March 2024)

* The DM API URLs still have to contain an `accept-experimental` URL parameter, and the metadata services has to use an `X-Adobe-Accept-Experimental` HTTP header. Both will fade out once Next Generation Dynamic Media reaches full general availability (expected later in 2024).
* Next Generation Dynamic Media is not supported in Media Handler for AEM 6.x, only for AEMaaCS
* Same as the Adobe Core Components, currently only a single remote AEM Asset instance is supported, which is configured centrally as described in [Next Generation Dynamic Media][aem-nextgen-dm]. The media handler is used the same convention for referencing remote assets (using strings starting with `/urn:aaid:aem:...`). This convention also does not support multiple remote AEM Asset instances, as it does not include a pointer to the Repository ID.
* If a component dialog is re-opened with a remote asset references and one of the Media Handler Granite UI widgets (e.g. pathfield), no thumbnail is displayed for the remote asset. But the reference is valid and works. The root cause is a bug/limitation in the underlying AEM pathfield component, which hopefully will be fixed soon by Adobe (SITES-19894).
* The Next Generation Dynamic Media remote asset picker currently ignored any folder structures for assets on the remote AEM Asset instance.
* The DM API currently does not support sending a "Content-Disposition: attachment" HTTP header for downloads. So even, if this is enforced by the Media Handler, it currently does not work for remote assets.


[aem-nextgen-dm]: https://experienceleague.adobe.com/docs/experience-manager-core-components/using/developing/next-gen-dm.html?lang=en
[aem-dm-api]: https://adobe-aem-assets-delivery-experimental.redoc.ly/
[general-concepts]: general-concepts.html
[file-format-support]: file-format-support.html
[configuration]: configuration.html
