# Payment Gateway Makefile

.PHONY: test scan build sast

test:
	docker run --rm --volumes-from jenkins -v /var/jenkins_home/.m2:/root/.m2 -w $(PWD) maven:3.9.3-eclipse-temurin-17 mvn clean test jacoco:report

build:
	docker build -t fintech/payment-gateway:latest .

scan:
	echo "Running SCA for payment-gateway..."
	mkdir -p odc-reports
	docker run --rm -v $(PWD):/src \
		-v odc-data:/usr/share/dependency-check/data \
		-v $(PWD)/odc-reports:/report \
		owasp/dependency-check:latest \
		--project "payment-gateway" \
		--scan /src \
		--format "ALL" \
		--out /report \
		--disableKnownExploited \
		--nvdApiKey "$(NVD_API_KEY)"

sast:
	echo "Running SAST for payment-gateway..."
	# Local Sonar scanner or linter
