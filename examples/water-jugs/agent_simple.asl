steps(0). // store the number of actions executed

!jugs(_,1). // 1l in jug 3l 


// goal achievement condition
+!jugs(J5,J3) : jug(5,J5) & jug(3,J3) & steps(C)
    <- .print("Problem solved with ",C," actions");
       //.stopMAS;
    .

+!jugs(J5,J3) 
    <- // select a random operation
       .nth( math.floor(math.random(6)), [fill(5), fill(3), empty(5), empty(3), pour(3,5), pour(5,3)], Op);
       !Op;
       !jugs(J5,J3). //  keep trying operators

+!fill(J)
    : jug(J,V) & V < J
      & not last_op(fill(J))  // to not repeat last op
      & steps(S)
    <- fill(J);
       -+steps(S+1);
       -+last_op(fill(J)).
+!fill(J). // fill is not possible, try other operator

+!empty(J)
    : jug(J,V) & V > 0
      & not last_op(empty(J)) // to not repeat last op
      & steps(S)
    <- empty(J);
       -+steps(S+1);
       -+last_op(empty(J)).
+!empty(J).

+!pour(JO,JD)
    : jug(JO,VO) & VO > 0     // origem is not empty
      & jug(JD,VD) & VD < JD  // destinatation is not full
      & not last_op(pour(JO,JD))  // to not repeat last op
      & steps(S)
    <- pour(JO,JD);
       -+steps(S+1);
       -+last_op(pour(JO,JD)).
+!pour(JO,JD).

+jug(_,_) : jug(5,J5) & jug(3,J3) <- .print("           State: (",J5,",",J3,")").
