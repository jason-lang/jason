//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// 
// To contact the authors:
// http://www.inf.ufrgs.br/~bordini
// http://www.das.ufsc.br/~jomi
//
//----------------------------------------------------------------------------


package jason.asSemantics;

import jason.asSyntax.Literal;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ActionExec implements Serializable {

    private Literal   action;
    private Intention intention;
    private boolean   result;
    private Literal   failureReason;
    private String    failureMsg;
    
    public ActionExec(Literal ac, Intention i) {
        action = ac;
        intention = i;
        result = false;
    }
    
    @Override
    public boolean equals(Object ao) {
        if (ao == null) return false;
        if (!(ao instanceof ActionExec)) return false;
        ActionExec a = (ActionExec)ao;
        return action.equals(a.action);
    }
    
    @Override
    public int hashCode() {
        return action.hashCode();
    }
   
    public Structure getActionTerm() {
        if (action instanceof Structure)
            return (Structure)action;
        else
            return new Structure(action);
    }
    
    public Intention getIntention() {
        return intention;
    }
    public boolean getResult() {
        return result;
    }
    public void setResult(boolean ok) {
        result = ok;
    }
    
    public void setFailureReason(Literal reason, String msg) {
        failureReason = reason;
        failureMsg    = msg;
    }
    public String getFailureMsg() {
        return failureMsg;
    }
    public Literal getFailureReason() {
        return failureReason;
    }
    
    public String toString() {
        return "<"+action+","+intention+","+result+">";
    }

    protected ActionExec clone() {
        ActionExec ae = new ActionExec((Pred)action.clone(), intention.clone());
        ae.result = this.result;
        return ae;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eact = (Element) document.createElement("action");
        eact.setAttribute("term", action.toString());
        eact.setAttribute("result", result+"");
        eact.setAttribute("intention", intention.getId()+"");
        return eact;
    }
}
