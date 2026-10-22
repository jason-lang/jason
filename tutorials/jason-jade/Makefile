#
# by Jomi
#

adoc:
	asciidoctor -r ../../../src/main/resources/pygments_init.rb readme.adoc

publish:
	cp readme.html index.html
#	zip -r ../../jason-jade/jade-example.zip jade-example/examples/bookTrading/*.java
	scp index.html jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/doc/tutorials/jason-jade
	scp readme.html jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/doc/tutorials/jason-jade
	scp -r figures jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/doc/tutorials/jason-jade
