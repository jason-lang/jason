#!/bin/sh

docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app jomifred/adoc asciidoctor -r /pygments_init.rb readme.adoc
mv readme.html index.html
scp index.html  jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi

#scp hello-bdi-code.zip jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi
