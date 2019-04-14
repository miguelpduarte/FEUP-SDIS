#!/usr/bin/env sh

cd $(dirname $0)

sh build_peer.sh
sh build_testapp.sh
