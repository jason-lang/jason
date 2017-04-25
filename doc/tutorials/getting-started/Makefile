#
# by Jomi
#

adoc:
	asciidoctor -r ../../../src/main/resources/pygments_init.rb readme.adoc
	asciidoctor shell-based.adoc

publish:
	#zip -r ../../getting-started/VacuumCleaning-1.zip VacuumCleaning-1/*.asl VacuumCleaning-1/*.mas2j VacuumCleaning-1/*.java
	cp readme.html index.html
	scp *.html jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started
	#scp -r figures jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started
	#scp exercise-answers.txt jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started
