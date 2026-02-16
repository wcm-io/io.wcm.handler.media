## File Format Support


### Supported image formats

The following image file formats are supported by wcm.io Media Handler:

| File Extension | Mime Type       | Remarks |
|----------------|-----------------|----------
| `jpg`, `jpeg`  | `image/jpeg`    |         |
| `png`          | `image/png`     |         |
| `gif`          | `image/gif`     | If rescaled or transformed, rendered as JPEG. |
| `tif`, `tiff`  | `image/tiff`    | Always rendered as JPEG. |
| `svg`          | `image/svg+xml` | Scaling of vector images is done by the browser.<br/>No support for transformations (no cropping, no rotation). |


### Supported video formats

The following video file formats are supported:

| File Extension    | Mime Type         |
|-------------------|-------------------|
| `mp4`             | `video/mp4`       |
| `mov`             | `video/quicktime` |
| `mpeg`, `mpg`     | `video/mpeg`      |
| `avi`             | `video/x-msvideo` |
| `m4v`             | `video/x-m4v`     |
| `webm`            | `video/webm`      |

#### Video delivery with Dynamic Media with OpenAPI

When using [Dynamic Media with OpenAPI][dm-openapi], video assets are delivered via adaptive streaming by default:

| Manifest Format | File Extension | Mime Type                        | Remarks |
|-----------------|----------------|----------------------------------|---------|
| HLS             | `m3u8`         | `application/vnd.apple.mpegurl`  | Default format |
| DASH            | `mpd`          | `application/dash+xml`           | Alternative format |

See [Dynamic Media with OpenAPI - Video Support][dm-openapi-video] for details.


### Unit Tests with AEM Mocks

You can use all file formats also when writing unit tests for your application with [AEM Mocks][aem-mock].

If you want to use TIFF or SVG images in your unit test, you need to define additional plugin test dependencies on your classpath, see [Java ImageIO - Advanced Image File Format Support][aem-mock-usage-imageio].



[aem-mock]: https://wcm.io/testing/aem-mock/
[aem-mock-usage-imageio]: https://wcm.io/testing/aem-mock/usage-imageio.html
[dm-openapi]: dynamic-media-openapi.html
[dm-openapi-video]: dynamic-media-openapi.html#video-support
