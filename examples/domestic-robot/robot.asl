/* Initial beliefs and rules */

// initially, I believe that there is some beer in the fridge
available(beer,fridge).

// my owner should not consume more than 10 beers a day :-)
limit(beer,10). 

too_much(B) :- 
   .date(YY,MM,DD) &
   .count(consumed(YY,MM,DD,_,_,_,B),QtdB) &
   limit(B,Limit) &
   QtdB > Limit.

    
/* Plans */
    
+!has(owner,beer)
   :  available(beer,fridge) & not too_much(beer)
   <- !at(robot,fridge);
      open(fridge);
      get(beer);
      close(fridge);
      !at(robot,owner);
      hand_in(beer);
      ?has(owner,beer);
      // remember that another beer has been consumed
      .date(YY,MM,DD); .time(HH,NN,SS);
      +consumed(YY,MM,DD,HH,NN,SS,beer).

+!has(owner,beer)
   :  not available(beer,fridge)
   <- .send(supermarket, achieve, order(beer,5));
      !at(robot,fridge). // go to fridge and wait there.

+!has(owner,beer)
   :  too_much(beer) & limit(beer,L)    
   <- .concat("The Department of Health does not allow me to give you more than ", L, 
              " beers a day! I am very sorry about that!",M);
      .send(owner,tell,msg(M)).    

   
-!has(_,_)
   :  true
   <- .current_intention(I); 
      .print("Failed to achieve goal '!has(_,_)'. Current intention is: ",I).
   
+!at(robot,P) : at(robot,P) <- true.
+!at(robot,P) : not at(robot,P)
  <- move_towards(P);
     !at(robot,P).

// when the supermarket makes a delivery, try the 'has' goal again   
+delivered(beer,_Qtd,_OrderId)[source(supermarket)]
  :  true
  <- +available(beer,fridge);
     !has(owner,beer). 
   
// when the fridge is opened, the beer stock is perceived
// and thus the available belief is updated
+stock(beer,0) 
   :  available(beer,fridge)
   <- -available(beer,fridge).
+stock(beer,N) 
   :  N > 0 & not available(beer,fridge)
   <- -+available(beer,fridge).

+?time(T) : true
  <-  time.check(T).

