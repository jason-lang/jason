/* initial beliefs */

time(0).
num_rounds(2000).

// Knowledge of the PD, shared by all agents
{ include("pd_payoffs.asl") }

// Rule to select two random players among known players
select(P1,P2) :- .count(player(_),N)  &
  two_randoms(R1,R2,N) & .findall(P,player(P),LP) &
  .nth(R1,LP,P1) & .nth(R2,LP,P2).

// Rule to get two *different* random numbers, both less than N;
// my.random is a "backtracking" internal action
two_randoms(R1,R2,N)   :- my.random(R1,N) & my.random(R2,N) & R2 \== R1.

/* plans */

// As soon as I know of 2 players, I can start arresting
+player(P)
  :  .count(player(_),2)
  <- !!arrest.

// I have the goal to arrest a new pair of players (and I
// haven't arrested 500 pairs of players yet)
@arrest[atomic]
+!arrest
  :  time(T) & num_rounds(R) & T < R
  <- ?select(P1,P2); // select two players randomly
     .print("I'm arresting ",P1," and ",P2," (time ",T,")");
     .send(P1,tell,arrested(T,P2));
     .send(P2,tell,arrested(T,P1));
     -+time(T+1).

// otherwise, I've arrested enough pairs already.
+!arrest
  <- .print("Finished.").

// Just got to know what the second player of a pair played,
// so I can calculate the scores and inform them
+play(T,M1)[source(P1)]
  :  play(T,M2)[source(P2)] & P1 \== P2
  <- ?payoffs(M1,M2,S1,S2);
     .print("Scores at time ",T,": ",S1," ",S2);
     .send(P1,tell,score(T,S1));
     .send(P2,tell,score(T,S2));
     .abolish(play(T,_));
     !!arrest.

// ignore the first player to inform me of their move
//+play(T,M1).

