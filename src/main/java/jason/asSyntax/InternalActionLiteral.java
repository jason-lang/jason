// ----------------------------------------------------------------------------
// Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------

package jason.asSyntax;

import jason.asSemantics.Agent;
import jason.asSemantics.InternalAction;
import jason.asSemantics.Unifier;
import jason.stdlib.puts;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 A particular type of literal used to represent internal actions (which has a "." in the functor).

 @navassoc - ia - InternalAction

 */
public class InternalActionLiteral extends Structure implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(InternalActionLiteral.class.getName());
    
    private InternalAction ia = null; // reference to the object that implements the internal action, stored here to speed up the process of looking for the IA class inside the agent 
    
    public InternalActionLiteral(String functor) {
        super(functor);
    }

    // used by clone
    public InternalActionLiteral(InternalActionLiteral l) {
        super(l.getNS(), (Structure) l);
        this.ia = l.ia;
    }

    // used by capply
    private InternalActionLiteral(InternalActionLiteral l, Unifier u) {
        super((Structure) l, u);
        this.ia = l.ia;
    }

    // used by the parser
    public InternalActionLiteral(Structure p, Agent ag) throws Exception {
        this(DefaultNS, p, ag);
    }
    public InternalActionLiteral(Atom ns, Structure p, Agent ag) throws Exception {
        super(ns, p);
        if (ag != null)
            ia = ag.getIA(getFunctor());
    }
    
    @Override
    public boolean isInternalAction() {
        return true;
    }

    @Override
    public boolean isAtom() {
        return false;
    }
    
    @Override
    public Literal makeVarsAnnon(Unifier un) {
        Literal t =  super.makeVarsAnnon(un);
        if (t.getFunctor().equals(".puts")) { // vars inside strings like in .puts("bla #{X}") should be also replace
                                              // TODO: should it work for any string? if so, proceed this replacement inside StringTermImpl
            ((puts)puts.create()).makeVarsAnnon(t,un);
        }
        return t;
    }
        
    @SuppressWarnings("unchecked")
    public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un) {
        if (ag == null || ag.getTS().getUserAgArch().isRunning()) {
            try {
                /*final QueryCache qCache;
                final CacheKey kForChache;
                if (ag != null) { 
                    qCache = ag.getQueryCache();
                    if (qCache != null) {
                        kForChache = qCache.prepareForCache(this, un);
                        Iterator<Unifier> ic = qCache.getCache(kForChache);
                        if (ic != null) {
                            //ag.getLogger().info("from cache internal action "+this);
                            return ic;
                        }
                    } else {
                        kForChache = null;
                    }
                } else {
                    qCache = null;
                    kForChache = null;
                }*/

                InternalAction ia = getIA(ag);
                // calls IA's execute method
                Object oresult = ia.execute(ag.getTS(), un, ia.prepareArguments(this, un));
                if (oresult instanceof Boolean && (Boolean)oresult) {
                    /*if (kForChache != null) {
                        qCache.addAnswer(kForChache, un);
                        qCache.queryFinished(kForChache);
                    }*/
                    return LogExpr.createUnifIterator(un);
                } else if (oresult instanceof Iterator) {
                    //if (kForChache == null) {
                        return ((Iterator<Unifier>)oresult);
                    /*} else {
                        // add a wrapper to collect results into the cache
                        return qCache.queryFilter(kForChache, (Iterator<Unifier>)oresult); 
                    }*/
                }
            } catch (ConcurrentModificationException e) {
                System.out.println("*-*-* "+getFunctor()+" concurrent exception - try later");
                e.printStackTrace();
                // try again later
                try { Thread.sleep(200); } catch (InterruptedException e1) {                }
                return logicalConsequence(ag, un);
            } catch (Exception e) {
                logger.log(Level.SEVERE, getErrorMsg() + ": " + e.getMessage(), e);
            }
        }
        return LogExpr.EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }
    
    public void setIA(InternalAction ia) {
        this.ia = ia; 
    }

    public InternalAction getIA(Agent ag) throws Exception {
        if (ia == null && ag != null)
            ia = ag.getIA(getFunctor());
        return ia;
    }
    
    @Override
    public String getErrorMsg() {
        String src = getSrcInfo() == null ? "" : " ("+ getSrcInfo() + ")"; 
        return "Error in internal action '"+this+"'"+ src;      
    }
    
    @Override
    public Term capply(Unifier u) {
        return new InternalActionLiteral(this, u);
    }
    
    public InternalActionLiteral clone() {
        return new InternalActionLiteral(this);
    }

    
    /** get as XML */
    @Override
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("ia", isInternalAction()+"");
        return u;
    }    
}
