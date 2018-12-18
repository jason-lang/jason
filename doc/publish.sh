#
# by Jomi
#

cd ..
gradle renderAsciidoc
gradle javadoc
cd doc
cp readme.html index.html
scp -r *  jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/doc
