import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class PubUtil {

    /**
     * Randomly generate id for movie, customer or review
     * @param choice 0:movie，1:customer，2:review
     * @param conn derby connection
     * @return int id
     */
    static int generateID(int choice, Connection conn){
        assert(choice == 0 || choice == 1 || choice == 2);
        int lowerBound = 0;
        int upperBound = 0;

        if (choice == 0) {    // bounds for movie id
            lowerBound = 1000;
            upperBound = 9999;
        } else if (choice == 1) {        // bounds for customer id
            lowerBound = 1000000;
            upperBound = 9999999;
        } else if (choice == 2) {
            lowerBound = 100000000;
            upperBound = 999999999;
        }

        Random random = new Random();
        int id = random.nextInt(upperBound-lowerBound+1) + lowerBound;
        while(taken(conn, id, choice)){
            id = random.nextInt(upperBound-lowerBound+1) + lowerBound;
        }
        return id;
    }

    /**
     * convert input String to date type
     * @param inputDate user input string
     * @return Date type of the input date
     */
    static Date convertToDate(String inputDate){
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;
        try{
            date = d.parse(inputDate);
        } catch (ParseException e){
            System.out.println("Invalid date!");
            return null;
        }
        Date res = new Date(date.getTime());
        return res;
    }

    /**
     * check if the id exits in the correspondent table
     * @param conn derby connection
     * @param id
     * @param choice 0:movie，1:customer，2:review
     * @return true if the id exits in the correspondent table
     */
    static boolean taken(Connection conn, int id, int choice){
        String table;
        if(choice == 0) table = "Movie";
        else if(choice == 1) table = "Customer";
        else table = "Review";
        ResultSet rs;
        boolean b = false;
        try {
            String query = "select * from " + table + " where " + table + "id = " + id;
            PreparedStatement findId = conn.prepareStatement(query);
            rs = findId.executeQuery();
            if(rs.next())  b = true;
        } catch (SQLException e) {
            b = false;
        }
        return b;
    }


    /**
     * Check whether the key of a column name is in a specific table
     * @param stmt derby Statement
     * @param tbl String, table name
     * @param colName String, column name
     * @param key int, id of the colName
     * @return true if key is in the table
     */
    public static boolean inTable(Statement stmt, String tbl, String colName, int key){
        ResultSet rs;
        String query = "select "+colName+" from "+tbl+" where "+colName+"  = " + key;
        try{
            rs = stmt.executeQuery(query);
            return rs.next();
        } catch (SQLException e){
            System.err.println("Error: SQLException in PubAPI.inTable()");
        }

        return false;
    }

    /**
     * check if the input is an int
     * @param str input string
     * @return -1 if invalid
     */
    public static int validInt(String str) {
        if(str.isEmpty()) {
            return -1;
        }
        try {
            int res = Integer.parseInt(str);
            return res;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }


    /**
     * Print row data with certain format: each element in row is printed with width=18 and separated by "|"
     * @param row String[], items in a row
     */
    public static void printRow(String[] row){
        for (String s:row){
            if (s.length() > 18){
                s = s.substring(0,15) + "...";
            }
            System.out.printf("|  %-18s", s);
        }
        System.out.println("|");
    }

    /**
     * Print a line with a width of nColumns
     * @param nColumns int, number of columns
     */
    public static void printLine(int nColumns){
        for (int c=0; c<nColumns; c++) System.out.print(" --------------------");
        System.out.println();
    }
}

