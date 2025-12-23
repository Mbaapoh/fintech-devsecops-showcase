# Identity Service Makefile

.PHONY: test scan build sast

CACHE_DIR ?= $(HOME)

test:
	docker run --rm -v $(PWD):/app -v $(CACHE_DIR)/.npm:/root/.npm -e CI=true -w /app node:18 /bin/bash -c "npm install && npm test -- --coverage --watchAll=false"

build:
	docker build -t fintech/identity-service:latest .

scan:
	echo "Running SCA for identity-service..."
	mkdir -p odc-reports
	chmod 777 odc-reports
	docker run --rm --user $(shell id -u):$(shell id -g) \
		-v $(PWD):/src \
		-v odc-data:/usr/share/dependency-check/data \
		-v $(PWD)/odc-reports:/report \
		owasp/dependency-check:latest \
		--project "identity-service" \
		--scan /src \
		--format "ALL" \
		--out /report \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for identity-service..."
	# Local ESLint or similar
