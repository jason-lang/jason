cd 3.1
rm -rf .gradle
zip -r ../np31.zip *
cd ..

scp np31.zip $USERSF,jason@web.sf.net:/home/project-web/jason/htdocs/np

#scp *.zip $USERSF,jacamo@web.sf.net:/home/project-web/jacamo/htdocs/nps


#scp np* $USERSF,jason@web.sf.net:/home/project-web/jason/htdocs/np
