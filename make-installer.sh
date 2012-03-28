# clean out stuff from last installerbuild creation
rm dist/kojo.zip
rm -rf installerbuild

# do a clean build
ant clean build-zip

# set up install dir structure
mkdir -p installerbuild/scratch/unpack
unzip dist/kojo.zip -d installerbuild/scratch/unpack
mv installerbuild/scratch/unpack/kojo/* installerbuild

cp -var kojo/* installerbuild
mkdir installerbuild/kojo/initk
mkdir installerbuild/kojo/libk

# remove test jar license files from install
rm installerbuild/licenses/cglib-license.txt
rm installerbuild/licenses/hamcrest-license.txt
rm installerbuild/licenses/jmock-license.txt
rm installerbuild/licenses/objenesis-license.txt
rm installerbuild/licenses/scalacheck-license.txt

# remove jni native libs that came in with NB7
# does not seem to be there with NB 711
rm -rf installerbuild/platform/modules/lib

# remove seemingly unused locale and auto-update jars
#  update dirs do not seem to be there with NB 711
rm -rf installerbuild/ide/modules/locale
rm -rf installerbuild/ide/update
rm -rf installerbuild/ide/update_tracking
rm -rf installerbuild/platform/modules/locale
rm -rf installerbuild/platform/update
rm -rf installerbuild/platform/update_tracking
rm -rf installerbuild/platform/lib/locale
rm -rf installerbuild/platform/core/locale

#copy i18n files
mkdir installerbuild/ide/modules/locale
mkdir installerbuild/platform/modules/locale
mkdir installerbuild/platform/lib/locale
mkdir installerbuild/platform/core/locale
cd installerbuild
sh ../swedish-cp.sh
cd ..

# run IzPack to create installerbuild
cp installer/* installerbuild/
cd installerbuild
/home/lalit/IzPack/bin/compile install.xml
echo installerbuild/install.jar is the Kojo installer.

