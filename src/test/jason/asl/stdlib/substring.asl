/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of then comes from examples of Jason's documentation
 */

{ include("test_assert.asl") }

!execute_test_plans.

@test_substring[atomic]
+!test_substring
    <-
    !assert_false(.substring("b","aaa"));

    !assert_false(.substring("b","aaa",X));

    !assert_true(.substring("a","bbacc"));

    !assert_false(.substring("z","bbacc"));

    for (.substring("a","abbacca",X4)) {
      if ((X4 == 0) | (X4 == 3) | (X4 == 6)) {
        !assert_true(true);
      } else {
        !assert_equals("not expected","0");
      }
    }

    .print("TODO: it was expected false");
    !assert_false(.substring("a","bbacc",0));

    !assert_true(.substring(a(10),b(t1,a(10)),X));
    .substring(a(10),b(t1,a(10)),X6_1);
    !assert_equals(5,X6_1);

    .substring(a(10),b("t1,a(10),kk"),X7);
    !assert_equals(6,X7);

    !assert_true(.eval(X8, .substring(a(10,20),X,5)));
    .substring(a(10,20),X8_1,5);
    !assert_equals("20)",X8_1);

    .print("TODO: it was expected true");
    !assert_true(.substring("20)",a(10,20),5,7));
    .substring(a(10,20),X9_1,5,7);
    !assert_equals("20",X9_1);
.

/**
 * Test if substring(_,_,0) really works as java startsWith
 */
@test_starts_with[atomic]
+!test_starts_with
    <-
    // Test startswith
    for (.member(S,["test_ss","somess_test_abc","test_j_ss"])) {
      if (.substring("test",S,0)) {
        if ((S == "test_ss") | (S == "test_j_ss")) {
          !assert_true(true);
        } else {
          .print("TODO: No fails are extected");
          !assert_true(false);
        }
      }
    }
    // Test endswith
    for (.member(S,["test_ee","someee_test_abc","test_j_ee"])) {
      P =  .length(S) - .length("ee");
      if (.substring("ee",S,P)) {
        if ((S == "test_ee") | (S == "test_j_ee")) {
          !assert_true(true);
        } else {
          .print("TODO: No fails are extected");
          !assert_true(false);
        }
      }
    }
.
