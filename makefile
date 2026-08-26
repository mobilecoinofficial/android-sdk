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

publishDirect:
	gradle clean
	gradle assemble
	gradle publish

publish: setup
# Docker omits an unset variable, so the same target serves CI and a workstation.
	docker run \
		-i \
		-v $(pwd):/home/gradle/ \
		-e MAVEN_USER \
		-e MAVEN_PASSWORD \
		-w /home/gradle/ android-build:android-gradle \
		make publishDirect

bash: setup
	docker run \
		-it \
		-v $(pwd):/home/gradle/ \
		-v $(maven_repo):/root/.m2/ \
		-w /home/gradle/ android-build:android-gradle \
		bash
	
setup: dockerImage

all: setup clean build deployLocal
