import java.sql.*;
import java.util.Date;

/**
 * This class implements functions for CustomerFunctions class helper function
 */
class CustomerHelper {
    static ResultSet rs = null;

    /**
     * Check if the input id is a valid id
     * @param id String, input id
     * @param conn Connection
     * @return id if valid, -1 otherwise
     */
    static int checkID(String id, Connection conn) {
        if(id.isEmpty()) {
            return -1;
        }
        try {
            int res = Integer.parseInt(id);
            if(!PubUtil.taken(conn, res, 1)) return -1;
            return res;
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Insert customer account data to database
     * @param conn Connection
     * @param name customer name
     * @param email customer email address
     * @param customerID customer id
     * @param date date of register an account
     */
    static boolean insertCustomer(Connection conn, String name, String email, int customerID, java.sql.Date date){
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
     * Insert movie attendance data to table Attendance
     * @param conn
     * @param customerID
     * @param movieID
     * @param attendanceDate
     * @return true if insert success, otherwise false
     */
    public static boolean insertAttendance(Connection conn, int customerID, int movieID, java.sql.Date attendanceDate){
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
     * Insert movie review to Review table
     * @param conn
     * @param customerID
     * @param movieID
     * @param reviewID
     * @param rate
     * @param reviewStr
     * @param today
     * @return true if insert success, otherwise false
     */
    public static boolean insertReview(Connection conn, int customerID, int movieID, int reviewID, int rate, String reviewStr, java.sql.Date today){
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

    /**
     * Insert endorsement of review to endorsement table
     * @param conn
     * @param reviewID
     * @param customerID
     * @param today
     * @return true if insert success, otherwise false
     */
    public static boolean insertEndorsement(Connection conn, int reviewID, int customerID, java.sql.Date today){
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

    /**
     * Calculate the average rate for all movies and return the result set in descending order
     * @param stmt Statement
     * @return result set of all movies name and average rating, in descending order
     */
    public static ResultSet getTopMovies(Statement stmt){
        String query = "SELECT m.Title, r.avg_rating FROM " +
                "(SELECT movieID, AVG(Rating) AS avg_rating FROM Review " +
                "GROUP BY movieID) r " +
                "JOIN Movie m ON r.movieId = m.movieId " +
                "ORDER BY r.avg_rating DESC";
        try {
            rs = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Sorry. Information not available now");
            e.printStackTrace();
        }
        return rs;
    }

}
