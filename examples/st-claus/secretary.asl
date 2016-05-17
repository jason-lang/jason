group([]).

// handle messages from workers and form groups
@liam1[atomic] 
+!iamhere[source(A)] : group_size(S,Kind) & group(Group) & .length(Group,S-1) 
   <- -+group([]);
      .send(santa,achieve,group(Kind,[A|Group])).

@liam2[atomic]
+!iamhere[source(A)] : group(Group) 
   <- -+group([A|Group]).
 
