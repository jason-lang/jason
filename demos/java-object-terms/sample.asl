// Agent sample in project ObjectTerm

/* Initial goals */

!start.

/* Plans */

+!start : true 
   <- date.today(T);           // Create a term T that is a java Calendar object
                               // note that all operation on T should be done by
                               // internal actions
      .print("Today=", T);
      date.add_days(T,20,T2);  // change the java object 
      .print("more 20 days=",T2);
      +test(T).                // to see the date in BB (and mind inspector)

