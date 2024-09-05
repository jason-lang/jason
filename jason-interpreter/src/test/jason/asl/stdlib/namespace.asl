/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_namespace
    <-
    !assert_false( .namespace(non_existing) );

    +a(1);
    +family::brother(bob);
    +financial::account(1234);

    !assert_true( .namespace(family) );
    !assert_true( .namespace(financial) );

    .namespace_set_prop(family, uri, "http://xxx.com");
    .namespace_set_prop(family, size, 4);
    .namespace_set_prop(family, root, bob);

    .namespace_get_prop(family, uri, URI);
    !assert_equals("http://xxx.com", URI);

    .namespace_get_prop(family, kk, KK, k1);
    !assert_equals(k1, KK);

    .findall( K, .namespace_get_prop(family, K),  LK);
    !assert_equals([uri,root,size], LK);

    .findall( [K,V], .namespace_get_prop(family, K, V),  LKV);
    !assert_equals([[uri,"http://xxx.com"],[root,bob],[size,4]], LKV);

.
