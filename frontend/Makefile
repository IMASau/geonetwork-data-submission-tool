clean:
	rm -fR resources/public/js/*

figwheel:
	clojure -m figwheel.main -co dev.cljs.edn -r

build:
	yarn
	yarn webpack --mode development
	clojure -m cljs.main -co dev.cljs.edn -c

copy-css:
	cp node_modules/normalize.css/normalize.css resources/public/css
	cp node_modules/@blueprintjs/core/lib/css/blueprint.css resources/public/css
	cp node_modules/@blueprintjs/datetime/lib/css/blueprint-datetime.css resources/public/css
	cp node_modules/@blueprintjs/table/lib/css/table.css resources/public/css
	cp node_modules/@blueprintjs/icons/lib/css/blueprint-icons.css resources/public/css
	cp node_modules/@blueprintjs/select/lib/css/blueprint-select.css resources/public/css
	cp node_modules/fixed-data-table-2/dist/fixed-data-table.css resources/public/css

release:
	yarn
	yarn webpack --mode production
	clojure -m cljs.main -co prod.cljs.edn -O advanced -c
