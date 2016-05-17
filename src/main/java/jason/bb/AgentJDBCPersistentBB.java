package jason.bb;

import jason.asSyntax.Literal;
import jason.asSyntax.PredicateIndicator;
import jason.asSyntax.Structure;

import java.sql.SQLException;

/**
 * Implementation of BB that stores some beliefs in a relational data base.
 * 
 * For all created table, an additional column (called j_agent) is created
 * and populated with the name of the agent that uses this customisation.
 * 
 * @author Neil Madden
 */
public class AgentJDBCPersistentBB extends JDBCPersistentBB {

    static final String COL_AGENT = "j_agent";
    
    public AgentJDBCPersistentBB() {
        extraCols = 3;
    }

    @Override
    protected String getCreateTable(String table, int arity, Structure columns) throws SQLException {
        StringBuilder q = new StringBuilder(super.getCreateTable(table,arity,columns));
        q.insert(q.length()-1, ", " + COL_AGENT + " varchar(100)");
        return q.toString(); 
    }

    protected String getAgWhere() {
        return COL_AGENT + " = '" + agentName + "'";
    }
    
    @Override
    protected String getDeleteAll(PredicateIndicator pi) throws SQLException {
        return super.getDeleteAll(pi) + " where " + getAgWhere();
    }
    

    @Override
    protected String getCountQuery(PredicateIndicator pi) throws SQLException {
        return super.getCountQuery(pi) + " where " + getAgWhere();
    }

    @Override
    protected String getSelectAll(PredicateIndicator pi) throws SQLException {
        return super.getSelectAll(pi) + " where " + getAgWhere();
    }
    
    @Override
    protected String getWhere(Literal l) throws SQLException {
        String q = super.getWhere(l);
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q += " and " + getAgWhere();
        }
        return q;
    }

    @Override
    protected String getInsert(Literal l) throws Exception {
        StringBuilder q = new StringBuilder(super.getInsert(l));
        if (isCreatedByJason(l.getPredicateIndicator())) {
            q.insert(q.length()-1, ", '" + agentName + "'");
        }
        return q.toString();
    }
}
