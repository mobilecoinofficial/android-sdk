#working directory is the root of the android project
pwd=$(shell pwd)

#default local maven deployment path
maven_repo=$(HOME)/.m2

.PHONY : build tests dockerImage clean deployLocal bash setup all publish publishDirect

build: setup
	docker run \
        --platform linux/x86_64 \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle build
	
tests: setup
	docker run \
        --platform linux/x86_64 \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		run_connected_tests.sh

dockerImage:
	docker build \
        --platform linux/x86_64 \
		-t android-build:android-gradle \
		docker

clean:
	docker run \
        --platform linux/x86_64 \
		-v $(pwd):/home/gradle/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle clean

deployLocal: setup
	docker run \
        --platform linux/x86_64 \
		-v $(pwd):/home/gradle/ \
		-v $(maven_repo):/root/.m2/ \
		-w /home/gradle/ \
		android-build:android-gradle \
		gradle publishToMavenLocal

# What publishing actually is. CI runs this directly inside the builder
# container, so the steps stay defined in one place.
publishDirect:
	gradle clean
	gradle assemble
	gradle publish

publish: setup
# Without MAVEN_USER the credentials come from local.properties, so publishing
# from a workstation works the same way.
	@if [ -z "${MAVEN_USER}" ]; then \
		docker run \
			-i \
			-v $(pwd):/home/gradle/ \
			-w /home/gradle/ android-build:android-gradle \
			make publishDirect; \
	else \
		echo "Running CI Publish"; \
		docker run \
			-i \
			-v $(pwd):/home/gradle/ \
			-e MAVEN_USER \
			-e MAVEN_PASSWORD \
			-w /home/gradle/ android-build:android-gradle \
			make publishDirect; \
	fi

bash: setup
	docker run \
		-it \
		-v $(pwd):/home/gradle/ \
		-v $(maven_repo):/root/.m2/ \
		-w /home/gradle/ android-build:android-gradle \
		bash
	
setup: dockerImage

all: setup clean build deployLocal
