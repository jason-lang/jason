#!/bin/bash

CURDIR=`pwd`
JASON_HOME=`dirname $0`
cd "$JASON_HOME/.."
JASON_HOME=`pwd`
#cd "$JASON_HOME/bin"
cd $CURDIR

OS=`uname`

if [ -z $JDK_HOME ] ; then
    if [ -n $JAVA_HOME ] ; then
	JDK_HOME=$JAVA_HOME
    fi
fi

if [ -z $JDK_HOME ] ; then
	if [ $OS == Darwin ] ; then
		JDK_HOME=/usr
	fi
fi

# check JDK_HOME
if [ ! -f $JDK_HOME/bin/javac ] ; then
   echo JDK_HOME is not properly set!
fi

export PATH="$JDK_HOME/bin":$PATH

DPAR=""
if [ $OS == Darwin ] ; then
	DPAR="-Dapple.laf.useScreenMenuBar=true"
fi

# run jIDE
java -classpath "$JASON_HOME/lib/jason.jar":"$JASON_HOME/bin/jedit/jedit.jar":"$JASON_HOME/lib/saci.jar":"$JASON_HOME/lib/jade.jar":"$JASON_HOME/lib/cartago.jar":"$JASON_HOME/lib/c4jason.jar":"$JASON_HOME/bin/jedit/jars/ErrorList.jar":"$JASON_HOME/bin/jedit/jars/SideKick.jar":$JASON_HOME/lib/cartago.jar:$JASON_HOME/lib/c4jason.jar:$JASON_HOME/lib/moise.jar:$JASON_HOME/lib/jacamo.jar:$CLASSPATH:. \
   $DPAR \
   org.gjt.sp.jedit.jEdit $1
   
#"$JASON_HOME/lib/ant.jar":"$JASON_HOME/lib/ant-launcher.jar":
#-settings=$JASON_HOME/bin/.jedit 
