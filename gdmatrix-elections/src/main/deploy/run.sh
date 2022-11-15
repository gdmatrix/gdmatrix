#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd $DIR
java -cp "../lib/*" org.santfeliu.elections.swing.ElectionsMonitor &
