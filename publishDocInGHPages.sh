cd ..

./gradlew doc

rm -rf ../jason-pages/api
rm -rf ../jason-pages/doc

#asciidoctor -a stylesheet=adoc-empty.css readme.adoc -o doc.html
IMAGE=jomifred/adoc
docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app "$IMAGE" asciidoctor -r /pygments_init.rb -a stylesheet=adoc-empty.css readme.adoc -o doc.html

cp -R jason-interpreter/build/docs/javadoc ../jason-pages/api
cp -R doc ../jason-pages

cd ../jason-pages
git add api
git commit -a -m "add javadoc api"

#find doc -name readme.html -execdir cp readme.html index.html \;
git add doc
git commit -a -m "add jason github doc folder"

git push



