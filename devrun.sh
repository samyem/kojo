set -x
rm -rf dist
ant build-zip
cd dist
unzip kojo.zip
cd ..
cp -r kojo/* dist/kojo/
cd dist/kojo/etc
cp ~/scratch/kojo.conf.dev ./kojo.conf
cd ..
sh ../../i18n/swedish-cp.sh
jar uvf ide/modules/locale/org-netbeans-modules-editor_sv.jar -C ../../i18n/ ./org/netbeans/modules/editor/Bundle_sv.properties 
cd ./bin
./kojo
