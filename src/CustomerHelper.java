import java.sql.*;
import java.util.Date;

public class CustomerHelper {
    static ResultSet rs = null;

    /**
     * check if the input id is a valid id
     * @param id
     * @param conn
     * @return id if valid, -1 otherwise
     */
    public int checkID(String id, Connection conn) {
        if(id.isEmpty()) {
            return -1;
        }
        try {
            int res = Integer.parseInt(id);
            if(!Biblio.taken(conn, res, 1)) return -1;
            return res;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * insert customer account data to database
     * @param conn
     * @param name customer name
     * @param email customer email address
     * @param customerID customer id
     * @param date date of register an account
     */
    public static boolean insertCustomer(Connection conn, String name, String email, int customerID, java.sql.Date date){
        try {
            PreparedStatement customer = conn.prepareStatement("insert into Customer (Name, Email, JoinedDate, CustomerID) values (?, ?, ?, ?)");
            customer.setString(1, name);
            customer.setString(2, email);
            customer.setDate(3, date);
            customer.setInt(4, customerID);
            customer.execute();
        } catch (SQLException e) {
            System.out.println("Invalid input, you cannot register.");
            return false;
        }
        System.out.println("Customer account inserted.");
        return true;
    }

    /**
     * insert movie attendance data to table Attendance
     * @param conn
     * @param customerID
     * @param movieID
     * @param attendanceDate
     */
    public boolean insertAttendance(Connection conn, int customerID, int movieID, java.sql.Date attendanceDate){
        PreparedStatement attendance;
        try {
            attendance = conn.prepareStatement("insert into Attendance (MovieId, AttendanceDATE, CustomerId) values (?, ?, ?)");
            attendance.setInt(1, movieID);
            attendance.setDate(2, attendanceDate);
            attendance.setInt(3, customerID);
            attendance.execute();
        } catch (SQLException e) {
            System.out.println("Sorry, we cannot confirm your attendance information.");
            return false;
        }

        System.out.println("You've checked in a movie successfully! How do you like it? You can give it a review!");
        return true;
    }

    /**
     * insert movie review to Review table
     * @param conn
     * @param customerID
     * @param movieID
     * @param reviewID
     * @param rate
     * @param reviewStr
     * @param today
     * @return
     */
    public boolean insertReview(Connection conn, int customerID, int movieID, int reviewID, int rate, String reviewStr, java.sql.Date today){
        PreparedStatement review;
        try {
            review = conn.prepareStatement("insert into Review (CustomerID, MovieID, ReviewDate, Rating, ReviewID, Review) values (?, ?, ?, ?, ?, ?)");
            review.setInt(1, customerID);
            review.setInt(2, movieID);
            review.setDate(3, today);
            review.setInt(4, rate);
            review.setInt(5, reviewID);
            review.setString(6, reviewStr);
            review.execute();
        } catch (SQLException e) {
            System.out.println("Sorry, we cannot add your review.");
            return false;
        }

        System.out.println("Thank you for your review!");
        return true;
    }

    public boolean insertEndorsement(Connection conn, int reviewID, int customerID, java.sql.Date today){
        PreparedStatement endorse;
        try {
            endorse = conn.prepareStatement("insert into Endorsement (ReviewID, CustomerID, EndorsementDate) values (?, ?, ?)");
            endorse.setInt(1, reviewID);
            endorse.setInt(2, customerID);
            endorse.setDate(3, today);
            endorse.execute();
        } catch (SQLException e) {
            System.out.println("Sorry, you cannot endorse for this review.");
            return false;
        }

        System.out.println("Thank you for your endorsement!");
        return true;
    }

    public static ResultSet getTopMovies(Connection conn){
        ResultSet RS = null;
        try {
            String query1 = "(\nselect cast(avg(Rating) as DOUBLE) \nFrom Review \nwhere review.movieid = movie.movieid) as avgRating";
            String query2 = "\nselect title," + query1 + " \nfrom movie \norder by avgRating desc";
            //System.out.println("Query: \n"+query2);
            PreparedStatement findMovieId = conn.prepareStatement(query2);
            rs = findMovieId.executeQuery();
        } catch (SQLException e) {
            System.out.println("Sorry. Information not available now");
        }
        return rs;
    }

    /**
     * print the table data related to a specific customer
     * @param stmt
     * @param table
     * @param id
     */
    public static void showInformation(Statement stmt, String table, String colName, int id){
        System.out.println("["+table+"]");

        try {
            rs = stmt.executeQuery("SELECT * From "+table+" where " + colName +" = "+id);
        } catch (SQLException e){}
        printResultSet(rs);
    }

    /**
     * print the specific table data
     * @param stmt
     * @param table
     */
    public static void showTable(Statement stmt, String table){
        System.out.println("["+table+"]");

        try {
            rs = stmt.executeQuery("SELECT * From "+table);
        } catch (SQLException e) {}
        printResultSet(rs);
    }




    public static void printResultSet(ResultSet rs) {
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            String[] row = new String [numberOfColumns];

            for (int i = 1; i <= numberOfColumns; i++) {
                String columnName = rsmd.getColumnName(i);
                row[i-1] = columnName;
            }
            Biblio.printLine(numberOfColumns);
            Biblio.printRow(row);
            Biblio.printLine(numberOfColumns);
            while (rs.next()) {
                for (int i = 1; i <= numberOfColumns; i++) {

                    String columnValue = rs.getString(i);
                    row [i-1] = columnValue;
                }
                Biblio.printRow(row);
            }
            Biblio.printLine(numberOfColumns);
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Information failed to show!");
        }
    }

    public static void printTop(ResultSet rs){
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            String[] row = new String [numberOfColumns];

            for (int i = 1; i <= numberOfColumns; i++) {
                String columnName = rsmd.getColumnName(i);
                row[i-1] = columnName;
            }
            Biblio.printLine(numberOfColumns);
            Biblio.printRow(row);
            Biblio.printLine(numberOfColumns);
            int num = 0;
            while (rs.next() && num < 3) {
                for (int i = 1; i <= numberOfColumns; i++) {

                    String columnValue = rs.getString(i);
                    row [i-1] = columnValue;
                }
                Biblio.printRow(row);
                num++;
            }
            Biblio.printLine(numberOfColumns);
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Information failed to show!");
        }
    }

}
