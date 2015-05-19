SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")
cd $BASEDIR"/.."

kill `cat config/pid.txt`
bin/start_server.sh