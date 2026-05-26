// this rule is used to select the plans for an event
// it selects plans with annotation kk

// parameters are E = event, I = intention, P = plan, U = unifier
select__option(E,I,P,U) :- 
   .print("option for ",E," and intention= ",I) & 
   .print("   option plan: ",P) &
   P = {@Label +!Trigger : Context <- Body} & 
   Label =.. [Functor, Terms, Annots] &
   .member(kk, Annots).

a(10). a(20). b(30).

+!g : a(X) <- .print(X). 
@pt[kk] +!g : b(X) <- .print(X). 

!g.