import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.Random;

public class Biblio {

    /** (yuting)
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
     * (chuhan)
     * convert input String to date type
     * @param inputDate
     * @return
     */
    static java.sql.Date convertToDate(String inputDate){
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date = null;
        try{
            date = d.parse(inputDate);
        } catch (ParseException e){
            System.out.println("Invalid date!");
            return null;
        }
        java.sql.Date res = new java.sql.Date(date.getTime());
        return res;
    }

    /**
     * (chuhan)
     * check if the id exits in the table
     * @param conn
     * @param id
     * @param choice
     * @return
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
     * check if the input is an int
     * @param str
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

    public static void printRow(String[] row){
        for (String s:row){
            if (s.length() > 18){
                s = s.substring(0,15) + "...";
            }
            System.out.printf("|  %-18s", s);
        }
        System.out.println("|");
    }

    public static void printLine(int nColumns){
        for (int c=0; c<nColumns; c++) System.out.print(" --------------------");
        System.out.println();
    }
}

