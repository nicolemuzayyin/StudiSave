package core.db_functions;

// original code: SQLite JDBC Driver https://github.com/xerial/sqlite-jdbc?tab=readme-ov-file
// authors: Nicole Muzayyin, Felix D'Cruz

import core.db_library.Query;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageConnection {

    public static void execute(ArrayList<Query> queryList) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:StudiSave.db")) {
            System.out.println("## New connection established");
            for (int i = 0; i < queryList.size(); i++) {
                Query q = queryList.get(i);
                System.out.println("SQL: Running query " + i + " (" + (q.isWriteQuery() ? "WRITE" : "READ") + ")");
                if (q.isWriteQuery()) {
                    runWrite(conn, q);
                } else {
                    runRead(conn, q);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        // connection closes automatically since it is in a try block
        System.out.println("## Connection closed");
    }

    private static void runWrite(Connection conn, Query q) throws SQLException {
        if (q.hasParameters()) {
            try (PreparedStatement ps = conn.prepareStatement(q.getSql())) {
                ps.setQueryTimeout(30);
                setParameters(ps, q.getParameters());
                int rows = ps.executeUpdate();
                System.out.println("SQL: Write affected " + rows + " row(s)");
            }
        } else {
            try (Statement stmt = conn.createStatement()) {
                stmt.setQueryTimeout(30);
                int rows = stmt.executeUpdate(q.getSql());
                System.out.println("SQL: Write affected " + rows + " row(s)");
            }
        }
    }

    private static void runRead(Connection conn, Query q) throws SQLException {
        if (q.hasParameters()) {
            try (PreparedStatement ps = conn.prepareStatement(q.getSql())) {
                ps.setQueryTimeout(30);
                setParameters(ps, q.getParameters());
                try (ResultSet rs = ps.executeQuery()) {
                    processResultSet(rs, q);
                }
            }
        } else {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs   = stmt.executeQuery(q.getSql())) {
                processResultSet(rs, q);
            }
        }
    }

    private static void processResultSet(ResultSet rs, Query q) throws SQLException {
        String[] columns = q.getResultColumns();
        int count = 0;
        while (rs.next()) {
            String[] row = new String[columns.length];
            for (int c = 0; c < columns.length; c++) {
                row[c] = rs.getString(columns[c]);
            }
            q.addResult(row);
            count++;
        }
        System.out.println("SQL: Read returned " + count + " row(s)");
    }

    private static void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}
