# Ledger API Makefile

.PHONY: test scan build sast

CACHE_DIR ?= $(HOME)

test:
	docker run --rm -v $(PWD):/app -v $(CACHE_DIR)/go-cache:/go -w /app golang:1.21 go test ./... -coverprofile=coverage.out

build:
	docker build -t fintech/ledger-api:latest .

scan:
	echo "Running SCA for ledger-api..."
	mkdir -p odc-reports
	docker run --rm -v $(PWD):/src \
		-v odc-data:/usr/share/dependency-check/data \
		-v $(PWD)/odc-reports:/report \
		owasp/dependency-check:latest \
		--project "ledger-api" \
		--scan /src \
		--format "ALL" \
		--out /report \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for ledger-api..."
	# Local Go linter (golangci-lint)
