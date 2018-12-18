package jason.bb;

import static jason.asSyntax.ASSyntax.createNumber;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import jason.asSemantics.Agent;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.asSyntax.parser.TokenMgrError;

/**
  Implementation of BB that stores some beliefs in a relational data base.

  <p>The parameters for this customisation are:
  <ul>
  <li>args[0] is the Database Engine JDBC drive
  <li>args[1] is the JDBC URL connection string <br/>
      The url can use the agent name as parameter as in "jdbc:mysql://localhost/%s".
      In this case, %s will be replaced by the agent's name.
  <li>args[2] is the username
  <li>args[3] is the password
  <li>args[4] is an AS list with all beliefs that are mapped to DB.
      Each element of the list is in the form
      <br/><br/>
      <code>predicate(arity [, table_name [, columns]])</code>
      <br/><br/>
      and columns is in the form
      <br/><br/>
      <code>columns( col_name(col_type), col_name(col_type), ....)</code>
      <br/><br/>
  </ul>

  <p>Example in .mas2j project, the agent c uses a JDBC belief base:<br>
  <br/>
  <pre>
 agents:
   c beliefBaseClass jason.bb.JDBCPersistentBB(
      "org.hsqldb.jdbcDriver", // driver for HSQLDB
      "jdbc:hsqldb:bookstore", // URL connection
      "sa", // user
      "", // password
      "[a(1,tablea,columns(runs(integer))),
        book(5),
        book_author(2),
        author(2,author,columns(id(integer),name(varchar(30)))),
        publisher(2)]");
   </pre>

   <p>The predicate <code>a/1</code> is mapped to a table called "tablea" with an integer column called runs;
   predicate <code>book</code> (with arity 5) is mapped to a table called "book"; and so on.


   <p>The name and type of the columns are used only if the table does not exits and have to be created.
      If no column name/type is provided, an arbitrary name is used with type varchar(256).
      If no table name is provided, the predicate name is used for the table name.
   <br/>

   @author Jomi
 */
public class JDBCPersistentBB extends ChainBBAdapter {
    private static Logger logger     = Logger.getLogger(JDBCPersistentBB.class.getName());

    // TODO: manage namespace
    static final String   COL_PREFIX = "term";
    static final String   COL_NEG    = "j_negated";
    static final String   COL_ANNOT  = "j_annots";

    /** the number of columns that this customisation creates (default is 2: the j_negated and j_annots columns) */
    protected int extraCols = 0;

    protected Connection  conn;
    protected String      url;
    protected String      agentName;

    public JDBCPersistentBB() {
        extraCols = 2;
    }
    public JDBCPersistentBB(BeliefBase next) {
        super(next);
        extraCols = 2;
    }


    // map of bels in DB
    protected Map<PredicateIndicator, ResultSetMetaData> belsDB = new HashMap<PredicateIndicator, ResultSetMetaData>();

    @Override
    public void init(Agent ag, String[] args) {
        try {
            agentName = ag.getTS().getUserAgArch().getAgName();
        } catch (Exception e) {
            logger.warning("Can not get the agent name!");
            agentName = "none";
        }
        try {
            logger.fine("Loading driver " + args[0]);
            Class.forName(args[0]);
            url = String.format(args[1], agentName);
            logger.fine("Connecting: url= " + url + ", user=" + args[2] + ", password=" + args[3]);
            conn = DriverManager.getConnection(url, args[2], args[3]);

            // load tables mapped to DB
            ListTerm lt = ListTermImpl.parseList(args[4]);
            for (Term t : lt) {
                Structure ts = (Structure)t;
                int arity    = Integer.parseInt(ts.getTerm(0).toString());
                String table = ts.getFunctor();
                if (ts.getArity() >= 2) {
                    table = ts.getTerm(1).toString();
                }

                Structure columns = new Structure("columns");
                if (ts.getArity() >= 3) {
                    columns = (Structure)ts.getTerm(2);
                }

                // create the table and get its Metadata
                Statement stmt = conn.createStatement();
                ResultSet rs;
                try {
                    rs = stmt.executeQuery("select * from " + table);
                } catch (SQLException e) {
                    // create table
                    stmt.executeUpdate(getCreateTable(table, arity, columns));
                    rs = stmt.executeQuery("select * from " + table);
                }
                belsDB.put(new PredicateIndicator(ts.getFunctor(), arity), rs.getMetaData());
                belsDB.put(new PredicateIndicator("~"+ts.getFunctor(), arity), rs.getMetaData());
                stmt.close();
            }
            //logger.fine("Map=" + belsDB);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.log(Level.SEVERE, "Wrong parameters for JDBCPersistentBB initialisation.", e);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading jdbc driver " + args[0], e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "DB connection failure. url= " + url + ", user=" + args[2] + ", password=" + args[3], e);
        }
        nextBB.init(ag, args);
    }

