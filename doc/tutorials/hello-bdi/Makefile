#
# by Jomi
#

adoc:
	asciidoctor -r ../../../src/main/resources/pygments_init.rb readme.adoc

publish:
	mv readme.html index.html
	scp index.html  jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi
#	scp jcm.css  jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi
#	scp -r screens jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi
#	scp -r code jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/hello-bdi
