FROM gradle:7.2-jdk11
USER root
ENV SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-6200805_latest.zip" \
    ANDROID_HOME="/usr/local/android-sdk" \
    ANDROID_VERSION=30 \
    ANDROID_BUILD_TOOLS_VERSION=30.0.2 \
    PATH="$PATH":/usr/local/bin:/usr/local/google-cloud-sdk/bin \
    GCLOUD_URL="https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-sdk-294.0.0-linux-x86_64.tar.gz"

# Download Android SDK
RUN mkdir "$ANDROID_HOME" .android \
    && cd "$ANDROID_HOME" \
    && mkdir cmdline-tools \
    && cd cmdline-tools \
    && curl -o sdk.zip $SDK_URL \
    && unzip sdk.zip \
    && rm sdk.zip \
    && mkdir "$ANDROID_HOME/licenses" || true \
    && echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > "$ANDROID_HOME/licenses/android-sdk-license" \
    && echo "y" | $ANDROID_HOME/cmdline-tools/tools/bin/sdkmanager --licenses

# Install Android Build Tool and Libraries
RUN $ANDROID_HOME/cmdline-tools/tools/bin/sdkmanager --update
RUN $ANDROID_HOME/cmdline-tools/tools/bin/sdkmanager "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "platforms;android-${ANDROID_VERSION}" \
    "platform-tools"
    
# Install Build Essentials
RUN apt-get update && apt-get install build-essential file apt-utils -y

#Download and install gcloud to access the Firebase testing cloud
RUN curl -o gcloud.tar.gz "$GCLOUD_URL" \
    && tar xvf gcloud.tar.gz -C /usr/local/ \
    && /usr/local/google-cloud-sdk/install.sh

# Copy scripts
COPY scripts/* /usr/local/bin/

