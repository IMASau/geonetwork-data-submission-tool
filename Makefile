

REPO=ternau
IMAGE_NAME=data-submission-tool

IMAGE=$(REPO)/$(IMAGE_NAME)

# GIT_VERSION=$(shell git show HEAD --no-patch --no-notes --date=short '--pretty=%cd-%h')
GIT_VERSION=$(shell git describe)

.PHONY: build up push check_not_dirty

build:
	docker build --build-arg GIT_VERSION=$(GIT_VERSION) -t $(IMAGE):latest .

up: build
	GIT_VERSION=$(GIT_VERSION) docker-compose -f docker-compose.yaml -f docker-compose-nginx.yaml up webapp cronapp nginx statsd

push: check_not_dirty build
	docker tag $(IMAGE):latest $(IMAGE):$(GIT_VERSION)
	docker push $(IMAGE):latest
	docker push $(IMAGE):$(GIT_VERSION)

check_not_dirty:
	@git diff --quiet || (echo "Make sure all changes are committed before bulding a release."; exit 1)
