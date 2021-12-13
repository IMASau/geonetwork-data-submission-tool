
# if BITBUCKET_CLONE_DIR is not set use HOME (it is set during bb pipeline run)
BITBUCKET_CLONE_DIR ?= $(HOME)

.PHONY: clean figwheel watch build copy-static release storybook

clean:
	rm -fR src/gen/* resources/public/js/*

# for backward compatibility
figwheel: watch

watch:
	# init yarn
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn
	# run shadow-cljs watch
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $(HOME)/.clojure:/root/.clojure \
		-v $(HOME)/.m2:/root/.m2 \
		-p 127.0.0.1:9500:9500 \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn shadow-cljs watch tern

build:
	# init yarn
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn
	# transpile js components
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn run build
	# run shadow-cljs compile
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $${HOME}/.clojure:/root/.clojure \
		-v $${HOME}/.m2:/root/.m2 \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn shadow-cljs compile tern

copy-static-pipeline:
	docker run --rm \
	  -v $(shell pwd):/tmp \
	  -w tmp \
	  theasp/clojurescript-nodejs:shadow-cljs-alpine \
	  npx sass --load-path=. --style compressed src/js/index.scss resources/public/css/style.npm-packaged.css
	mkdir -p resources/public/resources/icons resources/public/css/images
	cp -fR node_modules/@blueprintjs/icons/resources/icons resources/public/resources/icons
	cp -vr node_modules/leaflet-draw/dist/images/* resources/public/css/images

copy-static:
	npx sass --load-path=. --style compressed src/js/index.scss resources/public/css/style.npm-packaged.css
	mkdir -p resources/public/resources/icons resources/public/css/images
	cp -fR node_modules/@blueprintjs/icons/resources/icons resources/public/resources/icons
	cp -vr node_modules/leaflet-draw/dist/images/* resources/public/css/images

release:
	# init yarn
	docker run --rm \
		-v $(shell pwd):/tmp \
		-w /tmp \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn
	# transpile js components
	docker run --rm \
		-v $(shell pwd):/tmp \
		-w /tmp \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn run build
	# run shadow-cljs release
	docker run --rm \
		-v $(shell pwd):/tmp \
		-v $(BITBUCKET_CLONE_DIR)/.clojure:/root/.clojure \
		-v $(BITBUCKET_CLONE_DIR)/.m2:/root/.m2 \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn shadow-cljs release tern
