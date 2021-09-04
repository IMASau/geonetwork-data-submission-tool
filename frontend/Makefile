clean:
	rm -fR src/gen/* resources/public/js/*

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

copy-static:
    sass --load-path=. --style compressed src/js/index.scss resources/public/css/style.npm-packaged.css
    cp -fR node_modules/@blueprintjs/icons/resources/icons resources/public/resources/icons
    cp -vr node_modules/leaflet-draw/dist/images/* resources/public/css/images

release:
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
	# run shadow-cljs release
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $(HOME)/.clojure:/root/.clojure \
		-v $(HOME)/.m2:/root/.m2 \
		theasp/clojurescript-nodejs:shadow-cljs-alpine \
		yarn shadow-cljs release tern
