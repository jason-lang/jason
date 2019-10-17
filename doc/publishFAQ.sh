
IMAGE=jomifred/adoc
docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app "$IMAGE" asciidoctor -r /pygments_init.rb faq.adoc
scp faq.html  $USERSF,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/doc
