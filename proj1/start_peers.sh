#!/usr/bin/env bash

cd $(dirname $0)

FULL_PATH="$(pwd -P)"

CLASS_PATH="/out/production/proj1"
PEER_CLASS="Peer"

PEER_PREFIX="Peer-"

if ! [[ $1 =~ ^[0-9]+$ ]] ; then
    echo "Error: The number of peers must be a positive integer!" >&2; exit 1
fi

# Application arguments
PROTOCOL_VERSION="1.0"
#SERVER_ID -> Is generated sequentially
#SERVER_ACCESS_POINT -> Is generated sequentially

MC_HOSTNAME="228.25.25.25"
MC_PORT="8823"

MDB_HOSTNAME="228.25.25.25"
MDB_PORT="8824"

MDC_HOSTNAME="228.25.25.25"
MDC_PORT="8825"

for (( i = 0; i < $1; i++ )); do
    x-terminal-emulator -e java -cp "$FULL_PATH/$CLASS_PATH" $PEER_CLASS $PROTOCOL_VERSION $i $PEER_PREFIX$i $MC_HOSTNAME $MC_PORT $MDB_HOSTNAME $MDB_PORT $MDC_HOSTNAME $MDC_PORT
done