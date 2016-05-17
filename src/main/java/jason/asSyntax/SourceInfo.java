package jason.asSyntax;

import java.io.Serializable;

/**
 * Store information about the file source of some term (atom, literal, etc).
 * (immutable objects)
 */
public class SourceInfo implements Serializable {

    private final String      source; 
    private final int         beginSrcLine; // the line this literal appears in the source
    private final int         endSrcLine;

    public SourceInfo(String file, int beginLine) {
        source       = file;
        beginSrcLine = beginLine;
        endSrcLine   = beginLine;
    }
    public SourceInfo(String file, int beginLine, int endLine) {
        source       = file;
        beginSrcLine = beginLine;
        endSrcLine   = endLine;
    }
    public SourceInfo(SourceInfo o) {
        if (o == null) {
            source       = null;
            beginSrcLine = 0;
            endSrcLine   = 0;            
        } else {
            source       = o.source;
            beginSrcLine = o.beginSrcLine;
            endSrcLine   = o.endSrcLine;
        }
    }

    public SourceInfo clone() {
        return this;
    }
    
    public String getSrcFile() {
        return source;
    }

    public int getSrcLine() {
        return beginSrcLine;
    }

    public int getBeginSrcLine() {
        return beginSrcLine;
    }

    public int getEndSrcLine() {
        return endSrcLine;
    }

    public  String toString() {
        return (source == null ? "nofile" : source)
        + (beginSrcLine >= 0 ? ":"+beginSrcLine : "");       
    }
}
