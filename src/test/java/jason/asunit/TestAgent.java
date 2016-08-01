package jason.asunit;

import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.logging.Level;

import jason.JasonException;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Structure;
import jason.asSyntax.Trigger;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.infra.centralised.RunCentralisedMAS;
import jason.util.Config;

public class TestAgent extends Agent {
    
    // creates the masRunner
    static {
        new RunCentralisedMAS();
        Config.get().setProperty(Config.START_WEB_MI,  "false");
    }

    
    public TestAgent() {
        this(null);
    }
    
    public TestAgent(String agName) {
        try {
            TestArch arch = null;
            if (agName == null)
                arch = new TestArch();
            else
                arch = new TestArch(agName);
            new TransitionSystem(this, null, null, arch);
            arch.insertAgArch(arch);
            initAg(); 
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating TestArch", e);
        }
    }
    
    public boolean parseAScode(String aslCode) {
        try {
            getPL().clear(); // to force KQML plans to be after string plans
            setASLSrc("stringcode");
            parseAS(new StringReader(aslCode));
            addInitialBelsInBB();
            addInitialGoalsInTS();
            
            // kqml Plans at the end of the ag PS
            setASLSrc("kqmlPlans.asl");
            parseAS(JasonException.class.getResource("/asl/kqmlPlans.asl"));
            setASLSrc("stringcode");
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing\n"+aslCode+": "+e.getMessage());
            return false;
        }
    }

    public TestArch getArch() {
        return (TestArch)getTS().getUserAgArch();
    }
    
    public void setDebugMode(boolean on) {
        if (on) {
            getTS().getLogger().setLevel(Level.FINE);
            getArch().getLogger().setLevel(Level.FINE);
            getTS().getAg().getLogger().setLevel(Level.FINE);
        } else
            getTS().getLogger().setLevel(Level.INFO);            
    }
    
    // --------------------------------------------
    //   methods to change the state of the agent
    // --------------------------------------------
    
    public void addGoal(String g) {
        try {
            addGoal(ASSyntax.parseLiteral(g));
        } catch (Exception e) {
            fail("Parsing '"+g+"' as literal for a goal failed!");
        }
    }
    public void addGoal(Literal g) {
        getTS().getC().addAchvGoal(g, Intention.EmptyInt);
    }
    
    public void addBel(String bel) {
        try {
            super.addBel(ASSyntax.parseLiteral(bel));
        } catch (ParseException e) {
            fail("Parsing '"+bel+"' as a belief!");
        } catch (RevisionFailedException e) {
            fail("BRF error for adding '"+bel+"!");
        }        
    }
    public void delBel(String bel) {
        try {
            Literal l = ASSyntax.parseLiteral(bel);
            if (!l.hasSource()) {
                l.addAnnot(BeliefBase.TSelf);
            }
            super.delBel(l);
        } catch (ParseException e) {
            fail("Parsing '"+bel+"' as a belief!");
        } catch (RevisionFailedException e) {
            fail("BRF error for deleting '"+bel+"!");
        }        
    }
    
    // --------------------------------------------
    //   assert methods
    // --------------------------------------------
    
    public void assertBel(String formula, int maxCycles) {
        try {
            assertBel(ASSyntax.parseFormula(formula), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+formula+"' as a formula failed!");
        }
    }
    public void assertBel(final LogicalFormula belief, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return believes(belief, new Unifier());
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertBel("+belief+")");
    }
    
    
    public void assertEvt(String te, int maxCycles) {
        try {
            assertEvt(ASSyntax.parseTrigger(te), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+te+"' as trigger failed!");
        }
    }
    public void assertEvt(Trigger te, final int maxCycles) {
        final Event evt = new Event(te, Intention.EmptyInt);
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return getTS().getC().getEvents().contains(evt);
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertEvt("+te+")");
    }
    

    public void assertAct(String act, int maxCycles) {
        try {
            assertAct(ASSyntax.parseStructure(act), maxCycles);
        } catch (ParseException e) {
            fail("Parsing '"+act+"' as action failed!");
        }
    }
    public void assertAct(final Structure act, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return arch.getActions().contains(act);
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertAct("+act+")");
    }
    
    
    public void assertIdle(final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return getTS().canSleep();
            }
        };
        if (!assertMaxCyclesAndAnotherCondition(c, maxCycles))
            fail("failed assertIdle");
    }

    public void assertPrint(final String out, final int maxCycles) {
        Condition c = new Condition() {
            public boolean test(TestArch arch) {
                return arch.getOutput().indexOf(out) >= 0;
            }
        };
        if (assertMaxCyclesAndAnotherCondition(c, maxCycles))
            getArch().clearOutput();
        else
            fail("failed assertPrint("+out+")");
    }
    
    
    private boolean assertMaxCyclesAndAnotherCondition(final Condition c, final int maxCycles) {
        if (maxCycles <= 0)
            return c.test(getArch());
        try {
            Condition mc = new Condition() {
                public boolean test(TestArch arch) {
                    return arch.getCycle() < maxCycles && !c.test(arch);
                }
            };
            synchronized (mc) {
                getArch().start(mc);
                mc.wait();
            }
            return c.test(getArch());
        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
            e.printStackTrace();
        }
        return false;
    }
    
}
