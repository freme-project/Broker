ZIP_LOCATION="/var/www/html/freme-distributions/"
FREME_LOCATION="/opt/freme/"
FREME_USER=$0

cd target/FREME-*-full/FREME*/

# make release distribution nicer
dir="${PWD##*/}"
mv Broker*.jar "$dir.jar"

# zip it and move to zip location
cd ..
zip_file="$dir.zip"
zip -r $zip_file *
target_zip=$ZIP_LOCATION$zip_file
rm -f $target_zip
mv $zip_file $target_zip

# stop current freme
kill `cat "$FREME_LOCATION/FREME*/config/pid.txt"`

# deploy new freme
target_dir="$FREME_LOCATION$dir"
rm -rf /opt/freme/*
cp -r $dir /opt/freme
cd $target_dir
chmod +x bin/start.sh
chown "$FREME_USER:$FREME_USER" target_dir

# start new freme
nohup bin/start.sh >/dev/null 2>&1 &
echo $! > config/pid.txt