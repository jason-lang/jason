import java.util.*;

import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;

public class MySend extends jason.stdlib.send {

    // ensures the intention will be suspended after send
    @Override
    public boolean suspendIntention() {
         return true;
    }
}
