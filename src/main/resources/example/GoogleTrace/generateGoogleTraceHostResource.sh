#!/bin/bash

function generateOneClusterHostResource(){
    if [ $# -lt 1 ]; then
        echo "Please provide the geoogle cluster name."
        return 0
    fi
    local CLUSTER_NAME=$1
    QUERY="SELECT capacity.cpus, capacity.memory AS ram, COUNT(DISTINCT machine_id) AS machine_count
        FROM \`${CLUSTER_NAME}.machine_events\`
        WHERE capacity.cpus IS NOT NULL AND capacity.memory IS NOT NULL
        GROUP BY capacity.cpus, capacity.memory"
    bq query --use_legacy_sql=false --format=csv ${QUERY} > ${CLUSTER_NAME: -6}_host_resource.csv
}

generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_a"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_b"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_c"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_d"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_e"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_f"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_g"
generateOneClusterHostResource "google.com:google-cluster-data.clusterdata_2019_h"
