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

package jason.stdlib;

import jason.asSemantics.InternalAction;


/**
  <p>Internal action: <b><code>.print</code></b>.
  
  <p>Description: used for printing messages to the console where the system
  is running. It receives any number of parameters, which can be not only
  strings but also any AgentSpeak term (including variables). Terms are made
  ground according to the current unifying function before being printed
  out. No new line is printed after the parameters.

  <p> The precise format and output device of the message is defined
  by the Java logging configuration as defined in the
  <code>logging.properties</code> file in the project directory.
  
  <p>Parameters:<ul>
  
  <li>+arg[0] ... +arg[n] (any term): the terms to be printed out.<br/>

  </ul>
  
  <p>Example:<ul> 

  <li> <code>.print(1,X,"bla")</code>: prints out to the console the
  concatenation of the string representations of the number 1, of the value of
  variable X, and the string "bla".</li>

  </ul>

  @see jason.stdlib.println

*/
public class print extends println implements InternalAction {

    private static InternalAction singleton = null;
    public static InternalAction create() {
        if (singleton == null) 
            singleton = new print();
        return singleton;
    }
    
    @Override
    protected String getNewLine() {
        return "";
    }
}
