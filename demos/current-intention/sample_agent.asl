!start(a).
!start(b).
!start(c).
!start(d).

+!start(X)
   <- .print(X);
      .wait( math.random(1000)+1000 );
      !inspect(X);
      !start(X).

// this plan uses BDI internal actions to discover the current goal
+!inspect(X)
   <- .intention(Id,State,Stack,current); // doc at http://jason.sourceforge.net/api/jason/stdlib/intention.html
      .print("Current intention number is ",Id," and its state is ",State);
      Stack = [Top|_]; // get just the intended means in the top of current intention
      Top   = im(Label, { +!Goal },PlanBody,Unifier);
      .print("     current goal is ",Goal," being pursued by plan ",Label);
      Goal  =.. [NameSpace,Functor,Terms,Annots];
      .print("     its functor is '",Functor,"' and terms are ",Terms);
      .print;
      // prints out all other intentions
      for ( .intention(_,OtherState,[ im(_, { +!OtherGoal },_,_) |_]) & OtherGoal \== Goal) {
          .print("     another goal is ",OtherGoal," in state ",OtherState);
      }
   .
