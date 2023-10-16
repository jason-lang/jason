git checkout master
cd ..
./gradlew :jason-interpreter:javadoc
cp -R jason-interpreter/build/docs/javadoc /tmp/api

git checkout gh-pages

