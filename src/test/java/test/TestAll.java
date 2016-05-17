package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ASParserTest.class,
    BeliefBaseTest.class,
    ExprTermTest.class,
    JadeAgTest.class, 
    ListTermTest.class,
    MAS2JParserTest.class,
    PlanTest.class,
    RuleTest.class,
    StdLibTest.class,
    StringTermTest.class,
    TermTest.class,
    TSTest.class, 
    VarTermTest.class,
    NSTest.class
 })
public class TestAll { }
