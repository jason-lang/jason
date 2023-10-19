cd ..

./gradlew doc

#rm -rf /tmp/gh-pages*
#git clone https://github.com/jason-lang/jason.git --branch gh-pages-new --single-branch /tmp/gh-pages

rm -rf ../jason-pages/api
rm -rf ../jason-pages/doc

cp -R jason-interpreter/build/docs/javadoc ../jason-pages/api
cp -R doc ../jason-pages


cd ../jason-pages
git add api
git commit -a -m "add javadoc api"

find doc -name readme.html -execdir cp readme.html index.html \;
git add doc
git commit -a -m "add jason github doc folder"

git push



