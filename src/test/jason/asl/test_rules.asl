/**
 * Test rules
 */

{ include("tester_agent.asl") }

new_bb(B) :- b(BB) & B = BB * 2.
new_bb2(B*2) :- b(B).

/**
 * Test
 */
@[test]
+!test_rules
    <-
    !assert_false(b(10));
    !assert_false(new_bb(20));
    !assert_false(new_bb2(20));

    -+b(10);

    !assert_true(b(10));
    !assert_true(new_bb(20));
    //!assert_true(new_bb2(20));

    for ( new_bb2(XB) ) {
        .log(warning,"TODO: Unexpected behaviour when unifying a bb from a rule ",new_bb2(XB)," - ",XB);
    }

    -b(10);

    !assert_false(b(10));
    !assert_false(new_bb(20));
    !assert_false(new_bb2(20));
.
