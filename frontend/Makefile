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

copy-static:
	cp node_modules/normalize.css/normalize.css resources/public/css
	cp node_modules/@blueprintjs/core/lib/css/blueprint.css resources/public/css
	cp node_modules/@blueprintjs/core/lib/css/blueprint.css.map resources/public/css
	cp node_modules/@blueprintjs/datetime/lib/css/blueprint-datetime.css resources/public/css
	cp node_modules/@blueprintjs/datetime/lib/css/blueprint-datetime.css.map resources/public/css
	cp node_modules/@blueprintjs/table/lib/css/table.css resources/public/css
	cp node_modules/@blueprintjs/table/lib/css/table.css.map resources/public/css
	cp node_modules/@blueprintjs/icons/lib/css/blueprint-icons.css resources/public/css
	cp node_modules/@blueprintjs/icons/lib/css/blueprint-icons.css.map resources/public/css
	cp node_modules/@blueprintjs/select/lib/css/blueprint-select.css resources/public/css
	cp node_modules/@blueprintjs/select/lib/css/blueprint-select.css.map resources/public/css
	cp node_modules/fixed-data-table-2/dist/fixed-data-table.css resources/public/css
	cp -v node_modules/leaflet/dist/leaflet.css resources/public/css
	cp -vr node_modules/leaflet-draw/dist/* resources/public/css

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