    @Override
    public void stop() {
        if (conn == null) return;

        try {
            if (url.startsWith("jdbc:hsqldb")) {
                conn.createStatement().execute("SHUTDOWN");
            }
            conn.close(); // if there are no other open connection
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in shutdown SGBD ", e);
        }
        nextBB.stop();
    }

    public void clear() {
        logger.warning("clear is still not implemented for JDBC BB!");
    }

    /** returns true if the literal is stored in a DB */
    protected boolean isDB(Literal l) {
        return belsDB.get(l.getPredicateIndicator()) != null;
    }

    /** returns true if the table for pi was created by Jason */
    protected boolean isCreatedByJason(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        if (meta != null) {
            int cols = meta.getColumnCount();
            return cols >= extraCols &&
                   meta.getColumnName((cols - extraCols) + 1).equalsIgnoreCase(COL_NEG) &&
                   meta.getColumnName((cols - extraCols) + 2).equalsIgnoreCase(COL_ANNOT);
        }
        return false;
    }

    @Override
    public Literal contains(Literal l) {
        if (!isDB(l))
            return nextBB.contains(l);

        Statement stmt = null;
        try {
            // create a literal from query
            stmt = conn.createStatement();
            String q = getSelect(l);
            if (logger.isLoggable(Level.FINE)) logger.fine("query for contains "+l+":"+q);
            ResultSet rs = stmt.executeQuery(q);
            if (rs.next()) {
                return resultSetToLiteral(rs,l.getPredicateIndicator());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
            //} catch (ParseException e) {
            //    logger.log(Level.SEVERE, "Parser Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return null;
    }

    @Override
    public boolean add(Literal l) {
        return add(0, l);
    }

    @Override
    public boolean add(int index, Literal l) {
        if (!isDB(l))
            return nextBB.add(l);
        if (index != 0)
            logger.severe("JDBC BB does not support insert index "+index+" for "+l+", using index = 0!");

        Literal bl = contains(l);
        Statement stmt = null;
        try {
            if (bl != null) {
                if (isCreatedByJason(l.getPredicateIndicator())) {
                    // add only annots
                    if (l.hasSubsetAnnot(bl))
                        // the current bel bl already has l's annots
                        return false;
                    else {
                        // "import" annots from the new bel
                        bl.importAnnots(l);

                        // check if it needs to be added in the percepts list
                        if (l.hasAnnot(TPercept)) {
                            getDBBPercepts().add(bl);
                        }

                        // store bl annots
                        stmt = conn.createStatement();
                        String q = "update "+getTableName(bl)+" set "+COL_ANNOT+" = '"+bl.getAnnots()+"' "+getWhere(l);
                        if (logger.isLoggable(Level.FINE)) logger.fine("query for update "+q);
                        stmt.executeUpdate(q);
                        return true;
                    }
                }
            } else {
                // create insert command
                stmt = conn.createStatement();
                if (logger.isLoggable(Level.FINE)) logger.fine("query for insert "+getInsert(l));
                stmt.executeUpdate(getInsert(l));
                // add it in the percepts list
                if (l.hasAnnot(TPercept)) {
                    getDBBPercepts().add(l);
                }
                return true;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return false;
    }

    @Override
    public boolean remove(Literal l) {
        if (!isDB(l))
            return nextBB.remove(l);

        Literal bl = contains(l);
        if (bl != null) {
            Statement stmt = null;
            try {
                if (l.hasSubsetAnnot(bl)) {
                    if (l.hasAnnot(TPercept)) {
                        getDBBPercepts().remove(bl);
                    }
                    boolean result = bl.delAnnots(l.getAnnots()) || !bl.hasAnnot();
                    stmt = conn.createStatement();
                    if (bl.hasAnnot() && isCreatedByJason(l.getPredicateIndicator())) {
                        // store new bl annots
                        stmt.executeUpdate("update "+getTableName(bl)+" set "+COL_ANNOT+" = '"+bl.getAnnots()+"' "+getWhere(l));
                    } else {
                        // remove from DB
                        stmt.executeUpdate("delete from "+getTableName(bl)+getWhere(bl));
                    }
                    return result;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Error", e);
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "SQL Error closing connection", e);
                }
            }
        }
        return false;
    }

    private Set<Literal> getDBBPercepts() {
        BeliefBase last = getLastBB();
        if (last instanceof DefaultBeliefBase)
            return ((DefaultBeliefBase)last).getPerceptsSet();
        else
            return null;
    }


    @Override
    public boolean abolish(PredicateIndicator pi) {
        if (belsDB.get(pi) == null)
            return nextBB.abolish(pi);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(getDeleteAll(pi));
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return false;
    }


    @Override
    public Iterator<Literal> getCandidateBeliefs(Literal l, Unifier u) {
        final PredicateIndicator pi = l.getPredicateIndicator();
        if (belsDB.get(pi) == null)
            return nextBB.getCandidateBeliefs(l, u);

        if (l.isVar()) {
            // all bels are relevant
            return iterator();
        } else {
            // get all rows of l's table
            String q = null;
            try {
                q = getSelect(l);
                if (logger.isLoggable(Level.FINE)) logger.fine("getRelevant query for "+l+": "+q);
                final ResultSet rs = conn.createStatement().executeQuery(q);
                return new Iterator<Literal>() {
                    boolean hasNext   = true;
                    boolean firstcall = true;
                    public boolean hasNext() {
                        if (firstcall) {
                            try {
                                hasNext = rs.next();
                            } catch (SQLException e) {
                                logger.log(Level.SEVERE, "SQL Error", e);
                            }
                            firstcall = false;
                        }
                        return hasNext;
                    }
                    public Literal next() {
                        try {
                            if (firstcall) {
                                hasNext = rs.next();
                                firstcall = false;
                            }
                            Literal l = resultSetToLiteral(rs,pi);
                            hasNext = rs.next();
                            if (!hasNext) rs.close();
                            return l;
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "Error", e);
                        }
                        return null;
                    }
                    public void remove() {
                        logger.warning("remove in jdbc get relevant is not implemented!");
                    }
                };
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "SQL Error in getRelevant for "+l+" with query "+q, e);
            }
        }
        return null;
    }

    @Override
    public int size() {
        int count = 0;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            // for all tables, count rows
            for (PredicateIndicator pi : belsDB.keySet()) {
                if (!pi.getFunctor().startsWith("~")) {
                    ResultSet rs = stmt.executeQuery(getCountQuery(pi));
                    if (rs.next()) {
                        count += rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return count + nextBB.size();
    }

    @Override
    public Iterator<Literal> iterator() {
        List<Literal> all = new ArrayList<Literal>(size());

        Iterator<Literal> is = nextBB.iterator();
        while (is.hasNext()) {
            all.add(is.next());
        }

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            // for all tables, get rows literal
            for (PredicateIndicator pi : belsDB.keySet()) {
                if (!pi.getFunctor().startsWith("~")) {
                    ResultSet rs = stmt.executeQuery(getSelectAll(pi));
                    while (rs.next()) {
                        all.add( resultSetToLiteral(rs, pi));
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
        return all.iterator();
    }


    /** translates the current line of a result set into a Literal */
    protected Literal resultSetToLiteral(ResultSet rs, PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        boolean isJasonTable = isCreatedByJason(pi);
        Literal ldb = new LiteralImpl(pi.getFunctor());
        int end = meta.getColumnCount();
        if (isJasonTable)
            end = end - extraCols;
        for (int c = 1; c <= end; c++) {
            Term parsed = null;
            switch (meta.getColumnType(c)) {
            case Types.INTEGER:
            case Types.FLOAT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.REAL:
                parsed = new NumberTermImpl(rs.getDouble(c));
                break;

            case Types.TIMESTAMP:
                parsed = timestamp2structure(rs.getTimestamp(c));
                break;

            default:
                String sc = rs.getString(c);
                if (sc == null || sc.trim().length() == 0) {
                    parsed = new StringTermImpl("");
                } else if (Character.isUpperCase(sc.charAt(0))) {
                    // there is no var at BB
                    parsed = new StringTermImpl(sc);
                } else {
                    try {
                        parsed = ASSyntax.parseTerm(sc);

                        // if the parsed term is not equals to sc, try it as string
                        if (!parsed.toString().equals(sc))
                            parsed = ASSyntax.parseTerm(sc = "\"" + sc + "\"");
                    } catch (ParseException e) {
                        // can not be parsed, be a string
                        parsed = new StringTermImpl(sc);
                    } catch (TokenMgrError e) {
                        // can not be parsed, be a string
                        parsed = new StringTermImpl(sc);
                    }
                }
                break;
            }
            ldb.addTerm(parsed);
        }
        if (isJasonTable) {
            ldb.setNegated(!rs.getBoolean(end + 1));
            ldb.setAnnots(ListTermImpl.parseList(rs.getString(end + 2)));
        }
        return ldb;
    }


    protected String getTableName(Literal l) throws SQLException {
        return getTableName(l.getPredicateIndicator());
    }

    protected String getTableName(PredicateIndicator pi) throws SQLException {
        ResultSetMetaData meta = belsDB.get(pi);
        return meta.getTableName(1);
    }

    /** returns the SQL command to create a new table */
    protected String getCreateTable(String table, int arity, Structure columns) throws SQLException {
        StringBuilder ct = new StringBuilder("create table " + table + " (");
        for (int c = 0; c < arity; c++) {
            String colName = COL_PREFIX + c;
            String colType = "varchar(256)";
            // try to get colName and type from columns
            if (columns.getArity() > c) {
                Structure scol = (Structure)columns.getTerm(c);
                colName = scol.getFunctor();
                colType = scol.getTerm(0).toString();
            }
            ct.append(colName + " " + colType + ", ");
        }
        ct.append(COL_NEG + " boolean, " + COL_ANNOT + " varchar(256))");
        logger.fine("Creating table: " + ct);
        return ct.toString();
    }

    /** returns the SQL command for a select that retrieves the literal l from the DB */
    protected String getSelect(Literal l) throws SQLException {
        return "select * from "+getTableName(l)+getWhere(l);
    }

    /** returns the SQL command the selects all literals of type pi */
    protected String getSelectAll(PredicateIndicator pi) throws SQLException {
        return "select * from " + getTableName(pi);
    }


    /** returns the where clausule for a select for literal l */
    protected String getWhere(Literal l) throws SQLException {
        ResultSetMetaData meta = belsDB.get(l.getPredicateIndicator());
        StringBuilder q = new StringBuilder(" where ");
        String and = "";
        // for all ground terms of l
        for (int i = 0; i < l.getArity(); i++) {
            Term t = l.getTerm(i);
            if (t.isGround()) {
                q.append(and);
                String ts;
                if (t.isString()) {
                    ts = "'" + ((StringTerm) t).getString() + "'";
                } else if (t.isNumeric()) {
                    ts = t.toString();
                } else {
                    ts = "'" + t.toString() + "'";
                }
                q.append(meta.getColumnName(i + 1) + " = " + ts);
                and = " and ";
            }
        }
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q.append(and + COL_NEG + " = " + l.negated());
        }
        //System.out.println(q.toString());
        if (and.length() > 0) // add nothing in the clausule
            return q.toString();
        else
            return "";
    }

    /** returns the SQL command to insert l into the DB */
    protected String getInsert(Literal l) throws Exception {
        StringBuilder q = new StringBuilder("insert into ");
        ResultSetMetaData meta = belsDB.get(l.getPredicateIndicator());
        q.append(meta.getTableName(1));
        q.append(" values(");

        // values
        for (int i = 0; i < l.getArity(); i++) {
            Term t = l.getTerm(i);
            if (t.isString()) {
                q.append("'" + ((StringTerm) t).getString() + "'");
            } else {
                Timestamp timestamp = structure2timestamp(t);
                if (timestamp != null) {
                    q.append("TIMESTAMP '" + structure2timestamp(t) + "'");
                } else {
                    q.append("'" + t.toString() + "'");
                }
            }
            if (i < meta.getColumnCount() - 1) {
                q.append(",");
            }
        }
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q.append(l.negated() + ",");
            if (l.hasAnnot()) {
                q.append("\'" + l.getAnnots() + "\'");
            } else {
                q.append("\'[]\'");
            }
        }
        q.append(")");
        return q.toString();
    }

    /** returns a SQL command to delete all entries for a predicate */
    protected String getDeleteAll(PredicateIndicator pi) throws SQLException {
        return "delete from " + getTableName(pi);
    }

    /** returns a SQL command to count the number of instances of a predicate */
    protected String getCountQuery(PredicateIndicator pi) throws SQLException {
        return "select count(*) from " + getTableName(pi);
    }


    /** just create some data to test */
    public void test() {
        Statement stmt = null;
        try {
            // add a "legacy" table
            stmt = conn.createStatement();
            try {
                stmt.executeUpdate("drop table publisher");
            } catch (Exception e) {
            }
            stmt.executeUpdate("create table publisher (id integer, name varchar)");
            stmt.executeUpdate("insert into publisher values(1, 'Springer')");
            stmt.executeUpdate("insert into publisher values(2, 'MIT Press')");
            ResultSetMetaData meta = stmt.executeQuery("select * from publisher").getMetaData();
            belsDB.put(new PredicateIndicator("publisher", 2), meta);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "SQL Error", e);
        } finally {
            try {
                stmt.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "SQL Error closing connection", e);
            }
        }
    }

    public static final String timestampFunctor = "timestamp";

    /** translates a SQL timestamp into a structure like "timestamp(Y,M,D,H,M,S)" */
    public static Structure timestamp2structure(Timestamp timestamp) throws SQLException {
        Calendar time = Calendar.getInstance();
        time.setTime(timestamp);
        return ASSyntax.createStructure(timestampFunctor,
                                        createNumber(time.get(Calendar.YEAR)),
                                        createNumber(time.get(Calendar.MONTH)),
                                        createNumber(time.get(Calendar.DAY_OF_MONTH)),
                                        createNumber(time.get(Calendar.HOUR_OF_DAY)),
                                        createNumber(time.get(Calendar.MINUTE)),
                                        createNumber(time.get(Calendar.SECOND)));
    }

    /** translates structure like "timestamp(Y,M,D,H,M,S)" into a SQL timestamp */
    @SuppressWarnings("deprecation")
    public static Timestamp structure2timestamp(Term timestamp) throws Exception {
        if (timestamp.isStructure()) {
            Structure s = (Structure)timestamp;
            if (s.getFunctor().equals(timestampFunctor) && s.getArity() == 6) {
                return new Timestamp(
                           (int)((NumberTerm)s.getTerm(0)).solve() - 1900,
                           (int)((NumberTerm)s.getTerm(1)).solve(),
                           (int)((NumberTerm)s.getTerm(2)).solve(),
                           (int)((NumberTerm)s.getTerm(3)).solve(),
                           (int)((NumberTerm)s.getTerm(4)).solve(),
                           (int)((NumberTerm)s.getTerm(5)).solve(),
                           0
                       );
            }
        }
        return null;
    }
}
