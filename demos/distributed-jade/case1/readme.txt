Steps to run this demo

1. Run the application in a single container:

    jason demo-case1.mas2j

the output should be

[d] starting d
[c2] starting c2
[c1] starting c1
Jason Http Server running on http://10.0.1.2:3272
[b] hello world.
[d] hello world.
[a] hello world.
[c1] hello world.
[c2] hello world.
[c3] hello world.
[a] I received a hello from b
[a] I received a hello from c1
[a] I received a hello from c2
[a] I received a hello from c3
----------------------------------------------

To run the MAS in more containers, follows stesp 2-3.

2. Run the Jade Main Container

    ant -f bin/demo_distributed_jade.xml Main-Container

the output should be

[java] Agent container Main-Container@10.0.1.2 is ready.
----------------------------------------------

3. Run the container c1

    ant -f bin/demo_distributed_jade.xml c1

the output should be

[java] Agent container c1@10.0.1.2 is ready.
[java] INFO: hello world.
[java] INFO: hello world.
----------------------------------------------

4. Run the container c2

    ant -f bin/demo_distributed_jade.xml c2

the output should be

Agent container c2@10.0.1.2 is ready.
[java] INFO: hello world.
[java] INFO: hello world.
[java] INFO: hello world.
[java] INFO: hello world.

and in the console from step 3:

[java] INFO: I received a hello from c3
[java] INFO: I received a hello from c2
[java] INFO: I received a hello from c1
[java] INFO: I received a hello from b
----------------------------------------------

NB.: if Ant cannot be used, the JVM can be started as follows

Main Container with agents a and d:

  java -cp "$JASON_HOME/libs/*:bin/classes" jade.Boot -agents "a:jason.infra.jade.JadeAgArch(j-project,demo-case1.mas2j,a);d:jason.infra.jade.JadeAgArch(j-project,demo-case1.mas2j,d)"

Other Container with agents b and c:

  java -cp "$JASON_HOME/libs/*:bin/classes" jade.Boot -container -host localhost -agents "b:jason.infra.jade.JadeAgArch(j-project,demo-case1.mas2j,b);c1:jason.infra.jade.JadeAgArch(j-project,demo-case1.mas2j,c)"

...
