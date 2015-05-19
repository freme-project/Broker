BASEDIR=$(dirname $0)
cd ..
kill `cat config/pid.txt`
bin/start_server.sh