/**
 * Test unification
 */

{ include("tester_agent.asl") }

/**
 * Test if the incognito at while( incognito ) {} is destroyed and recreated
 * every iteration allowing multiple unifications
 */
@[test]
+!test_incognito_update_on_while
    <-
    -+step(1); // give an arbitrary value to step(_)
    +still_wait;
    while (still_wait & step(S)) {
        -step(_); // retrieve S value from the unification done at while () {}
        +step(S+1); // retrieve S value from the unification done at while () {}
        if (S == 3) {
            -still_wait;
            +stopped_at(S);
        }
    }
    !assert_not_equals(step(4),step(S)); // step(S) is not acessible from out of while () {}
    ?step(S); // let us retrieve it again
    !assert_equals(step(4),step(S)); // step(S) is still acessible out of while () {}
    !assert_true(stopped_at(3));
.
