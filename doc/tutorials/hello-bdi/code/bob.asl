sincere(alice).

!create_calendar.
+!create_calendar <- makeArtifact("c","Calendar",[],AId); focus(AId).

+happy(H)[source(A)] : sincere(A)[source(self)] & .my_name(H) <- !say(hello(A)).
+happy(H)            : not .my_name(H)                        <- !say(i_envy(H)).

-happy(H)[source(A)]
   <- .drop_intention(say(hello(A)));
      .drop_intention(say(i_envy(H))).

+!say(X) : today(friday)     <- .print(X,"!!!!!"); .wait(500); !say(X).
+!say(X) : not today(monday) <- .print(X);         .wait(math.random(400)+100); !say(X).

+today(wednesday) <- .print("**** Let's slow down.... **** (only 2 intentions)"); !enter_lazy_mode.
+today(friday)    <- .print("**** Let's finish the work!"); !resume_all.
+today(saturday)  <- .print("**** weekend!"); .drop_all_intentions.

+!enter_lazy_mode
    : .findall(A, .intend(say(A)), [_,_|L]) // the agent has two say intentions
   <- for ( .member(I,L) ) {
         .suspend(say(I));
      }.
+!enter_lazy_mode.


+!resume_all
    : .count( .intend(A) & .suspended(A,R) & .substring("suspended",R), I) & I > 0
   <- .resume(say(_));
      !resume_all.
+!resume_all.

