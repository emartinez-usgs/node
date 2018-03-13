Node
====

Builds a basic Node.js image based on USGS CentOS image for use throughout
the USGS. Image tags refer to the major release version of Node installed
on that image.

Node installation is done using NVM so even if a specific version of Node is
not available, it is easily obtainable through the [NVM utility][1].


Build Image
-----------

A Dockerfile is provided to simplify image building. Most basically one may
use the default image like so...

```
$ cd <PROJECT_ROOT>
$ docker build -t <TAG_NAME> .
```

> Here `<PROJECT_ROOT>` and `<TAG_NAME>` should be replaced with values appropriate
> to ones local environment and desired generated tag.

By defeault the current Node LTS is installed. The Dockerfile also supports
Node versions with the `NODE_VERSION` build argument. Furthermore, if one
chooses to not use the default USGS CentOS base image, that is also an option
with the `FROM_IMAGE` build agrument.

```
$ cd <PROJECT_ROOT>
$ docker build \
  --build-arg FROM_IMAGE=<OTHER_BASE_IMAGE>
  --build-arg NODE_VERSION=<MAJOR_NODE_VERSION> \
  -t <TAG_NAME> \
  .

```
> Note: Using a different base image may not work as expected. Other base
>       images should be similar to CentOS.

[1] https://github.com/creationix/nvm
