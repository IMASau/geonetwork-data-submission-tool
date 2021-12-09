
Data Submission Tool
====================

Getting started
---------------

To set up development environment clone this repo and create a .env file.

.. code:: bash

    cp env.template .env


Modify values in .env before continuing.

List of important variables:

- ODIC_*:
    update required settings. eps. important are `OIDC_RP_CLIENT_ID` and `OIDC_RP_CLIENT_SECRET`.

- GMAPS_API_KEY:
    google maps api key

- FRONTEND_DEV_MODE:
    set to `True` to load development css and js files.



Building UI
-----------

The UI (js and css code) lives inside the frontend folder. There is a `Makefile` which helps compiling the `clojure` code into js.

.. code:: bash

    cd frontend
    make <target>


UI make targets
^^^^^^^^^^^^^^^

- clean:
    remove all compiled js output

- figwheel:
    build the deveploment version of the UI and keep watching
    for changes to rebuild UI automatically when changes are written to the source code.

- build:
    build the development version of the UI

- copy-static:
    copy updated static files from node_modules (generated via `make build`) into resources folder.

- release:
    build the production / release version of the UI



Django App
----------

The development environment is fully dockerised and uses docker-compose with local host bind mounts (./volumes folder) for data storage.
If a full reset of the dev env is required, stop all containers and delete the `./volumes` folder.


.. code:: bash

    # start db in detached mode
    docker-compose up -d db
    # build initial image for data-submission-tool
    docker-compose build web
    # start django app in debug mode
    docker-compose up web

The django app should now be accessible at http://localhost:8000 .
All code and UI changes on host system should be detected by the django app inside the container and are immediately available;
usually without restartin the django app.

To run the cron job which updates and ingests data feeds into the db run the cron container.

.. code:: bash

    # run in a separate terminal or use -d to sapwn in detached mode.
    docker-compose up cron


If an interactive python debugger is required the django container nees to be started slightly different.

.. code:: bash

    # stop existing conatiner if running
    docker-compose stop web
    # start django container (this allows dropping into an interactive pdb session)
    docker-compose run --rm --service-ports web

    # if required: in a 2nd terminal install any additional pkgs required for debugging
    # e.g.:
    RUN_CID=$(docker ps --filter "label=com.docker.compose.oneoff=True" --filter "label=com.docker.compose.service=web" --format '{{.ID}}')
    docker exec -ti -u 0 $RUN_CID pip3 install pyreadline ipdb

Migrations can be created in a similar fashion:

.. code:: bash

    # stop existing conatiner if running
    docker-compose stop web
    # start django container (this allows dropping into an interactive pdb session)
    docker-compose run --rm --service-ports web python3 manage.py makemigrations


Release
=======

A Makefile is provided to simplify building and tagging final images.
Please make sure that the UI has been built in release mode as described above.

Make targets:
-------------

- build:
    build and tag the image with `:latest` and a tag derived from current git version.

- up:
    run built images without mounting any source files.
    This allows fora quick test whether the images are working correctly.
    This setup is accessible at `http://localhost:8001`

- push:
    push :latest and a tag derived from git to docker hub
