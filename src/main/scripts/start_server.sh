BASEDIR=$(dirname $0)
cd ..
nohup java -cp "./*:conf"  -Dlogging.config=config/log4j.properties -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher >/dev/null 2>&1 &
echo $! > config/pid.txt
