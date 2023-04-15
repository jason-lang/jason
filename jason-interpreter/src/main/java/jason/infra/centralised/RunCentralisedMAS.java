package jason.infra.centralised;

import jason.JasonException;
import jason.infra.local.RunLocalMAS;

/**
 * Runs MASProject using centralised infrastructure.
 *
 * @deprecated use RunLocalMas instead
 */
@Deprecated
public class RunCentralisedMAS extends RunLocalMAS {

    public static void main(String[] args) throws JasonException {
        System.err.println("RunCentralisedMAS sas renamed to RunLocalMAS ***********");
        RunLocalMAS.main(args);
    }

}
