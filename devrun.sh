# Script to run/debug Kojo during development
# Works much better than running Kojo from within NB -- custom heap size, sample and story dirs setup correctly, i18n etc

# *You need to tweak a few lines to make this work for you*. Lines commented with 'To make it work'

set -x
rm -rf dist
ant build-zip
cd dist
unzip kojo.zip
cd ..
cp -var kojo/* dist/kojo/
cd dist/kojo/etc
# To make it work - change the line below to - cp your-Kojo-dir/etc/kojo.conf ./kojo.conf
cp ~/scratch/kojo.conf.dev ./kojo.conf
cd ..
mkdir kojo/initk
cp -var ../../i18n/initk/ kojo
mkdir installerbuild/kojo/libk
# To make it work - comment out the next two lines
sh ../../i18n/swedish-cp.sh
jar uvf ide/modules/locale/org-netbeans-modules-editor_sv.jar -C ../../i18n/ ./org/netbeans/modules/editor/Bundle_sv.properties 
cd ./bin
# To debug Kojo, add the following to default_options within etc/kojo.conf:
# -J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005
# Then attach to port 5005 from Netbeans after Kojo starts up
./kojo
