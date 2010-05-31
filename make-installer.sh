# clean out stuff from last installer creation
rm dist/kojo.zip
rm installer/Kojo-license.txt
rm -rf installer/bin/
rm -rf installer/etc/
rm -rf installer/harness/
rm -rf installer/ide12/
rm -rf installer/kojo/
rm -rf installer/licenses/
rm -rf installer/platform11/
rm -rf installer/puzzles/
rm -rf installer/icons/
rm installer/install.jar

# do a clean build
ant clean build-zip

# set up install dir structure
rm -rf installer/scratch/*
mkdir -p installer/scratch/unpack
unzip dist/kojo.zip -d installer/scratch/unpack
mv installer/scratch/unpack/kojo/* installer

cp kojo/Kojo-license.txt installer/
mkdir installer/licenses/
cp kojo/licenses/* installer/licenses/
mkdir installer/puzzles/
cp kojo/puzzles/puzzles.jar installer/puzzles/
cp kojo/etc/kojo.conf installer/etc
mkdir installer/icons/
cp kojo/icons/* installer/icons/

# run IzPack to create installer
cd installer
compile install.xml
