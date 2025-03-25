#working directory is the root of the android project
pwd=$(shell pwd)

#default local maven deployment path
maven_repo=$(HOME)/.m2

.PHONY : build tests dockerImage clean deployLocal bash setup all

build: setup
	docker run \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle build

tests: setup
	docker run \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		run_connected_tests.sh

dockerImage:
	docker build \
		-t android-build:android-gradle \
		docker

clean:
	docker run \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle clean

deployLocal: setup
	docker run \
		-v $(pwd):/home/gradle/ \
		-v $(maven_repo):/root/.m2/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle publishToMavenLocal

publish: setup
# Check if we are in the CircleCI environment
# If not in CI env then use local.properties values to publish
# May not need this but it allows publishing from local if needed
	@if [ -z "${MAVEN_USER}" ]; then \
		docker run \
			-v $(pwd):/home/gradle/ \
			-w /home/gradle/ android-build:android-gradle \
			bash -c 'gradle clean && gradle assemble && gradle publish'; \
	else \
		echo "Running CI Publish"; \
		docker run \
			-v $(pwd):/home/gradle/ \
			-e MAVEN_USER \
			-e MAVEN_PASSWORD \
			-w /home/gradle/ android-build:android-gradle \
			bash -c 'gradle clean && gradle assemble && gradle publish'; \
	fi

bash: setup
	docker run \
		-v $(pwd):/home/gradle/ \
		-v $(maven_repo):/root/.m2/ \
		-w /home/gradle/ android-build:android-gradle \
		bash

setup: dockerImage

all: setup clean build deployLocal
