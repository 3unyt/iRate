

import java.io.*;
import java.sql.*;
import java.util.Arrays;

public class PubAPI {


    // print main ui:
    /*
    - load default data
    - print all data
    - login as customer
    - login as theater
    - quit
     */


    public static void loadDefaultData(Connection conn, Statement stmt){
        clearData(stmt);
        loadCustomer(conn);
        loadMovie(conn);
        loadAttendance(conn);
        loadReview(conn);
        loadEndorsement(conn);

    }

    public static void clearData(Statement stmt){
        // clear data from tables
        for (String tbl : Tables.dbTables) {
            try {
                stmt.executeUpdate("delete from " + tbl);
//                System.out.println("cleared table: "+tbl);
            } catch (SQLException ex) {
                System.out.println("Did not truncate table " + tbl);
            }
        }
        System.out.println("Cleared all tables.");
    }

    /** yuting
     * Load customer data from file
     * @param conn
     */
    public static void loadCustomer(Connection conn){
        String fineName = "iRate_customer.txt";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fineName)));
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fineName);
        }

        PreparedStatement customer;
        String line;
        int count = 0;
        try{
            customer = conn.prepareStatement("insert into Customer (Name, Email, JoinedDate, CustomerID) values (?, ?, ?, ?)");

            while((line=br.readLine()) !=  null) {
                String[] data = line.split("\t");
                if (data.length != 4) continue;
                customer.setString(1, data[0]);
                customer.setString(2, data[1]);
                customer.setDate(3, Biblio.convertToDate(data[2]));
                customer.setInt(4, Integer.parseInt(data[3]));
                customer.execute();
                count++;

            }

        } catch (SQLException e){
            System.err.println("ERROR: SQLException in loading customers");
        } catch (IOException e){
            System.err.println("ERROR: IOException in loading customers");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + count + " customers.");
    }

    /** yuting
     * Load movie data from file
     * @param conn
     */
    public static void loadMovie(Connection conn){
        String fineName = "iRate_movie.txt";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fineName)));
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fineName);
        }

        PreparedStatement movie;
        String line;
        int count = 0;
        try{
            movie = conn.prepareStatement("insert into Movie (Title, MovieID) values (?, ?)");
            while((line=br.readLine()) !=  null) {
                String[] data = line.split("\t");
                if (data.length != 2) continue;
                movie.setString(1, data[0]);
                movie.setInt(2, Integer.parseInt(data[1]));
                movie.executeUpdate();
                count++;

            }

        } catch (SQLException e){
            System.err.println("ERROR: SQLException in loading movies");
        } catch (IOException e){
            System.err.println("ERROR: IOException in loading movies");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + count + " movies.");
    }

    /** yuting
     * Load attentance data from file
     * @param conn
     */
    public static void loadAttendance(Connection conn){
        String fineName = "iRate_attendance.txt";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fineName)));
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fineName);
        }

        PreparedStatement attendance;
        String line;
        int count = 0;
        try{
            attendance = conn.prepareStatement("insert into Attendance (MovieId, AttendanceDATE, CustomerId) values (?, ?, ?)");

            while((line=br.readLine()) !=  null) {
                String[] data = line.split("\t");
                if (data.length != 3) continue;
                attendance.setInt(1, Integer.parseInt(data[0]));
                attendance.setDate(2, Biblio.convertToDate(data[1]));
                attendance.setInt(3, Integer.parseInt(data[2]));

                attendance.execute();
                count++;

            }

        } catch (SQLException e){
            System.err.println("ERROR: SQLException in loading attendances");
        } catch (IOException e){
            System.err.println("ERROR: IOException in loading attendances");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + count + " attendances.");
    }

    /**
     * Load review data from file
     * @param conn
     */
    public static void loadReview(Connection conn){
        String fineName = "iRate_review.txt";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fineName)));
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fineName);
        }

        PreparedStatement review;
        String line;
        int count = 0;
        try{
            review = conn.prepareStatement("insert into Review (CustomerID, MovieID, ReviewDate, Rating, Review, ReviewID) values (?, ?, ?, ?, ?, ?)");

            while((line=br.readLine()) !=  null) {
                String[] data = line.split("\t");
                if (data.length != 6) continue;
                review.setInt(1, Integer.parseInt(data[0]));
                review.setInt(2, Integer.parseInt(data[1]));
                review.setDate(3, Biblio.convertToDate(data[2]));
                review.setInt(4, Integer.parseInt(data[3]));
                review.setInt(6, Integer.parseInt(data[4]));
                review.setString(5, data[5]);
                review.execute();
                count++;

            }

        } catch (SQLException e){
            System.err.println("ERROR: SQLException in loading reviews");
        } catch (IOException e){
            System.err.println("ERROR: IOException in loading reviews");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + count + " reviews.");
    }

    /**
     * L
     * @param conn
     */
    public static void loadEndorsement(Connection conn){
        String fineName = "iRate_endorsement.txt";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(fineName)));
        }catch (FileNotFoundException e){
            System.err.println("File not found: " + fineName);
        }

        PreparedStatement endorse;
        String line;
        int count = 0;
        try{
            endorse = conn.prepareStatement("insert into Endorsement (ReviewID, CustomerID, EndorsementDate) values (?, ?, ?)");

            while((line=br.readLine()) !=  null) {
                String[] data = line.split("\t");
                if (data.length != 3) continue;
                endorse.setInt(1, Integer.parseInt(data[0]));
                endorse.setInt(2, Integer.parseInt(data[1]));
                endorse.setDate(3, Biblio.convertToDate(data[2]));
                endorse.execute();
                count++;

            }

        } catch (SQLException e){
            System.err.println("ERROR: SQLException in loading endorsements");
        } catch (IOException e){
            System.err.println("ERROR: IOException in loading endorsements");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Loaded " + count + " endorsements.");
    }

    /**
     * Print all tables to the console
     * @param stmt
     */
    public static void printAllTables(Statement stmt, ResultSet rs) {
        for(String tbl: Tables.dbTables){
            printTable(stmt, rs, tbl);
        }
    }

    public static void printTable(Statement stmt, ResultSet rs, String tbl) {
        System.out.println("| Table: " + tbl + "|");
        try{
            rs = stmt.executeQuery("select * from " + tbl);
            ResultSetMetaData rsmd = rs.getMetaData();
            int nColumns = rsmd.getColumnCount();
            String[] colNames = new String[nColumns];

            // get column name
            for (int i = 1; i<=nColumns; i++){
                colNames[i-1] = rsmd.getColumnName(i);

            }

            // print titles
            Biblio.printLine(nColumns);
            Biblio.printRow(colNames);
            Biblio.printLine(nColumns);

            // print rows:
            String[] row = new String[nColumns];
            while (rs.next()){
                for (int i = 1; i <= nColumns; i++) {
                    row [i-1] = rs.getString(i);
                }
                Biblio.printRow(row);
            }

            Biblio.printLine(nColumns);
            System.out.println();
        } catch (SQLException e){
            System.err.println("Failed to print table: " + tbl);
        }


    }



    /**
     * Check whether a key is in table
     * @param conn
     * @param stmt
     * @param tbl
     * @param key
     */
    public static boolean inTable(Connection conn, Statement stmt, String tbl, String colName, int key){
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





}
