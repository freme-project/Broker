#!/bin/sh
#
# Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

### BEGIN INIT INFO
# Provides:          Start / stop skript for FREME
# Required-Start:    
# Required-Stop:     
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: start / stop FREME
# Description:       supports command start, stop and restart
### END INIT INFO
# Author: Jan Nehring <jan.nehring@dfki.de>

FREME_DIR=/home/jan/workspaces/freme/release/Broker-0.1/target/FREME-0.1-full/FREME-0.1
PID_FILE=$FREME_DIR"/config/pid.txt"

start(){
	echo "Starting FREME...\n".
	cd $FREME_DIR
        nohup java -cp "./*:config"  -Dlogging.config=config/log4j.properties -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher > /dev/null 2>&1 &
	[ -f $pid ] && rm $PID_FILE
	echo $! > $PID_FILE
}

stop(){
	echo "Stopping FREME..."
	 [ -f $PID_FILE ] && kill `cat $PID_FILE`
}


case "$1" in
    start)
	start

        ;;
    stop)
       stop

        ;;
    restart)
        stop
	start
        ;;
esac

exit 0
