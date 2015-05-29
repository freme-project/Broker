ZIP_LOCATION="/var/www/html/freme-distributions/"
FREME_LOCATION="/opt/freme/"
BROKER_WORKSPACE="/var/lib/jenkins/workspace/Broker/"

cd target/FREME-*-full/FREME*/

# make release distribution nicer
dir="${PWD##*/}"
cd ..
cp -r $dir dist 
mv dist/Broker*.jar "dist/"$dir".jar"

# zip it and move to zip location
zip_file="$dir.zip"
zip -r $zip_file "dist"
target_zip=$ZIP_LOCATION"*SNAPSHOT.zip"
rm -f $target_zip
mv $zip_file $ZIP_LOCATION

# stop current freme
service freme stop

# deploy new freme
rm -rf /opt/freme/*
mv dist/* $FREME_LOCATION
chmod +x $FREME_LOCATION"bin/start_server.sh"
chmod +x $FREME_LOCATION"bin/start_local.sh"
chmod +x $FREME_LOCATION"bin/restart_server.sh"

# configuration
rm -r $FREME_LOCATION"config"
cp -r $BROKER_WORKSPACE"src/main/resources/configs/freme-dev/" $FREME_LOCATION"config"

# start
service freme start