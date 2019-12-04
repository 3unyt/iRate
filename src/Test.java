import java.sql.*;
import java.util.Scanner;


public class Test {
    public static void main(String[] args) {
        Connection conn = null;
        Scanner userInput= new Scanner(System.in);

        try{
            conn = Tables.newConnection();
            Statement stmt = conn.createStatement();
//            ResultSet rs = null;
//
            // create tables
//            Tables.initializeAll(stmt);
//
            // load default data
            PubAPI.loadDefaultData(conn, stmt);

//            PubAPI.printAllTables(stmt, null);

//            TheaterFunctions.selectFreeTicketCustomer(conn, userInput);
            // done

//            TheaterFunctions.selectCustomerWithGift(conn,stmt, userInput);

            testCustomerConstraints(conn, stmt, null);
            testAttendanceConstraints(conn, stmt, null);
            testReviewConstraints(conn, stmt, null);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public static void testCustomerConstraints(Connection conn, Statement stmt, PreparedStatement pstmt) throws SQLException {
        System.out.println("======= Test for Constraints for Table Customer =======");
        String name = "Steve";
        String email_good = "steve@gmail.com";
        String email_bad = "steve@gmail";
        int id_good = 1111111;
        int id_tooSmall = 999999;
        int id_tooLarge = 10000000;
        Date date = Biblio.convertToDate("2019-11-05");


        try {
            pstmt = conn.prepareStatement("insert into Customer (Name, Email, JoinedDate, CustomerID) values (?, ?, ?, ?)");

            pstmt.setString(1, name);
            pstmt.setString(2, email_good);
            pstmt.setDate(3, date);
            pstmt.setInt(4, id_good);
            pstmt.execute();
            System.out.println("Created new customer:");
            CustomerHelper.showInformation(stmt, "Customer", "CustomerID", id_good);
        } catch (SQLException e) {
            System.err.println("Error: SQLException in Test.testCustomerConstraints()");

        }

        try {
            System.out.println("--- An example of invalid email input: ---");
            System.out.println(name + " | " + email_bad + " | " + date + " | " + id_good);
            pstmt.setString(2, email_bad);
            pstmt.execute();
        } catch (SQLException e) {
            System.err.println("Error: SQLException in Test.testCustomerConstraints()");

        }

        try {
            System.out.println("--- An example of invalid id input (id too large): ---");
            System.out.println(name + " | " + email_good + " | " + date + " | " + id_tooLarge);
            pstmt.setString(2, email_good);
            pstmt.setInt(4, id_tooLarge);
            pstmt.execute();
        } catch (SQLException e) {
            System.err.println("Error: SQLException in Test.testCustomerConstraints()");

        }

        System.out.println("==== End of Test: Constraints for Table Customer =====\n\n");

    }

    public static void testAttendanceConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Customer =======");

        int cid = 1111111;
        int cid_notExist = 1111112;
        int mid = 1001;
        Date date_good = Biblio.convertToDate("2019-11-06");
        Date date_bad = Biblio.convertToDate("2019-11-04");

        try{
            pstmt = conn.prepareStatement("insert into Attendance (MovieId, AttendanceDATE, CustomerId) values (?, ?, ?)");
            System.out.println("--- An example of valid input: ---" );
            System.out.println("MovieID: " +mid+ " | AttendanceDate: " + date_good + " | customerID: " + cid );
            pstmt.setInt(1, mid);
            pstmt.setDate(2, date_good);
            pstmt.setInt(3, cid);
            pstmt.execute();
            System.out.println("Created new attendance:");
            CustomerHelper.showInformation(stmt, "Attendance", "CustomerID", cid);


        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        try{
            System.out.println("\n--- An example of entering a non-existing customer: ---" );
            System.out.println("MovieID: " +mid+ " | AttendanceDate: " + date_good + " | customerID: " + cid_notExist );
            pstmt.setInt(3, cid_notExist);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        try{
            System.out.println("\n--- An example of entering an invalid attendance date : ---" );
            System.out.println("MovieID: " +mid+ " | AttendanceDate: " + date_bad + " | customerID: " + cid );
            pstmt.setDate(2, date_bad);
            pstmt.setInt(3, cid);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Attendance =====\n\n");

    }

    static void testReviewConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Review =======");
        int cid = 1111111;
        int mid = 1001;
        int rid = 111111111;
        int rid2 = 111111112;
        int mid_not_watched = 1002;
        int rate_good = 5;
        int rate_bad = 0;

        String review = "A good movie!";
        Date date_good = Biblio.convertToDate("2019-11-06");
        Date date_tooEarly = Biblio.convertToDate("2019-11-05");
        Date date_tooLate = Biblio.convertToDate("2019-11-14");
        // multiple review

        try{
            pstmt = conn.prepareStatement("insert into Review (CustomerID, MovieID, ReviewDate, Rating, ReviewID, Review) values (?, ?, ?, ?, ?, ?)");
            System.out.println("\n--- An example of valid input: ---" );
            System.out.printf(
                    "| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_good, rate_good, rid, review);
            pstmt.setInt(1, cid);
            pstmt.setInt(2, mid);
            pstmt.setDate(3, date_good);
            pstmt.setInt(4, rate_good);
            pstmt.setInt(5, rid);
            pstmt.setString(6, review);
            pstmt.execute();
            System.out.println("Inserted new review: ");
            CustomerHelper.showInformation(stmt, "Review", "ReviewID", rid);

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of customer did not watch movie ---" );
            System.out.printf(
                    "| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                            "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid_not_watched, date_good, rate_good, rid2, review);
            pstmt.setInt(2, mid_not_watched);
            pstmt.setInt(5, rid2);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of a review date before attendance ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                            "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_tooEarly, rate_good, rid2, review);
            pstmt.setInt(2, mid);
            pstmt.setDate(3, date_tooEarly);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of a review date too late ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_tooLate, rate_good, rid2, review);
            pstmt.setDate(3, date_tooLate);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of an invalid rating ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_good, rate_bad, rid2, review);
            pstmt.setDate(3, date_good);
            pstmt.setInt(4, rate_bad);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of customer review the same movie more than once ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_good, rate_good, rid2, review);
            pstmt.setInt(4, rate_good);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Review =====\n\n");
    }

    static void testEndorsementConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Endorsement =======");
        try{
            pstmt = conn.prepareStatement("insert into Endorsement (ReviewID, CustomerID, EndorsementDate) values (?, ?, ?)");
        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testEndorsementConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Endorsement =====\n\n");
    }

}
