#!/bin/bash
gradle :testApp:assembleDebug
gradle :android-sdk:assembleGrpcDebugAndroidTest

gcloud config set project android-sdk-exper
gcloud auth activate-service-account \
    circleci@android-sdk-exper.iam.gserviceaccount.com \
    --key-file=/home/gradle/service-key.json --project=android-sdk-exper


gcloud firebase test android run \
    --type instrumentation \
    --device model=Pixel2.arm,version=33 \
    --app testApp/build/outputs/apk/debug/testApp-debug.apk \
    --test android-sdk/build/outputs/apk/androidTest/grpc/debug/android-sdk-grpc-debug-androidTest.apk \
    --timeout 45m
