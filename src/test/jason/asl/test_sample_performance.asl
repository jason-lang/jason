/**
 * Test best performance of agent speak approaches
 */

{ include("tester_agent.asl") }

distance(X1,Y1,X2,Y2,D) :- D = math.abs(X2-X1) + math.abs(Y2-Y1).

nearest_a(T,X,Y) :-
    myposition(X1,Y1) &
    .findall(p(D,X2,Y2),map(_,X2,Y2,T) & distance(X1,Y1,X2,Y2,D),FL) &
    .sort(FL,[H|R]) & H =.. A & .nth(2,A,N) & .nth(1,N,X) & .nth(2,N,Y).

nearest_b(T,X,Y) :-
    myposition(X1,Y1) &
    .findall(p(D,X2,Y2),map(_,X2,Y2,T) & distance(X1,Y1,X2,Y2,D),FL) &
    .min(FL,p(_,X,Y)).

/**
 * Compare two approaches: 'sort+nth' vs 'min'
 * It is expected that 'sort+nth' takes more time than 'min'
 */
@[atomic] //test disabled since it is sometimes giving unexpected results
+!test_sortnth_vs_min :
    true
    <-
    .abolish(map(_,_,_,_));
    .abolish(myposition(_,_));
    +map(0,5,5,goal)[source(self)];
    +map(0,-5,-5,goal)[source(self)];
    +map(0,-5,-4,goal)[source(self)];
    +map(0,4,2,goal)[source(self)];
    +myposition(0,0);
    ?nearest_a(goal,X1,Y1);
    !assert_equals(4,X1);
    !assert_equals(2,Y1);
    ?nearest_a(goal,X2,Y2);
    !assert_equals(4,X2);
    !assert_equals(2,Y2);
    !check_performance(testNearest_aPerformance,30,R0);
    !check_performance(testNearest_bPerformance,30,R1);
    !assert_greater_than(R0,R1);
.
+!testNearest_aPerformance :
    true
    <-
    ?nearest_a(goal,X,Y);
.
+!testNearest_bPerformance :
    true
    <-
    ?nearest_b(goal,X,Y);
.
