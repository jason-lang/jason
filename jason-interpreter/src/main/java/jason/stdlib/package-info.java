/**

Internal actions of Jason.

<h2>BDI</h2>
<ul>
  <li>{@link jason.stdlib.desire desire}: check agent's desires.</li>
  <li>{@link jason.stdlib.drop_desire drop_desire}: remove one agent's desire.</li>
  <li>{@link jason.stdlib.drop_all_desires drop_all_desires}: remove agent's desires.</li>

  <li>{@link jason.stdlib.intend intend}: check agent's intentions.</li>
  <li>{@link jason.stdlib.drop_intention drop_intention}: remove one agent's intention.</li>
  <li>{@link jason.stdlib.drop_all_intentions drop_all_intentions}: remove all agent's intentions.</li>
  <li>{@link jason.stdlib.intention intention}: get details of the state of Jason intentions.</li>

  <li>{@link jason.stdlib.succeed_goal succeed_goal}: abort some goal with success.</li>
  <li>{@link jason.stdlib.fail_goal fail_goal}: abort some goal with failure.</li>

  <li>{@link jason.stdlib.drop_event drop_event}: remove one event.</li>
  <li>{@link jason.stdlib.drop_all_events drop_all_events}: remove events in the interpreter.</li>

  <li>{@link jason.stdlib.suspend suspend}: suspend intentions.</li>
  <li>{@link jason.stdlib.resume resume}: resume suspended intentions.</li>
  <li>{@link jason.stdlib.suspended suspended}: check agent's suspended goals.</li>
</ul>

<h2>Belief base</h2>
<ul>
  <li>{@link jason.stdlib.abolish abolish}: removes some beliefs.</li>
  <li>{@link jason.stdlib.belief belief}: consults some beliefs.</li>
  <li>{@link jason.stdlib.findall findall}: finds all answers for a query.</li>
  <li>{@link jason.stdlib.setof setof}: finds a set of answers for a query.</li>
  <li>{@link jason.stdlib.count count}: counts the number of solutions of some query.</li>
  <li>{@link jason.stdlib.namespace namespace}: checks whether the argument is a name space.</li>
  <li>{@link jason.stdlib.relevant_rules relevant_rules}: gets a list of rules.</li>
  <li>{@link jason.stdlib.list_rules list_rules}: prints out the rules in the current belief base.</li>
</ul>


<h2>Plan Library</h2>
<ul>
  <li>{@link jason.stdlib.add_plan add_plan}: add new plans.</li>
  <li>{@link jason.stdlib.remove_plan remove_plan}: remove a plan.</li>
  <li>{@link jason.stdlib.plan_label plan_label}: get the label of a plan.</li>
  <li>{@link jason.stdlib.relevant_plans relevant_plans}: get a list of plans.</li>
  <li>{@link jason.stdlib.relevant_plan relevant_plan}: backtracking based version of .relevant_plans.</li>
  <li>{@link jason.stdlib.list_plans list_plans}: print out the plans in the current plan library.</li>
</ul>



<h2>Communication</h2>
<ul>
  <li>{@link jason.stdlib.send send}: send messages. </li>
  <li>{@link jason.stdlib.broadcast broadcast}: broadcast messages.</li>
  <li>{@link jason.stdlib.my_name my_name}: get the agent's name.</li>
  <li>{@link jason.stdlib.all_names all_names}: get the names of all agents in the system.</li>
  <li>{@link jason.stdlib.df_register df_register}: register a service in the Directory Facilitator.</li>
  <li>{@link jason.stdlib.df_deregister df_deregister}: removes a service in the Directory Facilitator.</li>
  <li>{@link jason.stdlib.df_search df_search}: search for a service in the Directory Facilitator.</li>
  <li>{@link jason.stdlib.df_subscribe df_subscribe}: subscribe for new providers of a service in the Directory Facilitator.</li>
</ul>


<h2>Lists, Sets and other collections</h2>
<ul>
  <li>{@link jason.stdlib.member member}: list members. </li>
  <li>{@link jason.stdlib.length length}: size of lists. </li>
  <li>{@link jason.stdlib.empty empty}: check whether the list is empty. </li>

  <li>{@link jason.stdlib.concat concat}: concat lists. </li>
  <li>{@link jason.stdlib.delete delete}: delete members of a lists. </li>

  <li>{@link jason.stdlib.reverse reverse}: reverse lists. </li>
  <li>{@link jason.stdlib.shuffle shuffle}: shuffle the elements of a list. </li>
  <li>{@link jason.stdlib.nth nth}: nth element of a lists. </li>
  <li>{@link jason.stdlib.max max}: maximum value of a lists. </li>
  <li>{@link jason.stdlib.min min}: minimum value of a lists. </li>
  <li>{@link jason.stdlib.sort sort}: sort lists. </li>
  <li>{@link jason.stdlib.list list}: check whether an argument is a list.</li>
  <li>{@link jason.stdlib.suffix suffix}: suffixes of a list. </li>
  <li>{@link jason.stdlib.prefix prefix}: prefixes of a list. </li>
  <li>{@link jason.stdlib.sublist sublist}: sublists of a list. </li>

  <li>{@link jason.stdlib.difference difference}: difference of sets. </li>
  <li>{@link jason.stdlib.intersection intersection}: intersection of sets. </li>
  <li>{@link jason.stdlib.union union}: union of sets. </li>
</ul>

<blockquote>
<h3>Java Sets</h3>
Java Sets can be manipulated in Jason by some internal actions (these sets are represented by a variable). Example:
<pre>
.set.create(S);                // S = {}
.set.add(S,a);                 // S = {a}
.set.add_all(S,[5,b,p(2),a]);  // S = {5,a,b,p(2)}
.set.remove(S,b);              // S = {5,a,p(2)}
.set.union(S,[a,1]);           // S = {1,5,a,p(2)}
.length(S,X);                  // X = 4
.type(S,T);                    // T = set
.set.difference(S,[1,a]);      // S = {5,p(2)}
.findall(K, .member(K,S), LL)  // LL = [5,p(2)]
.set.add(S,5);
.set.intersection(S,V);        // S = {5}
for ( .member(V,S) ) {         // iteration
   .print(K,V);
}.
.set.clear(S);                 // S = {}
</pre>

<h3>Java Queues</h3>
Java Queues can be manipulated in Jason by some internal actions (these queues are represented by a variable). Example:
<pre>
.queue.create(Q);                // Q = []
.queue.add(Q,a);                 // Q = [a]
.queue.add(Q,b);                 // Q = [a, b]
.queue.add_all(Q,[c,d,e]);       // Q = [a, b, c, d, e]
.queue.head(Q,H);                // H = a
.queue.remove(Q,H);              // H = a, Q = [b, c, d, e]
.length(Q,X);                    // X = 4
.type(Q,T);                      // T = queue
for ( .member(V,Q) ) {           // iteration
   .print(V);
}.
.queue.clear(Q);

.queue.create(Q,priority);       // Q = [] queue with priority (the queue is kept sorted)
.queue.add(Q,d);                 // Q = [d]
.queue.add(Q,c);                 // Q = [c, d]
.queue.add_all(Q,[b,d,a]);       // Q = [a, b, c, d, e]
.queue.head(Q,H);                // H = a
</pre>

<h3>Java Maps</h3>
Java Maps can be manipulated in Jason by some internal actions (these maps are represented by a variable). Example:
<pre>
.map.create(M);                  // M = {}
.map.put(M,a,10);                // M = {a->10}
.map.put(M,b,ok);                // M = {a->10, b->ok}
.map.put(M,a,20);                // M = {a->20, b->ok}
.length(M,X);                    // X = 2
.type(M,T);                      // T = map
.map.key(M,a)                    // true
.map.key(M,V)                    // unifies V with all keys of M
.map.value(M,10)                 // true
.map.value(M,V)                  // unifies V with all values of M
.findall(K, .map.key(M,K), LL)   // LL = [a,b]
.findall(V, .map.value(M,V), LL) // LL = [20,ok]
.findall([K,V], .map.key(M,K) & .map.get(M,K,V), LL)
                                 // LL = [[a,30],[d,ok(3)]]
.map.get(M,a,V);                 // V = 20
.map.get(M,c,V);                 // fail
.map.get(M,c,V,0);               // V = 0
.map.remove(M,a,V);              // M = {b->ok} V = 20
for ( .map.key(M,K) & .map.get(M,K,V) ) { // iteration
   .print(K,V);
}.
.map.clear(M);                   // M = {}
</pre>
</blockquote>

<h2>String</h2>
<ul>
  <li>{@link jason.stdlib.length length}: size of strings. </li>
  <li>{@link jason.stdlib.concat concat}: append strings. </li>
  <li>{@link jason.stdlib.delete delete}: delete characters of a string. </li>
  <li>{@link jason.stdlib.reverse reverse}: reverse strings. </li>
  <li>{@link jason.stdlib.nth nth}: nth element of a string. </li>
  <li>{@link jason.stdlib.substring substring}: test substrings of strings. </li>
  <li>{@link jason.stdlib.string string}: check whether an argument is a string.</li>
  <li>{@link jason.stdlib.suffix suffix}: suffixes of a string. </li>
  <li>{@link jason.stdlib.prefix prefix}: prefixes of a string. </li>
  <li>{@link jason.stdlib.term2string term2string}: convert terms to strings and vice-versa.</li>
  <li>{@link jason.stdlib.lower_case lower_case}: lower case strings.</li>
  <li>{@link jason.stdlib.upper_case upper_case}: upper case strings.</li>
  <li>{@link jason.stdlib.replace replace}: replace strings.</li>
</ul>

<h2>Execution control</h2>
<ul>
  <li>{@link jason.stdlib.if_then_else if}: implementation of <b>if</b>.</li>
  <li>{@link jason.stdlib.loop while}: implementation of <b>while</b>.</li>
  <li>{@link jason.stdlib.foreach for}: implementation of <b>for</b>.</li>
</ul>


<h2>Meta programming</h2>
<ul>
  <li>{@link jason.stdlib.atom atom}: check whether an argument is an atom (p).</li>
  <li>{@link jason.stdlib.structure structure}: check whether an argument is a  structure (p(t1,t2), [a,b]).</li>
  <li>{@link jason.stdlib.literal literal}: check whether an argument is a literal (p(t1,t2), ~p(t1,t2), p(t1,t2)[a1,a2]).</li>
  <li>{@link jason.stdlib.list list}: check whether an argument is a list ([a,b]).</li>
  <li>{@link jason.stdlib.ground ground}: check whether an argument is ground.</li>
  <li>{@link jason.stdlib.number number}: check whether an argument is a number (1, 2.3).</li>
  <li>{@link jason.stdlib.string string}: check whether an argument is a string ("s").</li>
  <li>{@link jason.stdlib.type type}: gets the type of a term.</li>
<!--  <li>{@link jason.stdlib.add_annot add_annot}: add an annotation in --
  --  a literal.</li> -->
  <li>{@link jason.stdlib.add_nested_source add_nested_source}: add a source in a literal.</li>
  <li>{@link jason.stdlib.eval eval}: evaluates a logical expression.</li>
</ul>


<h2>Miscellaneous</h2>
<ul>
  <li>{@link jason.stdlib.at at}: add a future event.</li>
  <li>{@link jason.stdlib.wait wait}: wait some event.</li>

  <li>{@link jason.stdlib.create_agent create_agent}: create a new agent.</li>
  <li>{@link jason.stdlib.save_agent save_agent}: store the beliefs and plans into a file.</li>
  <li>{@link jason.stdlib.create_agent create_agent}: create a new agent.</li>
  <li>{@link jason.stdlib.kill_agent kill_agent}: kill an agent.</li>
  <li>{@link jason.stdlib.clone}: clone an agent.</li>
  <li>{@link jason.stdlib.stopMAS stopMAS}: stop all agents.</li>
  <li>{@link jason.stdlib.version version}: gets the Jason version.</li>

  <li>{@link jason.stdlib.date date}: get the current date.</li>
  <li>{@link jason.stdlib.time time}: get the current time.</li>

  <li>{@link jason.stdlib.fail fail}: an action that always fails.</li>
  <li>{@link jason.stdlib.perceive perceive}: force perception.</li>

  <li>{@link jason.stdlib.range range}: backtrack values in a range (used in <b>for</b>).</li>

  <li>{@link jason.stdlib.random random}: produces random numbers.</li>
  <li>{@link jason.stdlib.set_random_seed math.set_random_seed}: sets the seed of Jason's random number generator.</li>

  <li>{@link jason.stdlib.include include}: imports a source code at run time.</li>

  <li>{@link jason.stdlib.printf printf}: formated print.</li>

</ul>
*/
package jason.stdlib;
