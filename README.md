# Mist Api library for Android

This repository contains the Mist Api library for Android.

## Build information

1. Change libraryVersion in build.gradle(Module: MistApi)

2. The following will build a release and push it to artifactory

$ ./gradlew --refresh-dependencies clean assembleRelease artifactoryPublish

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



