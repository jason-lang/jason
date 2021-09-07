!test. // initial goal
+!test : true <- .print("start"); !g1(X); .print("End, X=",X).

+!g1(X) : true <- .print(inig1); !g2(X); .print(endg1).
+!g2(X) : true <- .print(inig2); !g3(X); .print(endg2).
+!g3(X) : true <- .print(inig3); !g4(X); .print(endg3).
+!g4(X) : true <- .print(inig4); !g5(X); .print(endg4).
+!g5(_) : true <- .fail.

-!g3(failure)[error(ErrorId), error_msg(Msg), code(CodeBody), code_src(CodeSrc), code_line(CodeLine)] : true // get annotations related to the failure (these annotations are optional)
   <- .print("Error ", ErrorId, " '",Msg,"' by ",CodeBody," in ",CodeSrc,":",CodeLine);
      .intention(Id,S,IntendedMeans,current);
      .print("current intention state is: ",S);
      .println;
      .println("*  Intention #",Id, ", IM stack:");
      !print_im(IntendedMeans);
   .

+!print_im([]).
+!print_im([im(PlanLabel,Trigger,Body,Unif)|R])
  <- .println("*        ",Body,"      * unifier: ",Unif,", plan ",PlanLabel);
     !print_im(R).
