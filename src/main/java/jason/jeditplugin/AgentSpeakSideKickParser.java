 package jason.jeditplugin;

import jason.asSemantics.Agent;
import jason.asSyntax.Plan;

import java.io.StringReader;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.Buffer;

import sidekick.Asset;
import sidekick.SideKickParsedData;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

public class AgentSpeakSideKickParser extends sidekick.SideKickParser {
    public static final String ID = "asl";
    
    SideKickParsedData pd = null;

    public AgentSpeakSideKickParser() {
        super(ID);
    }
    
    public SideKickParsedData parse(Buffer buf, DefaultErrorSource errorSource) {
        String text;
        try {
            buf.readLock();
            text = buf.getText(0,buf.getLength());
        } finally {
            buf.readUnlock();
        }

        try {
            jason.asSyntax.parser.as2j parser = new jason.asSyntax.parser.as2j(new StringReader(text));
            
            Agent ag = new Agent();
            ag.initAg();
            parser.agent(ag);
            
            // create nodes 
            pd = new SideKickParsedData(buf.getName());
            for (Plan p: ag.getPL()) {
                DefaultMutableTreeNode node = new PlanAsset(p, buf).createTreeNode();
                pd.root.add(node);
            }          
                
        } catch (jason.asSyntax.parser.ParseException ex) {
            addError(ex, errorSource, buf.getPath());
        } catch (Exception e) {
            System.out.println("Error in AS SideKick:"+e);
            e.printStackTrace();
        }
        return pd;
    }

    public static void addError(jason.asSyntax.parser.ParseException ex, DefaultErrorSource errorSource, String path) {
        if (ex.currentToken != null && ex.currentToken.next != null && errorSource != null) {
            int line = ex.currentToken.next.beginLine-1;
            if (line < 0) line = 0;
            errorSource.addError(new DefaultErrorSource.DefaultError(
                    errorSource, 
                    ErrorSource.ERROR, 
                    path,
                    line, 0, 0,
                    ex.toString()));
        }       
    }
    
    public String toString() {
        return ID;
    }

    private static Icon PLAN_ICON;

    class PlanAsset extends Asset {

        private Plan plan;

        public PlanAsset(Plan p, Buffer buf) {
            //super(((p.getLabel() == null) ? "" : "@" + p.getLabel() + " ")    + p.getTriggerEvent());
            super(p.getTrigger().toString());
            this.plan  = p;
            this.start = toPos(buf, p.getSrcInfo().getBeginSrcLine());
            this.end   = toPos(buf, p.getSrcInfo().getEndSrcLine());
        }

        public Icon getIcon() {
            if (PLAN_ICON == null) {
                PLAN_ICON = new ImageIcon(AgentSpeakSideKickParser.class.getResource("/images/plan.png"));
            }
            return PLAN_ICON;
        }

        public String getShortString() {
            return name;
        }

        public String getLongString() {
            return plan.toASString();
        }

        private Position toPos(Buffer buffer, int line) {
            if ((line - 1) > buffer.getLineCount())
                return buffer.createPosition(buffer.getLength() - 1);
            int offset = buffer.getLineStartOffset(line - 1);
            if (offset >= buffer.getLength()) {
                return buffer.createPosition(buffer.getLength() - 1);
            }
            return buffer.createPosition(offset);
        }

        public DefaultMutableTreeNode createTreeNode() {
            return new DefaultMutableTreeNode(this, true);
        }
    }
}
