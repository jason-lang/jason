// leader agent

/* quadrant allocation */

@quads[atomic]
+gsize(S,W,H) : true
  <- // calculates the area of each quadrant and remembers them
     .print("Defining quadrants for simulation ",S," : ",W,"x",H);

     CellH = H div 2;
     +quad(S,1, 0,       0,       W div 2 - 1, CellH - 1);
     +quad(S,2, W div 2, 0,       W - 1,       CellH - 1);
     +quad(S,3, 0,       CellH,   W div 2 - 1, (CellH * 2)-1);
     +quad(S,4, W div 2, CellH,   W - 1,       (CellH * 2)-1);

     !inform_quad(S,miner1,1);
     !inform_quad(S,miner2,2);
     !inform_quad(S,miner3,3);
     !inform_quad(S,miner4,4).


// only informs the quadrant if the depot is not in the quadrant
+!inform_quad(S,Miner,Q)
  :  quad(S,Q,X1,Y1,X2,Y2) &
     depot(S,DX,DY) &
     not (DX >= X1 & DX <= X2 &
          DY >= Y1 & DY <= Y2)
  <- .send(Miner,tell,quadrant(X1,Y1,X2,Y2)).

+!inform_quad(_,Miner,_)
  <- .print("Miner ",Miner," is in the depot quadrant.").


/*

     +init_pos(S,X,Y)[source(A)]
  :  gsize(S,_,_) &
     .count(init_pos(S,_,_),6) // if all miners have sent their position
  <- // remember who doesn't have a quadrant allocated
     // (initially 4 miners that will find gold by quadrant)
     +~quad(S,miner1); +~quad(S,miner2);
     +~quad(S,miner3); +~quad(S,miner4);
     //+~quad(S,miner5); +~quad(S,miner6);
     !assign_quads(S,[1,2,3,4]).


+!assign_quads(_,[]).
+!assign_quads(S,[Q|T])
  <- !assign_quad(S,Q);
     !assign_quads(S,T).

// assign the quadrant Q to a miner
+!assign_quad(S,Q)
  :  quad(S,Q,X1,Y1,X2,Y2) &
     ~quad(S,_) // there still is a miner without a quadrant
  <- .findall(Ag, ~quad(S,Ag), LAgs);
     !calc_ag_dist(S,Q,LAgs,LD);
     .min(LD,d(Dist,Ag));
     .print(Ag, "'s Quadrant is: ",Q, ": ", X1,",",Y1,"x",X2,",",Y2);
     -~quad(S,Ag);
     .send(Ag,tell,quadrant(X1,Y1,X2,Y2)).

+!calc_ag_dist(S,Q,[],[]).
+!calc_ag_dist(S,Q,[Ag|RAg],[d(Dist,Ag)|RDist])
  :  quad(S,Q,X1,Y1,X2,Y2) & init_pos(S,AgX,AgY)[source(Ag)]
  <- // get the distance between X1,Y1 and AgX,AgY
     jia.dist(X1,Y1,AgX,AgY,Dist);
     !calc_ag_dist(S,Q,RAg,RDist).

*/

/* negotiation for found gold */

+bid(Gold,D,Ag)
  :  .count(bid(Gold,_,_),5)  // five bids were received
  <- //.print("bid from ",Ag," for ",Gold," is ",D);
     !allocate_miner(Gold);
     .abolish(bid(Gold,_,_)).
//+bid(Gold,D,Ag)
//  <- .print("bid from ",Ag," for ",Gold," is ",D).

+!allocate_miner(Gold)
  <- .findall(op(Dist,A),bid(Gold,Dist,A),LD);
     .min(LD,op(DistCloser,Closer));
     DistCloser < 10000;
     .print("Gold ",Gold," was allocated to ",Closer, " options ware ",LD);
     .broadcast(tell,allocated(Gold,Closer)).
     //-Gold[source(_)].
-!allocate_miner(Gold)
  <- .print("could not allocate gold ",Gold).

// if some announce gold, cancel previous allocation
+gold(X,Y)[source(Ag)]
  <- .broadcast(untell, allocated(gold(X,Y),Ag));
     .abolish(gold(_,_)).


