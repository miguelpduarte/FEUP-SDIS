#!/usr/bin/env bash

cd $(dirname $0)

# Runtime definitions
FULL_PATH="$(pwd -P)"
CLASS_PATH="/out/production/proj1"
TEST_APP_CLASS="base.TestApp"

# Ensuring project compilation (assumes that both shell scripts are in the same directory)
echo "Building project..."
sh "$FULL_PATH/build_testapp.sh"
if [[ $? != 0 ]]; then
    echo "Error building the project"
    exit 2
fi

if [[ $# < 2 ]]; then
    echo "Not enough arguments"
    echo "Usage: <peer_id> <protocol_method> <protocol_args>*"
    echo "Supported protocol methods: BACKUP, RESTORE, DELETE, RECLAIM, STATE"
    exit 1
fi

case $2 in
"BACKUP" | "BACKUPENH")
    if [[ $# != 4 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> $2 <file_path> <replication_degree>"
        exit 1
    fi
    ;;
"RESTORE" | "RESTOREENH" | "DELETE")
    if [[ $# != 3 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> $2 <file_path>"
        exit 1
    fi
    ;;
"RECLAIM")
    if [[ $# != 3 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> RECLAIM <disk_space_kbs>"
        exit 1
    fi
    ;;
"STATE")
    if [[ $# != 2 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> STATE"
        exit 1
    fi
    ;;
*)
    echo "Unknown protocol method '$2'"
    exit 2
    ;;
esac

java -cp "$FULL_PATH/$CLASS_PATH" $TEST_APP_CLASS $@