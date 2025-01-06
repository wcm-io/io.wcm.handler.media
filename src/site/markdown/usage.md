## Media Handler Usage


### Building media

The [Media Handler][media-handler] is a Sling Model and can be adapted either from a request or a resource. It automatically reads the context-specific configuration for the Site URLs of the [URL Handler][url-handler] based on the resource path of the current request or the path of the resource adapted from.

Example:

```java
MediaHandler mediaHandler = request.adaptTo(MediaHandler.class);

// build media referenced in current resource
Media media = mediaHandler.get(resource).build();

// build media referenced by path with a specific media format
Media media = mediaHandler.get("/content/dam/sample/asset1.jpg").mediaFormat(MediaFormats.MF_16_9).build();

// check if media is valid and get markup
if (media.isValid()) {
  String markup = media.getMarkup();
  // ...
}
```

Alternatively you can inject the `MediaHandler` into your Sling Model using the `@Self` annotation if the model itself adapts from request or resource.

The media handler uses a "builder pattern" so you can flexibly combine the different media generation options.
See [MediaBuilder][media-builder] for all options.


### Media properties in resource

When referencing a media in a resource multiple properties are used to describe the media reference. Some of the properties depend on the media source implementation.

By default, the Media Handler uses it's own set of properties (prefixed with "media"). For new projects it is recommended to switch to the "Adobe Standard" properties which are also by other image components.

These are the most common properties:

|Property name<br/>(wcm.io Default) |Property name<br/> (Adobe Standard)|Description
|-----------------------------------|-----------------------------------|-----------------------------------------------|
| `mediaRef`                        | `fileReference`                   | Reference/path to the media asset
| `mediaCrop`                       | `imageCrop`                       | Cropping parameters for image
| `mediaRotation`                   | `imageRotation`                   | Rotation parameter for image
| `mediaMap`                        | `imageMap`                        | Image map string
| `mediaAltText`                    | `alt`                             | Alternative text for media
| `mediaInline`                     | `file`                            | Node name for binary file uploaded to a page

Further properties are defined in [MediaNameConstants][media-name-constants].

To switch to "Adobe Standard" names add this class to your project (recommended):

```java
@Component(service = MediaHandlerConfig.class)
public class MediaHandlerConfigImpl extends MediaHandlerConfig {

  @Override
  public boolean useAdobeStandardNames() {
    // use standard name for asset references as used by the core components
    return true;
  }

}
```


### Media formats

Media formats describe expected output formats of media assets or images. They are defined as constants using a builder pattern. The most simple type of media format is defining an image with a  fixed dimension:

```java
public static final MediaFormat CONTENT_480 = MediaFormatBuilder.create("content_480")
    .label("Content Standard")
    .fixedDimension(480, 270)
    .extensions("gif", "jpg", "png")
    .build();
```

It is also possible to define a format which matches certain min/max-sizes and a ratio:

```java
public static final MediaFormat MF_16_9 = create("mf_16_9")
    .label("16:9 Image")
    .minWidth(1000)
    .minHeight(500)
    .ratio(2.0d)
    .extensions("gif", "jpg", "png")
    .build();
```

Or a media format defining downloads only restricting by file extensions:

```java
public static final MediaFormat DOWNLOAD = create("download")
    .label("Download")
    .extensions("pdf", "zip", "ppt", "pptx", "doc", "docx")
    .download(true)
    .build();
```

These media format definitions have to be provided to the media handling using an OSGi service extending the [MediaFormatProvider][media-format-provider] class. The default implementation supports extracting the defined formats from the public static fields of a class. Via [Context-Aware Services][sling-commons-caservices] you can make sure the media formats affect only resources (content pages, DAM assets) that are relevant for your application. Thus it is possible to provide different media formats for different applications running in the same AEM instance.

When resolving a media reference it is possible to specify one or multiple media formats. If the media asset contains a rendition that exactly matches the format it is returned. If it contains a rendition that is bigger but has the requested ratio a dynamically downscaled rendition is returned. If cropping parameters are defined they are applied before checking against the media format. If no rendition matches or can be rescaled the resolving process failed and the returned media is invalid. In Edit Mode `DummyImageMediaMarkupBuilder` is then used to render a drop area instead to which an DAM asset can be assigned via drag&drop.

To resolve multiple renditions at once for a responsive image it is possible to specify a list of mandatory media formats:

```java
MediaHandler mediaHandler = request.adaptTo(MediaHandler.class);

// build media with a list of mandatory media formats
Media media = mediaHandler.get(resource)
    .mandatoryMediaFormats(MediaFormats.FORMAT1, MediaFormats.FORMAT2).build();

// get all renditions
if (media.isValid()) {
  for (Rendition rendition : renditions) {
    // check rendition
  }
}
```

