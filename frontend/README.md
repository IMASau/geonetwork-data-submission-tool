# geonetwork-data-submission-tool single page webapp 


## Development

From the command-line 

```sh
lein figwheel
```

When you see the line `Successfully compiled "resources/public/dev.js" in 21.36 seconds.`, you're ready to go. Changes to CSS and CLJS files will be pushed to the browser.  No need to refresh.

To remote debug the live applciation open a nREPL connection.

For example, from Cursive Clojure create a remote nREPL connection to localhost:7888 and bootstrap connection with:

```clojure
(use 'figwheel-sidecar.repl-api)
(cljs-repl)
```

Now you have a live REPL connection to the live browser code.


## Building for deployment

``` sh
lein with-profile -dev,+uberjar cljsbuild once 
```

