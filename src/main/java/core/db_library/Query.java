package core.db_library;

// authors: Nicole Muzayyin, Felix D'Cruz
// class to define a query of two kinds (read or write) constructed with either string or a file.
    // read:    insert/update/delete, no resultsColumns (null)
    // write:   select, resultsColumns specifies the results that will be extracted

// CHANGE:
// removed the automatic parameter extraction since the database reflects the program not the other way around
// parameter extraction would be useful for making a program work with any database rather than a specific one

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Query {

    private final String sql;
    private final String[] resultColumns; // null for write queries
    private final List<Object> parameters;
    private final ArrayList<String[]> results = new ArrayList<>();

    // write query, defined from inline SQL with explicit parameters
    public Query(String sql, List<Object> parameters)
    {
        this.sql = sql;
        this.resultColumns = null;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }

    // read query, defined from file, with parameters
    public Query(String sql, String[] resultColumns, List<Object> parameters) {
        this.sql = sql;
        this.resultColumns = resultColumns;
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
    }

    // read or write query, defined from file, no parameters
    public Query(String sql, String[] resultColumns) {
        this(sql, resultColumns, null);
    }

    public static String fileToString(String sqlFile){
        try (InputStream in = Query.class.getResourceAsStream("/sql/" + sqlFile + ".sql")) {
            if (in == null) {
                throw new RuntimeException("SQL file not found on classpath: /sql/" + sqlFile + ".sql");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read SQL file: " + sqlFile, e);
        }
    }

    // store results after executing SELECT SQL in ManageConnection
    public void addResult(String[] row) {
        results.add(row);
    }

    public String getSql()             { return sql; }
    public String[] getResultColumns() { return resultColumns; }
    public List<Object> getParameters(){ return parameters; }
    public boolean hasParameters()     { return !parameters.isEmpty(); }
    public boolean isWriteQuery()      { return resultColumns == null; }
    public ArrayList<String[]> getResults() { return results; }
    public int rowCount()              { return results.size(); }
}