A custom markup builder can then generated the image tag with metadata for all breakpoints (depending on the frontend solution).


### Using media in HTL/Sightly template

To resolve a media inside a HTL template you can use a generic Sling Model for calling the handler: [ResourceMedia](apidocs/io/wcm/handler/media/ui/ResourceMedia.html)

HTL template example:

```html
<sly data-sly-use.media="${'io.wcm.handler.media.ui.ResourceMedia' @ mediaFormat='content'}"
    data-sly-test="${media.valid}">
  ${media.markup @ context='unsafe'}
</sly>
<sly data-sly-use.template="wcm-io/handler/media/components/placeholder/mediaPlaceholder.html"
    data-sly-call="${template.placeholder @ isEmpty=!media.valid, media=media.metadata}"></sly>
```

In this case the `${media.markup ...}` is replaced with the media markup of the media handler, which is not necessarily is an `img` element, but may be any markup (e.g. a `picture` or `video` or `div` element with custom markup).

The HTL template library `wcm-io/handler/media/components/placeholder/mediaPlaceholder.html` provides a "Media Handler-aware" version placeholder that is displayed for an image component when no or no valid asset is referenced. If an asset is referenced that does not match with the expected media formats an additional message is displayed in the placeholder.

A word on the `unsafe` context, which normally should  by avoided in HTL: Any HTML element that is unknown (unconfigured) in the AEM/Sling default XSS settings is stripped out when using the `html` context. Unfortunately this affects also modern HTML5 responsive image markup like `sizes` and `srcset` attributes, or the `picture` and `source` elements. So you either have to reconfigure the XSS setting of your AEM instance, or us the `unsafe` mode. In this case `unsafe` can be considered safe, because the media handler does not return any markup that can be entered by a user or injected in other ways, but only the markup it produces itself.


### Cropping and Image Rotation

The Media Handler supports manual cropping and image rotation using the AEM built-in in-place editor for images. This stores cropping and rotation parameters in two additional properties in the resource, which are respected by the Media Handler and are applied before the media format selection process takes place.

Additionally it is possible to activate "auto-cropping" for components. If an asset is referenced which ratio does not match with the required ratio of the media format is is cropped automatically to the requested ratio when auto-cropping is activated. To activate auto, you can either set the flag `autoCrop(true)` in the MediaArgs, or set the [component property][component-properties] `wcmio:mediaCropAuto` to `true` for the image component. It is possible to combine auto-cropping and manual cropping - in this case manually defined cropping parameters have higher precedence than auto-cropping.

If the expected media formats for a component are defined via [component properties][component-properties], these media formats are also used to customize the list of cropping ratios that are available in the in-place editor for images. This auto-generated list of cropping ratios is displayed by default when there is not custom list for cropping ratios defined for the image editor in the component definition.


### Configuring and tailoring the media resolving process

Optionally you can provide an OSGi service to specify in more detail the media resolving needs of your application. For this you have to extend the [MediaHandlerConfig][media-handler-config] class. Via [Context-Aware Services][sling-commons-caservices] you can make sure the SPI customization affects only resources (content pages, DAM assets) that are relevant for your application. Thus it is possible to provide different customizations for different applications running in the same AEM instance.

With this you can:

* Define which media sources are supported by your application or include your own ones
* Define which markup builders are supported by your application or include your own ones
* Define custom pre- and postprocessors that are called before and after the media resolving takes place
* Implement a method which returns the default quality when writing images with lossy compression

Example:

```java
@Component(service = MediaHandlerConfig.class, property = {
    ContextAwareService.PROPERTY_CONTEXT_PATH_PATTERN + "=^/content/(dam/)?myapp(/.*)?$"
})
public class MediaHandlerConfigImpl extends MediaHandlerConfig {

  private static final List<Class<? extends MediaSource>> MEDIA_SOURCES =
      ImmutableList.<Class<? extends MediaSource>>of(
          DamMediaSource.class,
          InlineMediaSource.class
      );

  private static final List<Class<? extends MediaMarkupBuilder>> MEDIA_MARKUP_BUILDERS =
      ImmutableList.<Class<? extends MediaMarkupBuilder>>of(
          SimpleImageMediaMarkupBuilder.class,
          DummyImageMediaMarkupBuilder.class
      );

  @Override
  public List<Class<? extends MediaSource>> getSources() {
    return MEDIA_SOURCES;
  }

  @Override
  public List<Class<? extends MediaMarkupBuilder>> getMarkupBuilders() {
    return MEDIA_MARKUP_BUILDERS;
  }

}
```

