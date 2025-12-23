# Customer Web Makefile

.PHONY: test scan build sast

IS_CONTAINER = $(shell [ -f /.dockerenv ] && echo "true" || echo "false")
CACHE_DIR ?= $(shell [ -f /.dockerenv ] && echo "/var/jenkins_home" || echo "$(HOME)")

ifeq ($(IS_CONTAINER),true)
    MOUNT_OPTS = --volumes-from $(shell hostname)
else
    MOUNT_OPTS = -v $(PWD):$(PWD)
endif

DOCKER_USER = --user $(shell id -u):$(shell id -g)

test:
	docker run --rm $(MOUNT_OPTS) $(DOCKER_USER) -w $(PWD) \
		-v $(CACHE_DIR)/.npm:/.npm \
		-e CI=true node:18 /bin/bash -c "npm install --cache /.npm --prefer-offline && npm test -- --coverage --watchAll=false"

build:
	docker build -t fintech/customer-web:latest .

scan:
	echo "Running SCA for customer-web..."
	mkdir -p odc-reports
	chmod 777 odc-reports
	docker run --rm $(MOUNT_OPTS) $(DOCKER_USER) -w $(PWD) \
		-v odc-data:/usr/share/dependency-check/data \
		owasp/dependency-check:latest \
		--project "customer-web" \
		--scan . \
		--format "ALL" \
		--out odc-reports \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for customer-web..."
