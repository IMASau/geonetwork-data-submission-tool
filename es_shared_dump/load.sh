#!/bin/bash

DUMP="docker run --add-host=host.docker.internal:host-gateway --rm -it -v $(pwd):/data elasticdump/elasticsearch-dump:v6.79.0"

# qudt_units-2021-06-07t14-06-1623076799
# shared_instruments-2021-06-07t14-06-1623076799
# shared_org-2021-06-07t14-06-1623076620
# shared_parameters-2021-06-07t14-06-1623076633
# shared_people-2021-06-07t14-06-1623076605
# shared_platforms-2021-06-07t14-06-1623076809


# qudt_units
# shared_instruments
# shared_org
# shared_parameters
# shared_people
# shared_platforms

function load_index () {
    ALIAS="$1"

    INDEX=$(cat ${ALIAS}_alias.json | jq -r '. | fromjson | keys[0]')

    echo "CREATE ${INDEX}"
    curl -X PUT -H "Content-Type: application/json" -d '{"settings": {"index": { "number_of_replicas": 0 }}}' http://localhost:9200/${INDEX}

    # commented because we don't want to load replica config from our cluster into dev ES
    # echo "LOAD ${INDEX} Analyzer"
    # $DUMP \
    #     --input=/data/${ALIAS}_analyzer.json \
    #     --output=http://host.docker.internal:9200/${INDEX} \
    #     --type=analyzer

    echo "LOAD ${INDEX} Alias"
    $DUMP \
        --input=/data/${ALIAS}_alias.json \
        --output=http://host.docker.internal:9200/${INDEX} \
        --type=alias

    echo "LOAD ${INDEX} Mapper"
    $DUMP \
        --input=/data/${ALIAS}_mapping.json \
        --output=http://host.docker.internal:9200/${INDEX} \
        --type=mapping

    echo "LOAD ${INDEX} Data"
    $DUMP \
        --limit=1000 \
        --input=/data/${ALIAS}_data.json \
        --output=http://host.docker.internal:9200/${INDEX} \
        --type=data

}

load_index qudt_units

load_index shared_instruments
load_index shared_org
load_index shared_parameters
load_index shared_people
load_index shared_platforms
load_index shared_instrument_types
load_index shared_anzsrc_keywords
load_index shared_aus_plant_name
load_index shared_faunal_name
load_index shared_gcmd_horizontal_resolution
load_index shared_gcmd_science_keywords
load_index shared_gcmd_temporal_resolution
load_index shared_gcmd_vertical_resolution
load_index shared_geonetwork_sources

load_index shared_horizontal_coordinate_reference_system
load_index shared_vertical_coordinate_reference_system
