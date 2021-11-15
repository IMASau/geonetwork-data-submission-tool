#!/bin/bash

DUMP="docker run --rm -it -v $(pwd):/data elasticdump/elasticsearch-dump:v6.74.0"

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

function dump_index () {
    AUTH="$1"
    INDEX="$2"

    echo "DUMP ${INDEX} Analyzer"
    $DUMP \
        --overwrite \
        --input=https://${AUTH}@es-test.tern.org.au/${INDEX} \
        --output=/data/${INDEX}_analyzer.json \
        --type=analyzer

    echo "DUMP ${INDEX} Alias"
    $DUMP \
        --overwrite \
        --input=https://${AUTH}@es-test.tern.org.au/${INDEX} \
        --output=/data/${INDEX}_alias.json \
        --type=alias

    echo "DUMP ${INDEX} Mapper"
    $DUMP \
        --overwrite \
        --input=https://${AUTH}@es-test.tern.org.au/${INDEX} \
        --output=/data/${INDEX}_mapping.json \
        --type=mapping

    echo "DUMP ${INDEX} Data"
    $DUMP \
        --limit=1000 \
        --overwrite \
        --input=https://${AUTH}@es-test.tern.org.au/${INDEX} \
        --output=/data/${INDEX}_data.json \
        --type=data
}

dump_index $AUTH_QUDT qudt_units

dump_index $AUTH_SHARED shared_org
dump_index $AUTH_SHARED shared_platforms
dump_index $AUTH_SHARED shared_parameters
dump_index $AUTH_SHARED shared_instrument_types
dump_index $AUTH_SHARED shared_instruments
dump_index $AUTH_SHARED shared_people
dump_index $AUTH_SHARED shared_anzsrc_for_keywords
dump_index $AUTH_SHARED shared_aus_plant_name
dump_index $AUTH_SHARED shared_faunal_name
dump_index $AUTH_SHARED shared_gcmd_horizontal_resolution
dump_index $AUTH_SHARED shared_gcmd_science_keywords
dump_index $AUTH_SHARED shared_gcmd_temporal_resolution
dump_index $AUTH_SHARED shared_gcmd_vertical_resolution
dump_index $AUTH_SHARED shared_geonetwork_sources

