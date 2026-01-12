## Dynamic Media

wcm.io Media Handler optionally supports the [Dynamic Media][aem-dynamic-media] feature of AEM for:

* Rendering renditions including resizing and cropping
* Delivery via Dynamic Media CDN
* AI-based Smart Cropping

Only the "Scene7" mode is supported (see [Setting Up Dynamic Media][aem-dynamic-media-administration]). The old and deprecated "hybrid" mode is not supported.

The Dynamic Media support automatically gets active when the instance was set up with the additional `dynamicmedia_scene7` run mode, and the assets are replicated to the Dynamic Media servers.


### Dynamic Media concept

The integration with Dynamic Media builds on the [general concepts][general-concepts] of the Media Handler using media formats and a unified media handling API to resolve the renditions for each use case.

If Dynamic Media is active, the media handler returns rendition URLs pointing to the Dynamic Media delivery servers instead of the Servlet the renders the renditions inside the publish instance without Dynamic Media. From the [supported file formats][file-format-support] Dynamic Media supports dynamic renditions for JPEG, PNG and TIFF images. All other file formats including GIF and SVG images are delivered as original binary via the Dynamic Media CDN.

It is not required to create image profiles or image presets in AEM for the basic functionality. It's also not required to configure anything in the component instance edit dialogs or content policies.


### Smart Cropping

To enable smart cropping you need to create an [image profile][aem-image-profiles] in AEM, enable "Smart Crop" an assign this profile to the asset folders with the assets you want to use (the profile association is inherited to sub folders). Within the image profile, create a cropping entry with a unique name for each rendition you have defined in the media formats, or you are using dynamically for the different breakpoints when using responsive images. If you have already uploaded the assets to the folder before assigning the profile, or change the profile later, you may need to re-run the "DAM Update Asset" workflow on them.

During the media resolution process when the media handler has detected the required renditions with their sizes and cropping to fit the output media format/ratio, it checks if the asset has an image profile assigned, and if this profile contains and named cropping preset for the target resolution. If this is the case, the cropping preset is used displaying the pre-rendered smart cropping cutout of the original image.

See also [this video][aem-smart-crop-video] for general information about smart cropping.


### System configuration

Make sure to configure the service user mapping for Dynamic Media as described in the [system configuration][configuration].

The "wcm.io Media Handler Dynamic Media Support" OSGi configuration supports additional options:

* **Enabled**: Dynamic media support is enabled by default, if Dynamic Media is configured for the AEM instance. With this flag it is possible to disable it in the media handler.
* Author Preview Mode: If activated, Dynamic Media requests are routed through the AEM instance. Ths is useful when the "Publish Assets" configuration is not set to "Immediately", and thus images that are not yet accessible live via Dynamic Media can be previewed on author instances. Must not be activated on publish instances.
* **Disable AEM Fallback**: Disable the automatic fallback to AEM-based rendering of renditions (via Media Handler) if Dynamic Media is enabled, but the asset has not the appropriate Dynamic Media metadata. The asset is then handled as invalid.
* **Enable Downloads**: By default this is disabled and downloads are not served by Dynamic Media. Instead, all downloads are service by AEM DAM, because Dynamic Media has limitations providing access to original binaries for images. If you enable this setting, all downloads are served by Dynamic Media (although this may not work for some file types).
* **Image width/height limit**: Has to be configured to the same values as the image server "Reply Image Size Limit" in Dynamic Media.

*Backward compatibility:* If you have enabled the **Enable Downloads** flag, activate this ruleset in Dynamic Media for the images services to support serving static content for "download" (with `Content-Disposition: attachment` header):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ruleset>
  <!-- Send header for static content suffixed with '?cdh=attachment' -->
  <rule RequestType="static">
    <expression>\?cdh=attachment$</expression>
    <substitution></substitution>
    <header Name="Content-Disposition">attachment</header>
  </rule>
</ruleset>
```


[aem-dynamic-media]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/dynamic-media.html
[aem-dynamic-media-administration]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/administering-dynamic-media.html
[aem-image-profiles]: https://experienceleague.adobe.com/docs/experience-manager-65/assets/dynamic/image-profiles.html
[aem-smart-crop-video]: https://experienceleague.adobe.com/docs/experience-manager-learn/assets/dynamic-media/smart-crop-feature-video-use.html
[general-concepts]: general-concepts.html
[file-format-support]: file-format-support.html
[configuration]: configuration.html
