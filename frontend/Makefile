clean:
	rm -fR resources/public/js/*

figwheel:
	clojure -m figwheel.main -co dev.cljs.edn -r

build:
	yarn
	yarn webpack --mode development
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
	yarn
	yarn webpack --mode production
	clojure -m cljs.main -co prod.cljs.edn -O advanced -c

# TODO: make dockerised builds the default some day
d_build:
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn

	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn webpack --mode development

	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $${HOME}/.clojure:/root/.clojure \
		-v $${HOME}.m2:/root/.m2 \
		clojure:openjdk-14-tools-deps-buster \
		clojure -m cljs.main -co dev.cljs.edn -c

d_release:
	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn

	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-w /tmp \
		node:13-alpine \
		yarn webpack --mode production

	docker run --rm -it \
		-v $(shell pwd):/tmp \
		-v $(HOME)/.clojure:/root/.clojure \
		-v $(HOME)/.m2:/root/.m2 \
		clojure:openjdk-14-tools-deps-buster \
		clojure -m cljs.main -co prod.cljs.edn -O advanced -c
