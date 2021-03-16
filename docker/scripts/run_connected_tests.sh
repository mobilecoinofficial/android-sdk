#!/bin/bash
gradle :testApp:assembleDebug
gradle :android-sdk:assembleDebugAndroidTest

gcloud config set project mobilecoin-60c
gcloud auth activate-service-account \
    ci-service-account@mobilecoin-60c.iam.gserviceaccount.com \
    --key-file=/home/gradle/service-key.json --project=mobilecoin-60c

gcloud firebase test android run \
    --type instrumentation \
    --device model=Nexus5X,version=24 \
    --app testApp/build/outputs/apk/debug/testApp-debug.apk \
    --test android-sdk/build/outputs/apk/androidTest/debug/android-sdk-debug-androidTest.apk
