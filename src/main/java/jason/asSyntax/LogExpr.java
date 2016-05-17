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
import jason.asSemantics.Unifier;
import jason.asSyntax.parser.as2j;

import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/** 
   Represents a logical formula with some logical operator ("&amp;",  "|", "not").

   @navassoc - op - LogicalOp
   
 */
public class LogExpr extends BinaryStructure implements LogicalFormula {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(LogExpr.class.getName());

    public static final List<Unifier> EMPTY_UNIF_LIST = Collections.emptyList();

    public enum LogicalOp { 
        none   { public String toString() { return ""; } }, 
        not    { public String toString() { return "not "; } }, 
        and    { public String toString() { return " & "; } },
        or     { public String toString() { return " | "; } };
    }

    private  LogicalOp op = LogicalOp.none;
    
    public LogExpr(LogicalFormula f1, LogicalOp oper, LogicalFormula f2) {
        super(f1, oper.toString(), f2);
        op = oper;
    }

    public LogExpr(LogicalOp oper, LogicalFormula f) {
        super(oper.toString(),(Term)f);
        op = oper;
    }

    /** gets the LHS of this Expression */
    public LogicalFormula getLHS() {
        return (LogicalFormula)getTerm(0);
    }
    
    /** gets the RHS of this Expression */
    public LogicalFormula getRHS() {
        return (LogicalFormula)getTerm(1);
    }

    public Iterator<Unifier> logicalConsequence(final Agent ag, final Unifier un) {
        try {
            /*final QueryCache qCache;
            final CacheKey kForChache;
            if (ag != null && (op == LogicalOp.and || op == LogicalOp.or)) { 
                qCache = null; //ag.getQueryCache();
                if (qCache != null) {
                    kForChache = qCache.prepareForCache(this, un);
                    Iterator<Unifier> ic = qCache.getCache(kForChache);
                    if (ic != null) {
                        //ag.getLogger().info("from cache expression!"+this);
                        return ic;
                    }
                } else {
                    kForChache = null;
                }
            } else {
                qCache = null;
                kForChache = null;
            }
            */
            
            switch (op) {
            
            case none: break;
            
            case not:
                if (!getLHS().logicalConsequence(ag,un).hasNext()) {
                    return createUnifIterator(un);
                }
                break;
            
            case and:
                return new Iterator<Unifier>() {
                    Iterator<Unifier> ileft   = getLHS().logicalConsequence(ag,un);;
                    Iterator<Unifier> iright  = null;
                    Unifier           current = null;
                    boolean           needsUpdate = true;
                    
                    public boolean hasNext() {
                        if (needsUpdate) 
                            get();
                        //if (kForChache != null && current == null) 
                        //    qCache.queryFinished(kForChache);
                        return current != null;
                    }
                    public Unifier next() {
                        if (needsUpdate)
                            get();
                        //Unifier a = current;
                        if (current != null)
                            needsUpdate = true;
                        //if (kForChache != null) 
                        //    qCache.addAnswer(kForChache, current);
                        return current;
                    }
                    private void get() {
                        needsUpdate = false;
                        current     = null;
                        while ((iright == null || !iright.hasNext()) && ileft.hasNext())
                            iright = getRHS().logicalConsequence(ag, ileft.next());
                        if (iright != null && iright.hasNext())
                            current = iright.next();
                    }
                    public void remove() {}
                };
                
            case or:
                return new Iterator<Unifier>() {
                    Iterator<Unifier> ileft  = getLHS().logicalConsequence(ag,un);
                    Iterator<Unifier> iright = null;
                    Unifier current          = null;
                    boolean needsUpdate      = true;
                    
                    public boolean hasNext() {
                        if (needsUpdate) 
                            get();
                        //if (kForChache != null && current == null) 
                        //    qCache.queryFinished(kForChache);
                        return current != null;
                    }
                    public Unifier next() {
                        if (needsUpdate) 
                            get();
                        //Unifier a = current;
                        if (current != null)
                            needsUpdate = true;
                        //if (kForChache != null) 
                        //    qCache.addAnswer(kForChache, current);
                        return current;
                    }
                    private void get() {
                        needsUpdate = false;
                        current     = null;
                        if (ileft != null && ileft.hasNext())
                            current = ileft.next();
                        else {
                            if (iright == null)
                                iright = getRHS().logicalConsequence(ag,un);
                            if (iright != null && iright.hasNext())
                                current = iright.next();
                        }
                    }
                    public void remove() {}
                };
            }
        } catch (Exception e) {
            String slhs = "is null ";
            Iterator<Unifier> i = getLHS().logicalConsequence(ag,un);
            if (i != null) {
                slhs = "";
                while (i.hasNext()) {
                    slhs += i.next().toString()+", ";
                }
            } else {
                slhs = "iterator is null";
            }
            String srhs = "is null ";
            if (!isUnary()) {
                i = getRHS().logicalConsequence(ag,un);
                if (i != null) {
                    srhs = "";
                    while (i.hasNext()) {
                        srhs += i.next().toString()+", ";
                    }
                } else {
                    srhs = "iterator is null";
                }
            } 
            
            logger.log(Level.SEVERE, "Error evaluating expression "+this+". \nlhs elements="+slhs+". \nrhs elements="+srhs,e);
        }
        return EMPTY_UNIF_LIST.iterator();  // empty iterator for unifier
    }   

    /** creates an iterator for a list of unifiers */
    static public Iterator<Unifier> createUnifIterator(final Unifier... unifs) {
        return new Iterator<Unifier>() {
            int i = 0;
            public boolean hasNext() {
                return i < unifs.length;
            }
            public Unifier next() {
                return unifs[i++];
            }
            public void remove() {}
        };
    }

    /** returns some LogicalFormula that can be evaluated */
    public static LogicalFormula parseExpr(String sExpr) {
        as2j parser = new as2j(new StringReader(sExpr));
        try {
            return (LogicalFormula)parser.log_expr();
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Error parsing expression "+sExpr,e);
        }
        return null;
    }
    
    @Override
    public Term capply(Unifier u) {
        // do not call constructor with term parameter!
        if (isUnary())
            return new LogExpr(op, (LogicalFormula)getTerm(0).capply(u));
        else
            return new LogExpr((LogicalFormula)getTerm(0).capply(u), op, (LogicalFormula)getTerm(1).capply(u));
    }
    
    /** make a hard copy of the terms */
    public LogicalFormula clone() {
        // do not call constructor with term parameter!
        if (isUnary())
            return new LogExpr(op, (LogicalFormula)getTerm(0).clone());
        else
            return new LogExpr((LogicalFormula)getTerm(0).clone(), op, (LogicalFormula)getTerm(1).clone());
    }
    

    /** gets the Operation of this Expression */
    public LogicalOp getOp() {
        return op;
    }
    
    /** get as XML */
    public Element getAsDOM(Document document) {
        Element u = super.getAsDOM(document);
        u.setAttribute("type","logical");
        return u;
    }
    
}
