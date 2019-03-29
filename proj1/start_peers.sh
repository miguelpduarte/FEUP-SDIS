#!/usr/bin/env bash

NUMBER_REGEX=^[0-9]+$

cd $(dirname $0)

FULL_PATH="$(pwd -P)"

CLASS_PATH="/out/production/proj1"
PEER_CLASS="base.PeerMain"

PEER_PREFIX="Peer-"

if ! [[ $1 =~ $NUMBER_REGEX ]]; then
    echo "Error: The number of peers must be a positive integer!"
    echo "Usage: $0 n_peers [--tile [n_cols]]"
    exit 1
fi

if [[ $2 == "--tile" ]]; then
    echo "Enabling tiling mode using i3";
    if [[ $3 =~ $NUMBER_REGEX ]]; then
        TILE_COLS=$3; echo "Using tiling of $3 columns"
    else
        TILE_COLS=3; echo "Using default tiling of 3 columns"
    fi
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
    # i3 tiling, ignore
    if [[ -n ${TILE_COLS+x} ]]; then
        col=$((i % TILE_COLS))
        # echo "my col: $col"
	    if [[ $col == 0 ]]; then
	        i3-msg -q split h
	    elif [[ $col == $((TILE_COLS-1)) ]]; then
	        i3-msg -q focus parent
	        i3-msg -q split v
	        # echo newrow
	    fi
    fi

    x-terminal-emulator -e java -cp "$FULL_PATH/$CLASS_PATH" $PEER_CLASS $PROTOCOL_VERSION $i $PEER_PREFIX$i $MC_HOSTNAME $MC_PORT $MDB_HOSTNAME $MDB_PORT $MDC_HOSTNAME $MDC_PORT
done