#!/usr/bin/env bash

# Have to use bash for regex I think. If already using bash and [[ might as well use it for everything - thus [ is not used

NUMBER_REGEX=^[0-9]+$

cd $(dirname $0)

FULL_PATH="$(pwd -P)"

# Runtime definitions
CLASS_PATH="/out/production/proj1"
PEER_CLASS="base.PeerMain"
PEER_PREFIX="Peer-" # To avoid collisions with other RMI declarations

# Ensuring project compilation (assumes that both shell scripts are in the same directory)
echo "Building project..."
sh "$FULL_PATH/build_peer.sh"
if [[ $? != 0 ]]; then
    echo "Error building the project"
    exit 2
fi

BASIC_PROTOCOL_VERSION="1.0"
ENHANCED_PROTOCOL_VERSION="2.1"

while [[ $# > 0 ]]; do
    case $1 in
    "--tile" | "-t")
	    echo "Enabling tiling mode using i3";
        if (( $# >= 2 )) && [[ $2 =~ $NUMBER_REGEX ]]; then
            TILE_COLS=$2; echo "Using tiling of $2 columns"
            shift
        else
            TILE_COLS=3; echo "Using default tiling of 3 columns"
        fi
        shift
	;;
	"--kill" | "-k")
	    echo "Killing the previously running peers"
	    # (assumes that both shell scripts are in the same directory)
        sh "$FULL_PATH/kill_peers.sh"
        # Error is ignored because no peers results in error
        shift
	;;
	"-s" | "--start-id")
        if (( $# >= 2 )) && [[ $2 =~ $NUMBER_REGEX ]]; then
	        echo "Using a non-0 peer start id of $2"
            PEER_START_ID=$2
            shift
        else
            echo "Invalid peer start id!"
            exit 1
        fi
        shift
	;;
	"-b" | "--basic")
	    echo "Starting in the basic protocol version (1.0)"
	    PROTOCOL_VERSION=$BASIC_PROTOCOL_VERSION
	    shift
	    ;;
    *)
        if ! [[ $1 =~ $NUMBER_REGEX ]]; then
            echo "Error: The number of peers must be a positive integer!"
            echo "Usage: $0 n_peers [--tile|-t [n_cols]] [--kill|-k] [-s|--start-id start_id]"
            echo "(Note: Tiling options require i3wm)"
            exit 1
        else
            echo "Starting $1 peers."
            N_PEERS=$1
            shift
        fi
	;;
    esac
done

echo "Starting with id ${PEER_START_ID+0}";

# Application arguments
# The default protocol version is enhanced
if [[ -z ${PROTOCOL_VERSION+x} ]]; then
    PROTOCOL_VERSION=$ENHANCED_PROTOCOL_VERSION
fi
#SERVER_ID -> Is generated sequentially
#SERVER_ACCESS_POINT -> Is generated sequentially

MC_HOSTNAME="228.25.25.25"
MC_PORT="8823"

MDB_HOSTNAME="228.25.25.25"
MDB_PORT="8824"

MDC_HOSTNAME="228.25.25.25"
MDC_PORT="8825"

for (( i = PEER_START_ID; i < N_PEERS + PEER_START_ID; i++ )); do
    # i3 tiling
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
    # (java -cp "$FULL_PATH/$CLASS_PATH" $PEER_CLASS $PROTOCOL_VERSION $i $PEER_PREFIX$i $MC_HOSTNAME $MC_PORT $MDB_HOSTNAME $MDB_PORT $MDC_HOSTNAME $MDC_PORT &)
done
