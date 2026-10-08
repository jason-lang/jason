IMAGE=jomifred/adoc
docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app "$IMAGE" asciidoctor -r /pygments_init.rb readme.adoc
docker run --rm -i --user="$(id -u):$(id -g)" --net=none -v "$PWD":/app "$IMAGE" asciidoctor -r /pygments_init.rb shell-based.adoc

cp readme.html index.html

scp -r *.html $USERSF,jason@web.sf.net:/home/project-web/jason/htdocs/doc/tutorials/getting-started

#zip -r ../../getting-started/VacuumCleaning-1.zip VacuumCleaning-1/*.asl VacuumCleaning-1/*.mas2j VacuumCleaning-1/*.java
#scp -r figures jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started
#scp exercise-answers.txt jomifred,jason@web.sf.net:/home/groups/j/ja/jason/htdocs/mini-tutorial/getting-started
