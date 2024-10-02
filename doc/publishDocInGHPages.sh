cd ..

./gradlew doc

#rm -rf /tmp/gh-pages*
#git clone https://github.com/jason-lang/jason.git --branch gh-pages-new --single-branch /tmp/gh-pages

SITE_PATH=../jason-lang.github.io

rm -rf $SITE_PATH/api
rm -rf $SITE_PATH/doc

cp -R jason-interpreter/build/docs/javadoc $SITE_PATH/api
cp -R doc $SITE_PATH


cd $SITE_PATH
git add api
git commit -a -m "add javadoc api"

find doc -name readme.html -execdir cp readme.html index.html \;
git add doc
git commit -a -m "add jason github doc folder"

git push

echo "edit jason-site.../doc/doc.html properly"

