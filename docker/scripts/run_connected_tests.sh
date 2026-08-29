#!/bin/bash
gradle :testApp:assembleDebug
gradle :android-sdk:assembleGrpcDebugAndroidTest

gcloud config set project android-sdk-exper
gcloud auth activate-service-account \
    --key-file=/home/gradle/service-key.json --project=android-sdk-exper

APP=testApp/build/outputs/apk/debug/testApp-debug.apk
TEST=android-sdk/build/outputs/apk/androidTest/grpc/debug/android-sdk-grpc-debug-androidTest.apk

# gcloud prints pass/fail counts and nothing else, so every message lands in
# the GCS bucket it names on the way in. Print the JUnit results here instead
# of leaving someone to click through to the bucket.
report_results() {
    local results
    results=$(sed -n 's#.*storage/browser/\([^]]*\)\].*#\1#p' "$1" | head -1)
    if [ -z "$results" ]; then
        echo "No GCS results path in the gcloud output; nothing to report."
        return
    fi

    local xml
    xml=$(gcloud storage ls "gs://${results%/}/**/test_result_*.xml" 2>/dev/null)
    if [ -z "$xml" ]; then
        echo "No JUnit results under gs://$results"
        return
    fi
    # GitHub Actions renders ANSI, so paint the failures red — the XML is
    # mostly passing testcases and the failures are what anyone is looking for.
    echo "$xml" | while read -r file; do
        echo "--- $file"
        gcloud storage cat "$file" \
            | sed $'s/<failure/\033[31m<failure/; s#</failure>#</failure>\033[0m#'
    done
}

gcloud firebase test android run \
    --type instrumentation \
    --device model=Pixel2.arm,version=33 \
    --app "$APP" \
    --test "$TEST" \
    --timeout 45m 2>&1 | tee /tmp/ftl-tests.log
status=${PIPESTATUS[0]}

if [ "$status" -ne 0 ]; then
    echo "=== Test failures ==="
    report_results /tmp/ftl-tests.log
fi

exit "$status"
