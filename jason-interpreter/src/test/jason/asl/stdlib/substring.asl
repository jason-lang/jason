/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_substring
    <-
    !assert_false(.substring("b","aaa"));

    !assert_false(.substring("b","aaa",X));

    !assert_true(.substring("a","bbacc"));

    !assert_false(.substring("z","bbacc"));

    for (.substring("a","abbacca",X4)) {
        !assert_contains([0,3,6],X4);
    }

    !assert_false(.substring("a","bbacc",0));

    !assert_true(.substring(a(10),b(t1,a(10)),X));
    .substring(a(10),b(t1,a(10)),X6_1);
    !assert_equals(5,X6_1);

    .substring(a(10),b("t1,a(10),kk"),X7);
    !assert_equals(6,X7);

    !assert_true(.eval(X8, .substring(a(10,20),X,5)));
    .substring(X8_1,a(10,20),5);
    !assert_equals("20)",X8_1);

    !assert_true(.substring("20)",a(10,20),5,7));
    .substring(X9_1,a(10,20),5,7);
    !assert_equals("20",X9_1);
.

/**
 * Test if substring(_,_,0) really works as java startsWith
 */
@[atomic,test]
+!test_starts_with
    <-
    // Test startswith
    for (.member(S,["test_ss","somess_test_abc","test_j_ss"])) {
        if (.substring("test",S,0)) {
            !assert_contains(["test_ss","test_j_ss"],S);
        }
    }
    // Test endswith (using force_pass and force_failure as an example)
    for (.member(S,["test_ee","someee_test_abc","test_j_ee"])) {
        P =  .length(S) - .length("ee");
        if (.substring("ee",S,P)) {
            if ((S == "test_ee") | (S == "test_j_ee")) {
                !force_pass;
            } else {
                !force_failure("expected test_ee or test_j_ee");
            }
        }
    }
.
