#!/bin/bash

function generateOneClusterUserRequest(){
    if [ $# -lt 3 ]; then
        echo "Please provide the google cluster name, project name and number of entries."
        return 0
    fi
    local CLUSTER_NAME=$1
    TMP_TABLE1="$2.tmp1"
    TMP_TABLE2="$2.tmp2"
    REQUEST_TABLE="$2.${CLUSTER_NAME: -6}_user_requests"
    LENGTH=$3

    TMP_SCHEMA1='time:INTEGER,collection_id:INTEGER,instance_index:INTEGER,cpus:FLOAT,ram:FLOAT,collection_type:INTEGER'
    REQUEST_SCHEMA='time:INTEGER,user:STRING,collection_id:INTEGER,instance_index:INTEGER,cpus:FLOAT,ram:FLOAT,collection_type:INTEGER'

    # find unique collection_id and instance_index entries.
    # Note that for each entry with the same collection_id and instance_index,
    # only the time will be different, and other attributes are the same.
    QUERY1="SELECT MIN(time) AS time, collection_id, instance_index, AVG(resource_request.cpus) AS cpus, AVG(resource_request.memory) AS ram, MIN(collection_type) AS collection_type
        FROM \`${CLUSTER_NAME}.instance_events\`
        WHERE time > 0
        GROUP BY collection_id, instance_index
        LIMIT ${LENGTH};"

    bq rm -f -t ${TMP_TABLE1}
    bq mk --schema ${TMP_SCHEMA1} --table ${TMP_TABLE1}
    bq query --use_legacy_sql=false --format=csv --destination_table ${TMP_TABLE1} "${QUERY1}"

    # find the username for each task from collection_events table
    QUERY2="WITH user_events AS (
    SELECT DISTINCT ce.collection_id, ce.user
    FROM \`${CLUSTER_NAME}.collection_events\` AS ce
    )

    SELECT ie.time, ue.user, ie.collection_id, ie.instance_index, ie.cpus, ie.ram, ie.collection_type
    FROM ${TMP_TABLE1} AS ie
    LEFT JOIN user_events AS ue
    ON ie.collection_id = ue.collection_id"

    bq rm -f -t ${TMP_TABLE2}
    bq mk --schema ${REQUEST_SCHEMA} --table ${TMP_TABLE2}
    bq query --use_legacy_sql=false --format=csv --destination_table ${TMP_TABLE2} "${QUERY2}"

    bq rm -f -t ${TMP_TABLE1}

    # Sort each entry according to the time of its earliest submitted instance in user units.
    QUERY3="SELECT t.time, t.user, t.collection_id, t.instance_index, t.cpus, t.ram, t.collection_type
        FROM ${TMP_TABLE2} AS t
        INNER JOIN (
        SELECT user, MIN(time) AS min_time
        FROM ${TMP_TABLE2}
        GROUP BY user
        ) AS sub
        ON t.user = sub.user
        ORDER BY sub.min_time, t.user;
        "

    bq rm -f -t ${REQUEST_TABLE}
    bq mk --schema ${REQUEST_SCHEMA} --table ${REQUEST_TABLE}
    bq query --use_legacy_sql=false --format=csv --destination_table ${REQUEST_TABLE} ${QUERY3}

    bq rm -f -t ${TMP_TABLE2}

    echo "The userRequests are saved in ${REQUEST_TABLE} table."
}

# you need to provide the google cluster name, project name and number of entries.
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_a" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_b" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_c" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_d" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_e" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_f" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_g" "bigqueryexample1" "10000"
generateOneClusterUserRequest "google.com:google-cluster-data.clusterdata_2019_h" "bigqueryexample1" "10000"
