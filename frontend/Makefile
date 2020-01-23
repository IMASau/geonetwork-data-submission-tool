clean:
	rm -fR resources/public/js/*

figwheel:
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $(HOME)/.clojure:/root/.clojure \
		-v $(HOME)/.m2:/root/.m2 \
		clojure:openjdk-14-tools-deps-buster \
		clojure -m figwheel.main -co dev.cljs.edn -r

build:
	# init yarn
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn
	# run webpack
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn webpack --mode development
	# run clojure compiler
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $${HOME}/.clojure:/root/.clojure \
		-v $${HOME}.m2:/root/.m2 \
		clojure:openjdk-14-tools-deps-buster \
		clojure -m cljs.main -co dev.cljs.edn -c

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
		node:13-alpine \
		yarn
	# run webpack
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn webpack --mode production
	# run clojure compiler
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $(HOME)/.clojure:/root/.clojure \
		-v $(HOME)/.m2:/root/.m2 \
		clojure:openjdk-14-tools-deps-buster \
		clojure -m cljs.main -co prod.cljs.edn -O advanced -c
