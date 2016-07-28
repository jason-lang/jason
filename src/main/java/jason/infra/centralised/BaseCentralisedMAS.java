//----------------------------------------------------------------------------
// Copyright (C) 2003  Rafael H. Bordini and Jomi F. Hubner
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

package jason.infra.centralised;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import jason.mas2j.MAS2JProject;
import jason.runtime.RuntimeServicesInfraTier;

/**
 * Runs MASProject using centralised infrastructure.
 */
public abstract class BaseCentralisedMAS {

    public final static String       logPropFile     = "logging.properties";
    public final static String       stopMASFileName = ".stop___MAS";
    public final static String       defaultProjectFileName = "default.mas2j";

    protected static Logger            logger        = Logger.getLogger(BaseCentralisedMAS.class.getName());
    protected static BaseCentralisedMAS runner        = null;
    protected static String            urlPrefix     = "";
    protected static boolean           readFromJAR   = false;
    protected static MAS2JProject      project;
    protected static boolean           debug         = false;
    
    protected CentralisedEnvironment        env         = null;
    protected CentralisedExecutionControl   control     = null;
    protected Map<String,CentralisedAgArch> ags         = new ConcurrentHashMap<String,CentralisedAgArch>();

    public boolean isDebug() {
        return debug;
    }
    
    public static BaseCentralisedMAS getRunner() {
        return runner;
    }

    public RuntimeServicesInfraTier getRuntimeServices() {
        return new CentralisedRuntimeServices(runner);
    }
    
    public CentralisedExecutionControl getControllerInfraTier() {
        return control;
    }

    public CentralisedEnvironment getEnvironmentInfraTier() {
        return env;
    }
    
    public MAS2JProject getProject() {
        return project;
    }
    public void setProject(MAS2JProject p) {
        project = p;
    }

    public void addAg(CentralisedAgArch ag) {
        ags.put(ag.getAgName(), ag);
    }
    public CentralisedAgArch delAg(String agName) {
        return ags.remove(agName);
    }
    
    public CentralisedAgArch getAg(String agName) {
        return ags.get(agName);
    }
    
    public Map<String,CentralisedAgArch> getAgs() {
        return ags;
    }
    
    public abstract void setupLogger();
        
    public abstract void finish();

    public abstract boolean hasDebugControl();

    public abstract void enableDebugControl();
    
    
}
