// leader agent

/* quadrant allocation */

@quads[atomic]
+gsize(S,W,H) : true
  <- // calculate the area of each quadrant and remember them
     .print("Defining quadrants for ",W,"x",H," simulation ",S);
     +quad(S,1, 0, 0, W div 2 - 1, H div 2 - 1);
     +quad(S,2, W div 2, 0, W-1, H div 2 - 1);
     +quad(S,3, 0, H div 2, W div 2 - 1, H - 1);
     +quad(S,4, W div 2, H div 2, W - 1, H - 1);
     .print("Finished all quadrs for ",S).

+init_pos(S,X,Y)[source(A)]
  :  // if all miners have sent their position
     .count(init_pos(S,_,_),4)
  <- .print("* InitPos ",A," is ",X,"x",Y);
     // remember who doesn't have a quadrant allocated
     // (initially all miners)
     +~quad(S,miner1); +~quad(S,miner2);
     +~quad(S,miner3); +~quad(S,miner4);
     !assign_all_quads(S,[1,2,3,4]).
+init_pos(S,X,Y)[source(A)] 
  <- .print("- InitPos ",A," is ",X,"x",Y).

  
+!assign_all_quads(_,[]).
+!assign_all_quads(S,[Q|T])
  <- !assign_quad(S,Q);
     !assign_all_quads(S,T).

// assign quadrant Q to a miner     
+!assign_quad(S,Q) 
  :  quad(S,Q,X1,Y1,X2,Y2) & 
     ~quad(S,_) // there still is a miner without quadrant 
  <- .findall(Ag, ~quad(S,Ag), LAgs);
     !calc_ag_dist(S,Q,LAgs,LD);
     .min(LD,d(Dist,Ag)); 
     .print(Ag, "'s Quadrant is: ",Q);
     -~quad(S,Ag);
     .send(Ag,tell,quadrant(X1,Y1,X2,Y2)).

+!calc_ag_dist(S,Q,[],[]).
+!calc_ag_dist(S,Q,[Ag|RAg],[d(Dist,Ag)|RDist]) 
  :  quad(S,Q,X1,Y1,X2,Y2) & init_pos(S,AgX,AgY)[source(Ag)]
  <- // get the distance between X1,Y1 and AgX,AgY
     jia.dist(X1,Y1,AgX,AgY,Dist);
     !calc_ag_dist(S,Q,RAg,RDist).


/* negotiation for found gold */

+bid(Gold,D,Ag)
  :  .count(bid(Gold,_,_),3)  // three bids were received
  <- .print("bid from ",Ag," for ",Gold," is ",D);
     !allocate_miner(Gold);
     .abolish(bid(Gold,_,_)).
+bid(Gold,D,Ag)
  <- .print("bid from ",Ag," for ",Gold," is ",D).
 
+!allocate_miner(Gold) 
  <- .findall(op(Dist,A),bid(Gold,Dist,A),LD);
     .min(LD,op(DistCloser,Closer));
     DistCloser < 10000;
     .print("Gold ",Gold," was allocated to ",Closer, " options were ",LD);
     .broadcast(tell,allocated(Gold,Closer)).
     //-Gold[source(_)].
-!allocate_miner(Gold)
  <- .print("could not allocate gold ",Gold).


/* end of simulation plans */     

@end[atomic]
+end_of_simulation(S,_) : true 
  <- .print("-- END ",S," --");
     .abolish(init_pos(S,_,_)).

