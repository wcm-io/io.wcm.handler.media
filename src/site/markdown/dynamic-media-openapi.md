## Dynamic Media with OpenAPI

wcm.io Media Handler optionally supports the [Dynamic Media with OpenAPI][aem-nextgen-dm] feature of AEM as a Cloud Service for:

* Rendering renditions including resizing and cropping
* Delivery via Dynamic Media with OpenAPI CDN
* AI-based Smart Cropping

Dynamic Media with OpenAPI (formerly known as Next Generation Dynamic Media) has to be licensed separately and is not activated by default for AEMaaCS instances. It can be used for two use cases:

* Rendering remote assets referenced via the Remote Asset Picker from other assets instances
  * Those remote asset references typically start with `/urn:aaid:aem:...`.
  * When Dynamic Media with OpenAPI is enabled for an AEMaaCS instance, the asset picker is shown automatically for Media Handler Granite UI widgets.
* Rendering local assets stored in the same AEMaaCS instance.

The Adobe AEM WCM Core Components currently support only a subset of features, i.e. they only support remote assets but no local assets. When you are using Media Handler directly in your components, or via the [wcm.io WCM Core Components][wcm-core-components] you can leverage all features described in this page.


### Media Handler concept

The integration with Dynamic Media with OpenAPI builds on the [general concepts][general-concepts] of the Media Handler using media formats and a unified media handling API to resolve the renditions for each use case.

If a rendition is rendered via Dynamic Media with OpenAPI, the media handler returns rendition URLs pointing to the local ore remote asset instance using the [Assets Delivery API (DM API)][aem-dm-api]. From the [supported file formats][file-format-support] Dynamic Media with OpenAPI supports scaling and smart cropping for JPEG, PNG, GIF and TIFF images. All other file formats including SVG images are delivered as original binary via the Dynamic Media with OpenAPI CDN.

It's not required to configure anything in the component instance edit dialogs or content policies.


### Approval State

Publishing assets to be used via Dynamic Media with OpenAPI works differently than usual:

* Set the property **Review Status** of the asset to **Approved**
  * With this, the asset is accessible for rendering both in author and publish/live environment
  * If this status is not set, it's neither visible in Remote Asset Picker, nor can it rendered from local assets
* The Publication status of an asset is not relevant
  * However, if you render local assets via Dynamic Media with OpenAPI and the Media Handler, you have to publish the asset after it is set to Approved, so the existence and approval state can be checked on the publish instance when rendering the pages and components.


### Smart Cropping

Smart Cropping is used automatically if a media format with a specific ratio (e.g. 16:9) is used.

Media formats without any size restrictions, or e.g. only with a width restrictions, can be rendered, but are not cropped.


### Validating Assets

When rendering local assets, the existence and approval state is checked within the local content repository when resolving the media. For this reason, local assets have to be published in AEM after setting the Approval state.

For remote assets, the Media Handler assumes by default that a given remote asset reference is always valid, and supports all requested resolutions.

Optionally, you can enable the metadata service. If enabled, each time a remote asset reference is resolved, the following checks are executed:

* If the remote asset does not exist, or is not approved, the reference is handled as invalid and the component can react to it (e.g. hide the image component).
* If the requested resolution of a rendition is larger than the original resolution of the binary asset, the rendition is handled as invalid. This avoid upscaling, and avoids using an asset in a context which would result in bad image quality for the user.

See system configuration how to enable the metadata service.


### System configuration

In your project-specific implementation of `io.wcm.handler.media.spi.MediaHandlerConfig` you have to add the media sources implementation `io.wcm.handler.mediasource.ngdm.NextGenDynamicMediaMediaSource` to the list returned by the `getSources()` method (overwrite it from the superclass if required). If you want to use local assets, make sure to put it on top of the list (above the `io.wcm.handler.mediasource.dam.DamMediaSource` media source).

Example:

```java
@Component(service = MediaHandlerConfig.class)
public class MediaHandlerConfigImpl extends MediaHandlerConfig {

  private static final List<Class<? extends MediaSource>> MEDIA_SOURCES = List.of(
      NextGenDynamicMediaMediaSource.class,
      DamMediaSource.class,
      InlineMediaSource.class);

  public @NotNull List<Class<? extends MediaSource>> getSources() {
    return MEDIA_SOURCES;
  }

  // ...
}
```

With this configuration, remote assets should work out-of-the-box, if a remote asset repository is configured for the AEMaaCS instance.

The "wcm.io Dynamic Media with OpenAPI Support" OSGi configuration allows to reconfigure the actual URLs used for the [Assets Delivery API (DM API)][aem-dm-api]. Usually you can stick with the default values which reflect the latest version of the DM API. Remote assets are supported by default, but can be disabled via this configuration. Local assets are disabled by support, but can be enabled via this configuration. In this case, you also have to configure a repository ID for building the rendition URLs pointing to the AEMaaCS instance. Example:

```json
{
  "enabledLocalAssets": true,
  "localAssetsRepositoryId": "$[env:LOCAL_ASSET_DELIVERY_REPOSITORY_ID;default=]"
}
```

With this, you can configure an environment variable `LOCAL_ASSET_DELIVERY_REPOSITORY_ID` pointing to the actual host name which usually has a syntax like `delivery-pXXXXX-eXXXXX.adobeaemcloud.com` with the corresponding program and environment numbers.

The "wcm.io Dynamic Media with OpenAPI Metadata Service" allows to enable the Asset Metadata support (see above). When this is enabled, for each resolved remote asset, a HTTP request is send from the server to the DM API, so make sure this is allowed in the network infrastructure (should work by default in AEMaaCS instances). Optionally, you can configure an proxy server and timeouts.


### Known Limitations (as of June 2024)

* Dynamic Media with OpenAPI is not supported in Media Handler for AEM 6.x, only for AEMaaCS
* Same as with the Adobe AEM WCM Core Components, currently only a single remote AEM Asset instance is supported, which is configured centrally as described in [Dynamic Media with OpenAPI][aem-nextgen-dm]. The media handler uses the same convention for referencing remote assets (using strings starting with `/urn:aaid:aem:...`). This convention also does not support multiple remote AEM Asset instances, as it does not include a pointer to the Repository ID.
* If a component dialog is re-opened with a remote asset references and one of the Media Handler Granite UI widgets (e.g. pathfield), no thumbnail is displayed for the remote asset. But the reference is valid and works. The root cause is a bug/limitation in the underlying AEM pathfield component, which hopefully will be fixed soon by Adobe (SITES-19894).
* The Dynamic Media with OpenAPI remote asset picker currently ignores any folder structures for assets on the remote AEM Asset instance.
* The DM API currently does not support sending a "Content-Disposition: attachment" HTTP header for downloads. So, even if this is enforced by the Media Handler, it currently does not work for remote assets.


[aem-nextgen-dm]: https://experienceleague.adobe.com/docs/experience-manager-core-components/using/developing/next-gen-dm.html?lang=en
[aem-dm-api]: https://adobe-aem-assets-delivery-experimental.redoc.ly/
[general-concepts]: general-concepts.html
[file-format-support]: file-format-support.html
[configuration]: configuration.html
[wcm-core-components]: https://wcm.io/wcm/core-components/