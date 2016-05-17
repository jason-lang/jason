// CArtAgO artifact code for project cArtAgOandJasonEnv

import cartago.*;

public class SomeArt extends Artifact {
    void init(int initialValue) {
        defineObsProperty("count", initialValue);
    }
    
    @OPERATION
    void inc() {
        ObsProperty prop = getObsProperty("count");
        prop.updateValue(prop.intValue()+1);
        signal("tick");
    }
}

