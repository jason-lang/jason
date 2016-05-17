// Beliefs
resource_needed(1).

// Plans

// a resource has been dropped at site so build site further
// using that resource
//@pnr[breakpoint]
+new_resource(R,ID) : resource_needed(R)
   <- build_using(R,ID).

// a resource is not needed any more, inform collectors
// to search for another resource
+enough(R): true
   <- -resource_needed(R);
      +resource_needed(R+1);
      .broadcast(achieve, search_for(R+1)).

// just tell collectors that I finished the building
+building_finished: true
   <- .broadcast(tell,building_finished).
   
