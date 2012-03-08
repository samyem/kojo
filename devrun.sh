set -x
rm -rf dist
ant build-zip
cd dist
unzip kojo.zip
cd ..
cp -r kojo/* dist/kojo/
cd dist/kojo/etc
cp ~/scratch/kojo.conf.dev ./kojo.conf
cd ../bin
./kojo
