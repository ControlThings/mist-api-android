# Mist Api library for Android

This repository contains the Mist Api library for Android.

## Build

### Before building

Get git submodules

```
git submodule update --init --recursive
```

You will need to point to Android SDK and NDK in a `local.properties`, like this:

```  
ndk.dir=/home/jan/Android/Sdk/ndk/16.1.4479499
sdk.dir=/home/jan/Android/Sdk
```

Note: The only tested ndk version 16.1.4479499

### Setting up Github packages

The `wishcore` dependency is distributed via github packages, so this step is required for building MistApi.

Create github.properties in repo root, and add to .gitignore; 

```
gpr.usr=
gpr.key=
```

Then visit this page to get your Github id: https://api.github.com/users/<username> like https://api.github.com/users/janyman

Take "id" field, and insert into `github.properties` as `grp.usr`.

Then create a publishing key for you, by visinting https://github.com/settings/tokens
Create a token that has "package write" permission, cut&paste into `github.properties` as `grp.key`

### Build and publish artifact

1. Tag release

```
git tag 0.9.1
```

`MistApi/build.gradle` has the magic for setting artifact version according to latest git tag.

2. Build only the MistApi module, since the 'app' module has some test code that does not seem to be up to date.

```
./gradlew :MistApi:assembleRelease
```

NOTE: java-8-openjdk, openjdk version "1.8.0_342", use `sudo  update-alternatives --config java` to select on Ubuntu

Doing a clean build:

Note: this attempts to build all modules. TODO: fix `app` module.

```
$ ./gradlew --refresh-dependencies clean assembleRelease 
```

3. Publish the 'aar' artifact

This publishes to Github packages:

```
./gradlew publish 
```

The publishing is defined in `app/build.gradle` and requires that you have Github packages set up.



## Debugging the native code

Edit your Android.mk so that you do not define -DRELEASE_BUILD and that
you have -O0 -g

Build with:

NDK_DEBUG=1 ndk-build

Then build the project "assembleDebug" with Gradle

Then you can move the resulting aar file MistApi-<version-git-tag>-debug.aar

To get stack traces you should use adb logcat and ndk-stack:

  adb logcat | ndk-stack -sym ../obj/local/armeabi-v7a/

Or another example

  adb logcat|~/Android/android-ndk-r16b/ndk-stack -sym MistApi/src/main/obj/local/armeabi-v7a/



