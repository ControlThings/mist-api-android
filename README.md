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

## Build information

Build only the MistApi module, since the 'app' module has some test code that does not seem to be up to date.

./gradlew :MistApi:assembleRelease

### clean build

Note: this attempts to build all modules.

$ ./gradlew --refresh-dependencies clean assembleRelease 

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



