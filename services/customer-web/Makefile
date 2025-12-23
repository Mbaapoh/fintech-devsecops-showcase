# Customer Web Makefile

.PHONY: test scan build sast

test:
	docker run --rm --volumes-from jenkins -v /var/jenkins_home/.npm:/root/.npm -e CI=true -w $(PWD) node:18 /bin/bash -c "npm install && npm test -- --coverage --watchAll=false"

build:
	docker build -t fintech/customer-web:latest .

scan:
	echo "Running SCA for customer-web..."
	docker run --rm -v $(PWD):/src -v $(PWD)/odc-reports:/report owasp/dependency-check:latest --project "customer-web" --scan /src --format "ALL" --out /report

sast:
	echo "Running SAST for customer-web..."
	# Local ESLint or similar
