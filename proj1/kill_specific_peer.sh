#!/usr/bin/env bash

if ! [[ $1 =~ ^[0-9]+$ ]]; then
    echo "Invalid peer id!"
    echo "Usage: $0 <peer-id-to-kill>"
    exit 1
fi

PEER_PREFIX="Peer-" # To avoid collisions with other RMI declarations

pkill -f "java.*PeerMain.*$PEER_PREFIX$1"