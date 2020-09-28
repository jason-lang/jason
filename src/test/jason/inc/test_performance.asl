/**
 * Performance meter helpers
 */

/**
 * Check the performance of executing a plan P
 * take the average on N iterations
 */
@check_performance[atomic]
+!check_performance(P,N,MEAN) :
    .intention(ID,_,[ im(Label,{+!Goal[An]},{ Test; _ },_)|_],current) &
    _[code_line(Line),code_src(Src)] = Label
    <-
    -+mean(P,0);
    for (.range(J,1,N)) {
      .nano_time(T0);
      !P;
      .nano_time(T1);
      ?mean(P,M);
      -+mean(P,M+((T1-T0)/N));
    }
    ?mean(P,MEAN);
    .log(info,"check_performance on event '",Goal,"' starting at line ",Line,": ",math.round(MEAN/1000)," microseconds");
.
-!check_performance(X,Y) :
    true
    <-
    +test(Test,failed,Src,Line)[check_performance(P,N)];
.
