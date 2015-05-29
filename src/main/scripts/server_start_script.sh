#!/bin/sh
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
