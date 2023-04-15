/**
 * Test plans for jason internal actions in stdlib
 *
 * Most of examples come from Jason's documentation
 */

{ include("tester_agent.asl") }

@[test]
+!test_list_files
    <-
    // no filter and ending with slash
    .list_files("./src/test/jason/inc/",L1);
    !assert_contains(L1,"./src/test/jason/inc/test_manager.asl");

    // filtering .asl files
    .list_files("./src/test/jason/inc",".*.asl",L2);
    !assert_contains(L2,"./src/test/jason/inc/tester_agent.asl");

    // filtering non existing files
    .list_files("./src/test/jason/inc",".*.xxx",L3);
    !assert_equals(0,.length(L3));

    // filtering in a more regex way for the file self_tester.asl
    .list_files("./src/test/jason/inc","self*.*",L4);
    !assert_equals(1,.length(L4));

    // listing files in a non existing folder
    .list_files("/non/existing/folder",L5);
    !assert_equals(0,.length(L5));
.
