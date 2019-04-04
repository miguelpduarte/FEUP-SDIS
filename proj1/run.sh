#!/usr/bin/env bash

cd $(dirname $0)

FULL_PATH="$(pwd -P)"
CLASS_PATH="/out/production/proj1"
TEST_APP_CLASS="base.TestApp"

case $2 in
"BACKUP")
    if [[ $# != 4 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> BACKUP <file_path> <replication_degree>"
        exit 1
    fi
    ;;
"RESTORE" | "DELETE")
    if [[ $# != 2 ]]; then
        echo "Wrong no. of arguments"
        echo "Usage: <peer_id> $2 <file_path>"
        exit 1
    fi
    ;;
*)
    echo "Invalid protocol method"
    exit 2
    ;;
esac

java -cp "$FULL_PATH/$CLASS_PATH" $TEST_APP_CLASS $@