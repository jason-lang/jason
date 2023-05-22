#!/bin/bash

CURDIR="`pwd`"
P=`which "$0"`
JASON_CLI_HOME="`dirname $P`"
cd $JASON_CLI_HOME
JASON_CLI_HOME="`pwd`"
#echo "JASON_CLI_HOME is $JASON_CLI_HOME"
cd "$CURDIR"

# add  uberJar and others in classpath
java -cp ".:build/classes/java/main:$JASON_CLI_HOME/*" jason.infra.local.RunLocalMAS $*
