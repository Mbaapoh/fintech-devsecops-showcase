# Identity Service Makefile

.PHONY: test scan build sast

test:
	docker run --rm --volumes-from jenkins -v /var/jenkins_home/.npm:/root/.npm -e CI=true -w $(PWD) node:18 /bin/bash -c "npm install && npm test"

build:
	docker build -t fintech/identity-service:latest .

scan:
	echo "Running SCA for identity-service..."
	mkdir -p odc-reports
	docker run --rm -v $(PWD):/src \
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
