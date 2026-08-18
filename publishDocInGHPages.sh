# this script is used to create the jason documentation
# and publish it in the jason-lang.github.io repository
# it should be run from the jason repository
#
# the jason-lang.github.io/doc should not be edited
# changes should be done in the jason repository and then
# this script should be run to publish the changes


cd ..

./gradlew doc
./gradlew javadoc

SITE_PATH=../jason-lang.github.io

rm -rf $SITE_PATH/api
rm -rf $SITE_PATH/doc

IMAGE=jomifred/adoc
docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app "$IMAGE" asciidoctor -r /pygments_init.rb -a stylesheet=adoc-empty.css doc/readme.adoc -o doc/doc.html

cp -R jason-interpreter/build/docs/javadoc $SITE_PATH/api
cp -R doc $SITE_PATH

cd $SITE_PATH
git add api
git commit -a -m "add javadoc api"

#find doc -name readme.html -execdir cp readme.html index.html \;
git add doc
git commit -a -m "add jason github doc folder"

git push
