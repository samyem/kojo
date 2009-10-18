ant clean build-zip

#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/cglib-nodep-2.1_3.jar
# zip -d -v dist/kojo.zip kojo/kojo/modules/ext/ScalaCheck-1.5.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/hamcrest-core-1.1.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/hamcrest-library-1.1.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/jmock-2.5.1.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/jmock-junit4-2.5.1.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/jmock-legacy-2.5.1.jar
#zip -d -v dist/kojo.zip kojo/kojo/modules/ext/objenesis-1.0.jar

zip -v dist/kojo.zip kojo/Kojo-license.txt
zip -v dist/kojo.zip kojo/licenses/*
zip -v dist/kojo.zip kojo/puzzles/puzzles.jar

rm -rf ~/work/kojo-dist/*
unzip dist/kojo.zip -d ~/work/kojo-dist/unpack
mv ~/work/kojo-dist/unpack/kojo/* ~/work/kojo-dist/
cp dist/kojo.zip /c/share
