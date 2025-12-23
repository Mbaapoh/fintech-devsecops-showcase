# Customer Web Makefile

.PHONY: test scan build sast

CACHE_DIR ?= $(HOME)

test:
	docker run --rm -v $(PWD):/app -v $(CACHE_DIR)/.npm:/root/.npm -e CI=true -w /app node:18 /bin/bash -c "npm install && npm test -- --coverage --watchAll=false"

build:
	docker build -t fintech/customer-web:latest .

scan:
	echo "Running SCA for customer-web..."
	mkdir -p odc-reports
	docker run --rm -v $(PWD):/src \
		-v odc-data:/usr/share/dependency-check/data \
		-v $(PWD)/odc-reports:/report \
		owasp/dependency-check:latest \
		--project "customer-web" \
		--scan /src \
		--format "ALL" \
		--out /report \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for customer-web..."
	# Local ESLint or similar
