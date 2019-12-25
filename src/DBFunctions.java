import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Helper functions that are needed to create tables
 */
public class DBFunctions {

    static final String protocol = "jdbc:derby:";
    static final String dbName = "iRate";
    static final String connStr = protocol + dbName + ";create=true";
    static Properties props = getProps(); // connection properties
    static final long MS_IN_A_DAY = 24 * 60 * 60 * 1000;

    public static Properties getProps() {
        Properties props = new Properties(); // connection properties
        props.put("user", "user1");
        props.put("password", "user1");
        return props;
    }


    /**
     * Check whether email is a valid email address
     *
     * @param email
     * @return true if the email address is valid
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        boolean valid = email.matches(emailRegex);
        if(email.isEmpty() || !valid) {
            System.err.println("Error: " +email + " is an invalid email address! [DBFunctions.isValidAttendanceDate()]");
        }
        return valid;
    }


    /**
     * Attendance date should not prior to the customer's join_date
     *
     * @param cid int, customer id
     * @param attendanceDate java.sql.Date, attendance date for the movie
     * @return true if attendance date is valid. false if customer does not exist or the attendance date is invalid
     */
    public static boolean isValidAttendanceDate(int cid, Date attendanceDate) {
        String query = "select JoinedDate from Customer where CustomerID = " + cid;
        try (
                Connection conn = DriverManager.getConnection(connStr, props);
                Statement stmt = conn.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()){    // customer do not exist
                System.err.println("Error: Customer "+cid+" does not exist! [DBFunctions.isValidAttendanceDate()]");
            }
            else {              // attendance date is earlier in customer's join date
                Date joinDate = rs.getDate("JoinedDate");
                if (attendanceDate.getTime() - joinDate.getTime() >= 0) {
                    return true;
                } else {
                    System.err.println("Error: attendance date " +attendanceDate+
                            " is earlier than customer's join date " + joinDate +
                            "\n      [DBFunctions.isValidAttendanceDate()]");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error: SQLException in DBFunctions.isValidAttendanceDate");
            // e.printStackTrace();
        }
        return false;
    }

    /**
     * The customer must review the movie within 7 days of attendance.
     *
     * @param cid int, customer id
     * @param mid int, movie id
     * @param reviewDate java.sql.Date, review date
     * @return true if the review date is within 7 days of the attendance date.
     *         false if customer did not watch the movie
     *         false if the review date is before or 7 days later than the attendance date
     */
    public static boolean checkReviewDate(int cid, int mid, Date reviewDate) {
        String query = "select AttendanceDATE from Attendance where CustomerID = " + cid + " and MovieID = " + mid + " order by AttendanceDate desc";
        try (
                Connection conn = DriverManager.getConnection(connStr, props);
                Statement stmt = conn.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()){    // customer has not watched the movie
                System.err.println("Error: customer " + cid +
                        " has not watched the movie " + mid +
                        "\n      [DBFunctions.isValidAttendanceDate()]");
            }
            else {  // review date is not valid
                Date attendDate = rs.getDate("AttendanceDate");
                long diffDay = (reviewDate.getTime() - attendDate.getTime())/MS_IN_A_DAY;
                if (diffDay < 0){
                    System.err.println("Error: Invalid Review Date! " +
                            "\n       customer " + cid +
                            " cannot review the movie " + mid +
                            " before the attendance date" + attendDate +
                            "\n      [DBFunctions.isValidAttendanceDate()]");
                }
                else if ( diffDay <=7) return true;

                else {
                    System.err.println("Error: Invalid Review Date! " +
                            "\n       customer " + cid +
                            " cannot review the movie " + mid +
                            " after 7 days of the attendance " + attendDate +
                            "\n      [DBFunctions.isValidAttendanceDate()]");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    /**
     * There can only be one movie review per customer, which means a customer can review a certain movie only once.
     *
     * @param cid int, indicates the customer who is going to review the movie
     * @param mid int, indicates the movie to be reviewed
     * @return true if the customer hasn't reviewed this movie yet,and this is going to be the first review by the customer
     */


    public static boolean isFirstReview(int cid, int mid) {
        String query = "select customerID, movieID from Review where movieID = " + mid +
                " and customerID = " + cid;
        try (
                Connection conn = DriverManager.getConnection(connStr, props);
                Statement stmt = conn.createStatement()
        ) {
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                return true;
            } else {
                System.err.println("Error: customer " +cid+
                        " already reviewed the movie " +mid+
                        "\n      [DBFunctions.isFirstReview()]" );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This is the function to check if the rating is valid. Rating must be any integer between 1 and 5.
     * 1 = BAD, 2 = DISAPPOINTING, 3 = GOOD, 4 = GREAT, 5 = FANTASTIC.
     *
     * @param rating int, the rating given by a customer
     * @return true if the rating is valid.
     */
    public static boolean isValidRating(int rating) {
        if (rating > 5 || rating < 1) {
            System.err.printf("Error: Invalid rating: %d. Rating must be between 1-5." +
                    "\n      [DBFunctions.isValidRating()]", rating);
            return false;
        }
        return true;
    }

    /**
     * This is the function that checks if the date is open for endorsement for a particular review:
     * Rule: voting is closed for all reviews written three days ago.
     *
     * @param endorseDate java.sql.Date, the current endorsement date of a review
     * @param reviewDate java.sql.Date, the date of a review written for a movie
     * @return true if the date is open for voting a review
     */
    public static boolean isOpenForEndorsement(Date reviewDate, Date endorseDate) {
        if (reviewDate == null) {
            System.out.println("Oops! No review written on this date.");
            return false;
        }
        long diffDays = (endorseDate.getTime() - reviewDate.getTime())/ MS_IN_A_DAY;
        if (diffDays > 3) {
            System.err.println("Sorry, you cannot vote a review written 3 days ago." +
                    "\n      [DBFunctions.isOpenForEndorsement()]");
            return false;
        } else if (diffDays < 0){
            System.err.println("Sorry, you cannot vote a review before it is written."+
                    "\n      [DBFunctions.isOpenForEndorsement()]");
            return false;
        }
        return true;
    }

    /**
     * This is the function to check if the voter is voting his or her own review. The rule is a customer cannot endorse
     * his or her own review.
     *
     * @param customerID int
     * @param endorserID int
     * @return true if the customer is self-endorse.
     */
    public static boolean isSelfEndorse(int endorserID, int customerID) {
        if (endorserID == customerID) {
            System.err.println("Sorry, customer " +customerID+
                    ", you can not vote for yourself."+
                    "\n      [DBFunctions.isSelfEndorse()]");
            return true;
        }
        return false;
    }

    /**
     * This is the function to check if the current endorsement date for a movie is at least one day after the customer's
     * endorsement of a review for the same movie.
     *
     * @param endorseDate java.sql.Date
     * @param lastEndorseDate java.sql.Date
     * @return true if current endorsement date is more than 24 hours later than last endorsement of the same movie, or
     * the customer hasn't endorsed of this movie yet.
     */
    public static boolean checkEndorsementDate(Date lastEndorseDate, Date endorseDate) {
        if (lastEndorseDate == null) {
            return true;
        }
        if (endorseDate.getTime() - lastEndorseDate.getTime() >= MS_IN_A_DAY) {
            return true;
        } else {
            System.err.println("Sorry, you cannot endorse reviews of the same movie within 24 hours."+
                    "\n      [DBFunctions.checkEndorsementDate()]");
            return false;
        }
    }

    /**
     * This function checks if the endorsement is allowed for a review of a certain movie.
     *
     * @param rid int, reviewID
     * @param endorser_id int, customer id of the endorser
     * @param endorseDate java.sql.Date, endorse date
     * @return true if endorse is allowed, which means the endorsement satisfies three conditions.
     */
    public static boolean isEndorseAllowed(int rid, int endorser_id, Date endorseDate) {
        try (
                Connection conn = DriverManager.getConnection(connStr, props);
                Statement stmt = conn.createStatement()
        ) {
            // Get information of the review with the reviewID same as rid from the Review table
            String query = "select customerID, movieID, reviewDate from Review where reviewID = " + rid;
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                System.out.println(rid);
                System.err.println("Error: reviewID does not exist! [DBFunctions.isEndorseAllowed()]");
                return false;
            }
            int cid1 = rs.getInt("customerID");
            int mid = rs.getInt("movieID");
            Date reviewD = rs.getDate("reviewDate");
            // if the endorser is the same as who wrote the review, endorsement is not allowed
            if (isSelfEndorse(endorser_id, cid1)) {
                return false;
            }
            // if the review date is more than 3 days earlier than the endorsement date, endorsement is not allowed
            if (!isOpenForEndorsement(reviewD, endorseDate)) {
                return false;
            }
            Statement stmt1 = conn.createStatement();
            // Get all the reviewIDs of a certain movie from Review table
            String query1 = "select reviewID from Review where movieID = " + mid;
            ResultSet rs1 = stmt1.executeQuery(query1);
            while (rs1.next()) {
                int revID = rs1.getInt("ReviewID");
                Statement stmt2 = conn.createStatement();
                // Get the endorsement date of a review by the endorser from Endorsement table
                String query2 = "select endorsementDate from Endorsement where reviewID = " + revID +
                        " and customerID = " + endorser_id;
                ResultSet rs2 = stmt2.executeQuery(query2);
                if (rs2.next()) {
                    Date lastEndorseDate = rs2.getDate("endorsementDate");
                    // if current endorse date is within 1 day of the last endorsement date of the same movie, then
                    // endorsement is not allowed
                    if (!checkEndorsementDate(lastEndorseDate, endorseDate)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error: SQLException in DBFunctions.isEndorseAllowed()");
            e.printStackTrace();
            return false;
        }
    }
}
