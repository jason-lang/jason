/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_type_string
    <-
    /* check strings */
    !assert_true( .type("home page",string) );
    !assert_false( .type(b(10),string) );
    !assert_false( .type(b,string) );
    !assert_false( .type(X,string) );
    Y = "a string";
    !assert_true( .type(Y,string) );
    Z = atom;
    !assert_false( .type(Z,string) );

    /* unifying as strings */
    .type("home page",T0_0);
    !assert_equals(string,T0_0);
    .type(b(10),T0_1);
    !assert_not_equals(string,T0_1);
    .type(b,T0_2);
    !assert_not_equals(string,T0_2);

    .findall(T0_3,.type(X,T0_3),L0_3);
    !assert_not_contains(L0_3,string);

    .type(Y,T0_4);
    !assert_equals(string,T0_4);
    .type(Z,T0_5);
    !assert_not_equals(string,T0_5);
.

@[test]
+!test_type_atom
    <-
    /* check strings */
    !assert_false( .type(b(10),atom) );
    !assert_true( .type(b,atom) );
    !assert_false( .type(~b,atom) );
    !assert_false( .type(10,atom) );
    !assert_false( .type("home page",atom) );
    !assert_false( .type(X,atom) );

    Y = "a string";
    !assert_false( .type(Y,atom) );

    Z = atom;
    !assert_true( .type(Z,atom) );

    !assert_false( .type(a(X),atom) );
    !assert_false( .type(a[X],atom) );
    !assert_false( .type([a,b,c],atom) );
    !assert_false( .type([a,b,c(X)],atom) );

    /* unifying as atom */
    .type(b(10),T0);
    !assert_not_equals(atom,T0);
    .type(b,T1);
    !assert_equals(atom,T1);
    .type(~b,T2);
    !assert_not_equals(atom,T2);
    .type(10,T3);
    !assert_not_equals(atom,T3);
    .type("home page",T4);
    !assert_not_equals(atom,T4);
    .type(X,T5);
    !assert_not_equals(atom,T5);

    .type(Y,T6);
    !assert_not_equals(atom,T6);

    .type(Z,T7);
    !assert_equals(atom,T7);

    .type(a(X),T8);
    !assert_not_equals(atom,T8);
    .type(a[X],T9);
    !assert_not_equals(atom,T9);
    .type([a,b,c],T10);
    !assert_not_equals(atom,T10);
    .type([a,b,c(X)],T11);
    !assert_not_equals(atom,T11);
.

@[test]
+!test_typeof_literal
    <-
    !assert_true( .type(b(10),literal) );
    !assert_true( .type(b,literal) );
    !assert_true( .type(~b,literal) );
    !assert_false( .type(10,literal) );
    !assert_false( .type("Jason",literal) );
    !assert_false( .type(X,literal) );

    Y = "a string";
    !assert_false( .type(Y,literal) );

    Z = atom;
    !assert_true( .type(Z,literal) );

    !assert_true( .type(a(X),literal) );
    !assert_false( .type([a,b,c],literal) );
    !assert_false( .type([a,b,c(X)],literal) );

    /* unifying as string */
    .type(b(10),T0);
    !assert_equals(literal,T0);

    // This case needs backtracking
    .findall(T1, .type(b,T1) ,L1);
    !assert_contains(L1,literal);

    .type(~b,T2);
    !assert_equals(literal,T2);
    .type(10,T3);
    !assert_not_equals(literal,T3);
    .type("home page",T4);
    !assert_not_equals(literal,T4);
    .type(X,T5);
    !assert_not_equals(literal,T5);

    .type(Y,T6);
    !assert_not_equals(literal,T6);

    // This case needs backtracking
    .findall(T7, .type(b,T7) ,L7);
    !assert_contains(L7,literal);

    .type(a(X),T8);
    !assert_equals(literal,T8);
    .type(a[X],T9);
    !assert_equals(literal,T9);
    .type([a,b,c],T10);
    !assert_not_equals(literal,T10);
    .type([a,b,c(X)],T11);
    !assert_not_equals(literal,T11);
