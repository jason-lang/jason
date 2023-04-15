package test;

import java.io.StringReader;
import java.util.Map;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.bb.ChainBB;
import jason.mas2j.ClassParameters;
import jason.mas2j.MAS2JProject;
import jason.mas2j.parser.ParseException;
import jason.mas2j.parser.mas2j;
import junit.framework.TestCase;

/** JUnit test case for syntax package */
public class MAS2JParserTest extends TestCase {

    mas2j parser;

    protected void setUp() throws Exception {
        super.setUp();
        StringBuffer source = new StringBuffer("MAS auctionCent { \n");
        source.append("infrastructure: JaCaMo \n");
        source.append("environment: myEnv at \"x.edu\" \n");
        source.append("executionControl: myController ");
        source.append("agents: ag1 [events=discard,intBels=newFocus,osfile=\"a/x.xml\"]; ag2 /home/agTest.asl agentClass mypkg.MyAgent #2; ag3 at \"x.edu\"; auctionner agentArchClass AuctionnerGUI;");
        source.append("directives: md1=mypkg.DebugDirective; md2=mypkg.LogDirective;");
        source.append("classpath: \"x.jar\"; \"../../\";");
        source.append("aslSourcePath: \"kk\"; \".\";");
        source.append("}");
        parser = new mas2j(new StringReader(source.toString()));
    }

    public void testToString() {
        boolean ok = true;
        try {
            MAS2JProject project = parser.mas();
            //System.out.println(project);
            parser = new mas2j(new StringReader(project.toString()));
            parser.mas();
        } catch (Exception e) {
            System.err.println("Error:"+e);
            e.printStackTrace();
            ok = false;
        }
        assertTrue(ok);
    }

    @SuppressWarnings("unchecked")
    public void testParser1() {
        try {
            MAS2JProject project = parser.mas();
            project.setDirectory("/tmp");

            //project.writeXMLScript(System.out);
            //project.writeScripts(true);
            Map ag1Opt = project.getAg("ag1").getOptions();
            assertEquals(ag1Opt.size(),3);

            assertEquals(project.getDirectiveClasses().size(),2);
            assertEquals(project.getDirectiveClasses().get("md2").toString(), "mypkg.LogDirective");
            assertEquals("JaCaMo", project.getInfrastructure().getClassName());

            // jacamo is removed
            //String facClass = Config.get().getInfrastructureFactoryClass(project.getInfrastructure().getClassName());
            //assertEquals("jacamo.infra.JaCaMoInfrastructureFactory", facClass);

            //System.out.println(project.getInfrastructureFactory());
        } catch (Exception e) {
            System.err.println("Error:"+e);
            e.printStackTrace();
        }
    }

    public void testClassParForChainBB() throws Exception {
        StringBuffer source = new StringBuffer("MAS auctionCent { ");
        source.append("agents: bob beliefBaseClass jason.bb.ChainBB(jason.bb.TextPersistentBB, ");
        source.append("jason.bb.IndexedBB(\"student(key,_)\", \"depot(_,_,_)\"));");
        source.append(" }");
        parser = new mas2j(new StringReader(source.toString()));

        MAS2JProject project = parser.mas();
        assertEquals(2,  project.getAg("bob").getBBClass().getParametersArray().length);

        ChainBB bb = new ChainBB();
        bb.init(null, project.getAg("bob").getBBClass().getParametersArray());
        bb.add(Literal.parseLiteral("b(1)"));
    }

    public void testClassDef1() throws ParseException {
        String archClass = "my.Arch(test)";
        mas2j parser = new mas2j(new StringReader(archClass));
        ClassParameters c = parser.classDef();
        assertEquals("my.Arch", c.getClassName());
        assertEquals(1,c.getParametersArray().length);
    }
    public void testClassDef2() throws ParseException, jason.asSyntax.parser.ParseException {
        String archClass = "my.Arch()";
        mas2j parser = new mas2j(new StringReader(archClass));
        ClassParameters c = parser.classDef();
        assertEquals("my.Arch", c.getClassName());
        assertEquals(0,c.getParametersArray().length);

        Structure s = ASSyntax.parseStructure(c.toString()); // a classDef should be parsed as a structure
        assertEquals("my.Arch", s.toString());
    }
}
