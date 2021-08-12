DUMP indices:
=============

1. set env vars

    export AUTH_QUDT=user:password
    export AUTH_SHARED=user:password

2. run dump script
    ./dump.sh

Package this folder:
====================

gnutar czf es_shared_dump.tar.gz --transform 's,^\.,es_shared_dump,' ./*.txt ./*.sh ./*.json


LOAD indices:
=============

1. start up ES

    docker run --rm -it -p 9200:9200 -v $(pwd)/es-data:/usr/share/elasticsearch/data -e "discovery.type=single-node" elasticsearch:7.5.2

2. load data dump into local ES

    ./load.sh


Cleanup local ES:
=================

curl -X DELETE 'localhost:9200/qudt_units*'
curl -X DELETE 'localhost:9200/shared_*'
