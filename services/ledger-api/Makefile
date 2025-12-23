# Ledger API Makefile

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
		-e GOCACHE=$(CACHE_DIR)/.go-cache \
		golang:1.21 go test ./... -coverprofile=coverage.out

build:
	docker build -t fintech/ledger-api:latest .

scan:
	echo "Running SCA for ledger-api..."
	mkdir -p odc-reports
	chmod 777 odc-reports
	docker run --rm $(MOUNT_OPTS) $(DOCKER_USER) -w $(PWD) \
		-v odc-data:/usr/share/dependency-check/data \
		owasp/dependency-check:latest \
		--project "ledger-api" \
		--scan . \
		--format "ALL" \
		--out odc-reports \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for ledger-api..."