.


@[test]
+!test_type_list
    <-
    !assert_true( .type([a,b,c],list) );
    !assert_true( .type([a,b,c(X)],list) );
    !assert_false( .type(10,list) );
    !assert_false( .type("home page",list) );
    !assert_false( .type(X,list) );

    Y = "a string";
    !assert_false( .type(Y,list) );

    Z = [];
    !assert_true( .type(Z,list) );

    !assert_false( .type(a(X),list) );

    /* unifying as list */
    .type([a,b,c],T0);
    !assert_equals(list,T0);
    .type([a,b,c(X)],T1);
    !assert_equals(list,T1);
    .type(10,T2);
    !assert_not_equals(list,T2);
    .type("home page",T3);
    !assert_not_equals(list,T3);
    .type(X,T4);
    !assert_not_equals(list,T4);

    .type(Y,T5);
    !assert_not_equals(list,T5);

    .type(Z,T6);
    !assert_equals(list,T6);

    .type(a(X),T7);
    !assert_not_equals(list,T7);
.

@[test]
+!test_type_ground
    <-
    !assert_true( .type(b(10),ground) );
    !assert_true( .type(b,ground) );
    !assert_true( .type(~b,ground) );
    !assert_true( .type(10,ground) );
    !assert_true( .type("home page",ground) );
    !assert_false( .type(X,ground) );

    Y = "a string";
    !assert_true( .type(Y,ground) );

    Z = atom;
    !assert_true( .type(Z,ground) );

    !assert_false( .type(a(X),ground) );
    !assert_false( .type(a[X],ground) );
    !assert_true( .type([a,b,c],ground) );
    !assert_false( .type([a,b,c(X)],ground) );

    /* unifying as ground - many case needs backtracking*/

    .findall(T0, .type(b(10),T0) ,L0);
    !assert_contains(L0,ground);

    .findall(T1, .type(b,T1) ,L1);
    !assert_contains(L1,ground);
    .findall(T2, .type(~b,T2) ,L2);
    !assert_contains(L2,ground);
    .findall(T3, .type(10,T3) ,L3);
    !assert_contains(L3,ground);
    .findall(T4, .type("home page",T4) ,L4);
    !assert_contains(L4,ground);
    .findall(T5, .type(X,T5) ,L5);
    !assert_not_contains(L5,ground);

    .findall(T6, .type(Y,T6) ,L6);
    !assert_contains(L6,ground);

    .findall(T7, .type(Z,T7) ,L7);
    !assert_contains(L7,ground);

    .type(a(X),T8);
    !assert_not_equals(ground,T8);
    .type(a[X],T9);
    !assert_not_equals(ground,T9);
    .findall(T10, .type([a,b,c],T10) ,L10);
    !assert_contains(L10,ground);
    .type([a,b,c(X)],T11);
    !assert_not_equals(ground,T11);
.

@[test]
+!test_number
    <-
    !assert_false( .type(b(10),number) );
    !assert_false( .type(b,number) );
    !assert_false( .type(~b,number) );
    !assert_true( .type(10,number) );
    !assert_false( .type("home page",number) );
    !assert_false( .type(X,number) );

    Y = "a string";
    !assert_false( .type(Y,number) );

    Z = -400;
    !assert_true( .type(Z,number) );

    !assert_false( .type(a(X),number) );
    !assert_false( .type([1,2],number) );

    .type(b(10),T0);
    !assert_not_equals(number,T0);
    .type(b,T1);
    !assert_not_equals(number,T1);
    .type(~b,T2);
    !assert_not_equals(number,T2);
    .type(10,T3);
    !assert_equals(number,T3);
    .type("home page",T4);
    !assert_not_equals(number,T4);
    .type(X,T5);
    !assert_not_equals(number,T5);

    .type(Y,T6);
    !assert_not_equals(number,T6);

    .type(Z,T7);
    !assert_equals(number,T7);

    .type(a(X),T8);
    !assert_not_equals(number,T8);
    .type([1,2],T9);
    !assert_not_equals(number,T9);
.
