ZIP_LOCATION="/var/www/html/freme-distributions/"
FREME_LOCATION="/opt/freme/"
BROKER_WORKSPACE=`pwd`

cd target/FREME-*-full/FREME*/

# make release distribution nicer
dir="${PWD##*/}"
mv Broker*.jar "$dir.jar"

# zip it and move to zip location
cd ..
zip_file="$dir.zip"
zip -r $zip_file *
target_zip=$ZIP_LOCATION"/*SNAPSHOT.zip"
rm -f $target_zip
mv $zip_file $target_zip

# stop current freme
kill `cat $FREME_LOCATION$dir"/config/pid.txt"`

# deploy new freme
target_dir="$FREME_LOCATION$dir"
rm -rf /opt/freme/*
cp -r $dir /opt/freme
chmod +x $target_dir"/bin/start_server.sh"
chmod +x $target_dir"/bin/start_local.sh"
chmod +x $target_dir"/bin/restart_server.sh"

# configuration
rm -r $target_dir"/config"
cp -r $BROKER_WORKSPACE"/src/main/resources/configs/freme-dev/" $target_dir"/config/"

# start new freme
sh $target_dir"/bin/start_server.sh"