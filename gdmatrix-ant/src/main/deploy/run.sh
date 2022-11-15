#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR
java -cp "../lib/*" org.santfeliu.ant.AntLauncher $1 $2 $3 $4 $5 $6 $7 $8 $9
