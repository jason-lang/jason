/* Initial goals */

!move.

/* Plans for goals */

+!move <- move; !move.

/* Plans for perception */

+battery(low) <- !charge.

+!charge <- plug; .wait({ -battery(low) }); unplug.

             // note: the plan for charge does not need to care about
             // other goals. the programmer of this plan is concerned
             // about charging and not possible concurrent goals
             // interralation between goals are handled by meta events


/* meta-plans */

// when goal charge is started, suspends goal move

^!charge[state(executing)] <- .suspend(move).
^!charge[state(finished)] <- .resume(move).
^!charge. // I am not interested in other states

// states are: started, suspended, resumed, finished, failed
// see GoalListener API doc
