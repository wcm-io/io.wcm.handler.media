## Dynamic Media with OpenAPI

wcm.io Media Handler optionally supports the [Dynamic Media with OpenAPI][aem-remote-assets] feature of AEM as a Cloud Service for:

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

If you want to give the business users more control about the actual cropping area of an image, you can create an [image profile][aem-image-profiles] in AEM, enable "Smart Crop" an assign this profile to the asset folders with the assets you want to use (the profile association is inherited to sub folders). Within the image profile, create a cropping entry with a unique name for each rendition you have defined in the media formats, or you are using dynamically for the different breakpoints when using responsive images. If you have already uploaded the assets to this folder you may need to reprocess them. Having this in place, you can use the "Smart Crop" action in the Assets UI to adjust the cropping area for individual assets.

During the media resolution process, when the media handler has detected the required renditions with their sizes and cropping to fit the output media format/ratio, it checks if named smart crops exist in the asset metadata matching for the requested aspect ratio. If this is the case, the manual adjusted cropping area is used instead of the automatic detected one (if present). To support this for remote assets, the metadata service needs to be enabled (see system configuration).


### Validating Assets

When rendering local assets, the existence and approval state is checked within the local content repository when resolving the media. For this reason, local assets have to be published in AEM after setting the Approval state.

For remote assets, validating depends on the state of the metadata service (see system configuration). If enabled, the following checks are executed during media resolution:

* If the remote asset does not exist, or is not approved, the reference is handled as invalid and the component can react to it (e.g. hide the image component).
* If the requested resolution of a rendition is larger than the original resolution of the binary asset, the rendition is handled as invalid. This avoid upscaling, and avoids using an asset in a context which would result in bad image quality for the user.

If the metadata services is not enabled, the Media Handler assumes by default that a given remote asset reference is always valid, and supports all requested resolutions.


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

By default, support for local and remote assets is disabled, because it depends on separate licensing for an AEMaaCS environment. For using remove assets, additional configuration (environment variables) are required to configure the remote repository, see AEM documentation.

The "wcm.io Dynamic Media with OpenAPI Support" OSGi configuration allows to enable the support. Additionally it is possible and to reconfigure the actual URLs used for the [Assets Delivery API (DM API)][aem-dm-api]. Usually you can stick with the default values which reflect the latest version of the DM API.

To enable support for remote Assets configure:

```json
{
  "enabledRemoteAssets": true
}
```

If you want to enable support for local assets you also have to configure a repository ID for building the rendition URLs pointing to the AEMaaCS instance. Example:

```json
{
  "enabledLocalAssets": true,
  "localAssetsRepositoryId": "$[env:LOCAL_ASSET_DELIVERY_REPOSITORY_ID;default=]"
}
```

With this, you can configure an environment variable `LOCAL_ASSET_DELIVERY_REPOSITORY_ID` pointing to the actual host name which usually has a syntax like `delivery-pXXXXX-eXXXXX.adobeaemcloud.com` with the corresponding program and environment numbers.

#### Metadata Service

The "wcm.io Dynamic Media with OpenAPI Metadata Service" allows to enable the Asset Metadata support for validation and Smart Cropping. The metadata service is enabled by default. If enabled, for each resolved remote asset a HTTP request is send from the server to the DM API to fetch the asset's metadata.

By default, Dynamic Media with OpenAPI provides only minimal metadata for each asset (dimensions, mime type). If you want access to full metadata (e.g. title, description and other properties from the asset metadata in AEM), you need to configure an IMS authentication. With that configured, the Media Handler sends an authentication token with each metadata call, which returns the full metadata and exposes it via the Media Handler API.

To enable IMS authentication for the metadata service:

1. In the [Adobe Developer Console][adobe-developer-console] create a new *empty* project
2. Rename the project to a sensible name
3. Add an API connection to the project with connection to "Cloud Manager"
    * Use "OAuth Server-to-Server" credentials
    * Ensure the name for the credentials created is not too long (shorten it if the auto-generated name is longer than 45 chars)
    * Use a minimum set of product profiles, e.g. "Integrations"
4. In the [Adobe Admin Console][adobe-admin-console] go to projects, and manage "Adobe Experience Manager as a Cloud Service"
5. Choose the instance(s) for the author environment(s) that hosts the assets to be accessed
6. Create a new profile with minimal access, or re-use an existing profile e.g. the "AEM Users" profile, and add a new entry for "API credentials". Pick the credential entry you created in step 3.
7. Define two new environment variables (_secret_) in the AEM Sites instances which is using the assets (you can use different names for the variables than shown here):
    * ASSET_DELIVERY_METADATA_AUTH_CLIENT_ID: Copy the value "Client ID" in Adobe Developer Console from the details view of the credentials created for the API
    * ASSET_DELIVERY_METADATA_AUTH_CLIENT_SECRET: From the same screen, use the button "Retrieve Client Secret" to get the secret value
8. Apply an OSGi configuration for the "wcm.io Dynamic Media with OpenAPI Metadata Service" like this:
    ```
    {
      "enabled": true,
      "authenticationClientId": "$[secret:ASSET_DELIVERY_METADATA_AUTH_CLIENT_ID;default=]",
      "authenticationClientSecret": "$[secret:ASSET_DELIVERY_METADATA_AUTH_CLIENT_SECRET;default=]"
    }
    ```



### Known Limitations (as of July 2024)

* Dynamic Media with OpenAPI is not supported in Media Handler for AEM 6.x, only for AEMaaCS
* Same as with the Adobe AEM WCM Core Components, currently only a single remote AEM Asset instance is supported, which is configured centrally as described in [Support for Remote Assets ][aem-remote-assets]. The media handler uses the same convention for referencing remote assets (using strings starting with `/urn:aaid:aem:...`). This convention also does not support multiple remote AEM Asset instances, as it does not include a pointer to the Repository ID.
* If a component dialog is re-opened with a remote asset references and one of the Media Handler Granite UI widgets (e.g. pathfield), no thumbnail is displayed for the remote asset. But the reference is valid and works. The root cause is a bug/limitation in the underlying AEM pathfield component, which hopefully will be fixed soon by Adobe (SITES-19894).
* The Dynamic Media with OpenAPI remote asset picker currently ignores any folder structures for assets on the remote AEM Asset instance.


[aem-remote-assets]: https://experienceleague.adobe.com/en/docs/experience-manager-core-components/using/developing/remote-assets
[aem-dm-api]: https://adobe-aem-assets-delivery.redoc.ly/
[general-concepts]: general-concepts.html
[file-format-support]: file-format-support.html
[wcm-core-components]: https://wcm.io/wcm/core-components/
[aem-image-profiles]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/image-profiles.html
[adobe-developer-console]: https://developer.adobe.com/console
[adobe-admin-console]: https://adminconsole.adobe.com/
