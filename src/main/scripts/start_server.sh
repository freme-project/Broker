SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")
cd $BASEDIR"/.."

nohup java -cp "./*:conf"  -Dlogging.config=config/log4j.properties -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher > /opt/freme/nohup.out 2>&1 &
echo $! > config/pid.txt