Schematic flow of media handling process:

1. Start media handler processing
2. Detect media source, store result in media request
3. Apply preprocessors on media request
4. Resolve media using media source, store result in media request
5. Generate markup using markup builder, store result in media request
6. Apply postprocessors on media request


### Responsive Images

In a responsive web project there is often the need to show images with same ratio but different resolutions depending on the target device, screen size and pixel depth. The Media Handler helps you on this with special markup builders.

Example - define a media format with a certain ratio and minimum sizes (big enough for for the largest resolution on the website):

```
public static final MediaFormat MF_16_9 = MediaFormatBuilder.create("mf_16_9")
    .label("Media 16:9")
    .minWidth(1600)
    .minHeight(900)
    .ratio(16, 9)
    .extensions("jpg", "jpeg", "png")
    .build();
```

Your can build responsive images with a single ratio and different image sizes:

```java
Media media = mediaHandler.get(resource)
    .mediaFormat(MF_16_9)
    .imageSizes("(min-width: 1280px) 1200px, 100vw", 1600, 1200, 800)
    .build();
```

This results in a markup like this:

```html
<img src="/path/mymedia.jpg"
    sizes="(min-width: 1280px) 1200px, 100vw"
    srcset="/path/mymedia.jpg 1600w, /path/mymedia.1200.759.jpg 1200w, /path/mymedia.800.500.jpg 800w">
```

Alternatively you can generate a picture element with sources if you need different ratios for different breakpoints:

```java
Media media = mediaHandler.get(resource)
    .mediaFormat(MF_16_9)
    .pictureSource(new PictureSource(MF_16_9).media("(min-width: 1024px)").widths(1600, 1200, 800))
    .pictureSource(new PictureSource(MF_4_3).widths(800, 400))
    .build();
```

This results in a markup like this:

```html
<picture>
  <source media="(min-width: 1024px)" srcset="/path/mymedia.jpg 1600w, /path/mymedia.1200.759.jpg 1200w, /path/mymedia.800.500.jpg 800w">
  <source srcset="/path/mymedia.800.600.jpg 800w, /path/mymedia.400.300.jpg 400w">
  <img src="/path/mymedia.jpg">
</picture>
```

#### Using density descriptors
By default `srcset` attribute is generated using width descriptors, e.g. `800w`. This fits well when you want to
use different sizes of image in different media conditions. But if you prefer using same size
with different pixel densities depending on the client's device capabilities, you should use density descriptors instead. 
For example `2x` for retina displays. Density descriptors cannot be used together with `sizes`.

```java
import io.wcm.handler.media.MediaArgs;

Media media = mediaHandler.get(resource)
        .mediaFormat(MF_16_9)
        .imageSizes("", new WidthOption(800, "1x"), new WidthOption(1600, "2x"))
        .build();
```
This results in a markup like this:

```html
<img src="/path/mymedia.jpg"
    srcset="/path/mymedia.800.500.jpg, /path/mymedia.jpg 2x">
```

Alternatively you can generate a picture element:

```java
Media media = mediaHandler.get(resource)
    .mediaFormat(MF_16_9)
    .pictureSource(new PictureSource(MF_16_9).media("(min-width: 1024px)").widthOptions(new WidthOption(800, "1x"), new WidthOption(1600, "2x")))
    .pictureSource(new PictureSource(MF_4_3).widthOptions(new WidthOption(400), new WidthOption(800, "2x")))
    .build();
```

This results in a markup like this:

```html
<picture>
  <source media="(min-width: 1024px)" srcset="/path/mymedia.800.500.jpg, /path/mymedia.jpg 2x">
  <source srcset="/path/mymedia.400.300.jpg, /path/mymedia.800.600.jpg 2x">
  <img src="/path/mymedia.jpg">
</picture>
```


[media-handler]: apidocs/io/wcm/handler/media/MediaHandler.html
[media-builder]: apidocs/io/wcm/handler/media/MediaBuilder.html
[media-name-constants]: apidocs/io/wcm/handler/media/MediaNameConstants.html
[media-handler-config]: apidocs/io/wcm/handler/media/spi/MediaHandlerConfig.html
[media-format-provider]: apidocs/io/wcm/handler/media/spi/MediaFormatProvider.html
[url-handler]: ../url/
[sling-commons-caservices]: ../../sling/commons/context-aware-services.html
[component-properties]: component-properties.html
