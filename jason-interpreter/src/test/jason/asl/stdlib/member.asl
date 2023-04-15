/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[atomic,test]
+!test_member
    <-
    !assert_true(.member(c,[a,b,c]));
    !assert_false(.member(3,[a,b,c]));
    for (.member(X,[a,b,c])) {
        !assert_contains([a,b,c],X);
    }
.


@[atomic,test]
+!test_equivalent_member
    <-
    LL = [a[test],b[test,atomic],c[atomic],d[atomic,test]];

    // First approach, using Head|Tail
    for (.member(Label,LL)) {
        Label = Name[Head|Tail];
        if ((Head == test) | (.list(Tail) & (.member(test,Tail)))) {
            !assert_contains([a,b,d],Name);
        } else {
            !assert_equals(c,Name);
        }
    }

    // Second approach, using test unification outside of member
    +count_occurrences(0);
    for (.member(Label,LL) & _[test] = Label) {
        ?count_occurrences(O);
        -+count_occurrences(O+1);
        !assert_contains([a,b,d],Name);
    }
    ?count_occurrences(O1);
    !assert_equals(3,O1);

    // Third approach, using test unification inside of member
    -+count_occurrences(0);
    for (.member(Label[test],LL)) {
        ?count_occurrences(O);
        -+count_occurrences(O+1);
        !assert_contains([a,b,d],Name);
    }
    ?count_occurrences(O2);
    !assert_equals(3,O2);
.
