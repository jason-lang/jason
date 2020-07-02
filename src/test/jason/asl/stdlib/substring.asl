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
    .eval(X1, .substring("b","aaa"));
    !assert_false(X1);

    .eval(X2, .substring("b","aaa",X));
    !assert_false(X2);

    .eval(X3, .substring("a","bbacc"));
    !assert_true(X3);

    for (.substring("a","abbacca",X4)) {
      if ((X4 == 0) | (X4 == 3) | (X4 == 6)) {
        !assert_true(true);
      } else {
        !assert_true(false);
      }
    }

    .print("TODO: it was expected false");
    .eval(X5, .substring("a","bbacc",0));
    !assert_false(X5);

    .eval(X6, .substring(a(10),b(t1,a(10)),X));
    !assert_true(X6);
    .substring(a(10),b(t1,a(10)),X6_1);
    !assert_equals(5,X6_1);

    .substring(a(10),b("t1,a(10),kk"),X7);
    !assert_equals(6,X7);

    .eval(X8, .substring(a(10,20),X,5));
    !assert_true(X8);
    .substring(a(10,20),X8_1,5);
    !assert_equals("20)",X8_1);

    .print("TODO: it was expected true");
    .eval(X9, .substring(a(10,20),X,5,7));
    !assert_true(X9);
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
