SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")
cd "$BASEDIR"/..
java -cp "./*:conf" org.springframework.boot.loader.JarLauncher
