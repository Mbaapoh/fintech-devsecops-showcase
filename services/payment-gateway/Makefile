# Payment Gateway Makefile

.PHONY: test scan build sast

# Detect environment
IS_CONTAINER = $(shell [ -f /.dockerenv ] && echo "true" || echo "false")
CACHE_DIR ?= $(shell [ -f /.dockerenv ] && echo "/var/jenkins_home" || echo "$(HOME)")

# Strategy for mounting workspace (Host vs DooD)
ifeq ($(IS_CONTAINER),true)
    # Inside Docker: Bridge volumes from the agent container
    MOUNT_OPTS = --volumes-from $(shell hostname)
else
    # Bare Metal: Standard volume mount
    MOUNT_OPTS = -v $(PWD):$(PWD)
endif

# Run as current user to avoid permission locks
DOCKER_USER = --user $(shell id -u):$(shell id -g)

test:
	docker run --rm $(MOUNT_OPTS) $(DOCKER_USER) -w $(PWD) \
		maven:3.9.3-eclipse-temurin-17 \
		mvn clean test jacoco:report -Dmaven.repo.local=$(CACHE_DIR)/.m2/repository

build:
	docker build -t fintech/payment-gateway:latest .

scan:
	echo "Running SCA for payment-gateway..."
	mkdir -p odc-reports
	chmod 777 odc-reports
	docker run --rm $(MOUNT_OPTS) $(DOCKER_USER) -w $(PWD) \
		-v odc-data:/usr/share/dependency-check/data \
		owasp/dependency-check:latest \
		--project "payment-gateway" \
		--scan . \
		--format "ALL" \
		--out odc-reports \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for payment-gateway..."
	# Local Sonar scanner or linter
