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

import jason.asSyntax.Trigger;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    Trigger   trigger   = null;
    Intention intention = Intention.EmptyInt;
    
    public Event(Trigger t, Intention i) {
        trigger   = t;
        intention = i;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public Intention getIntention() {
        return intention;
    }
    public void setIntention(Intention i) {
        intention = i;
    }
    
    public boolean sameTE(Object t) {
        return trigger.equals(t);
    }

    public boolean isExternal() {
        return intention == Intention.EmptyInt;
    }
    public boolean isInternal() {
        return intention != Intention.EmptyInt;
    }
    public boolean isAtomic() {
        return intention != null && intention.isAtomic();
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Event) {
            Event oe = (Event)o;
            if (this.intention == null && oe.intention != null) return false;
            if (this.intention != null && !this.intention.equals(oe.intention)) return false;
                
            return this.trigger.equals(oe.trigger);
        }
        return false;
    }

    public Object clone() {
        Trigger   tc = (trigger   == null ? null : (Trigger)trigger.clone());
        Intention ic = (intention == null ? null : (Intention)intention.clone());
        return new Event(tc, ic);
    }
    
    public String toString() {
        if (intention == Intention.EmptyInt)
            return ""+trigger;
        else
            return trigger+"\n"+intention;
    }

    /** get as XML */
    public Element getAsDOM(Document document) {
        Element eevt = (Element) document.createElement("event");
        eevt.appendChild(trigger.getAsDOM(document));
        if (intention != Intention.EmptyInt) {
            eevt.setAttribute("intention", intention.getId()+"");
        }
        return eevt;
    }

}
