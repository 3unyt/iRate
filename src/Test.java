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
            Tables.initializeAll(stmt);
//

            insertTestMovie(conn, stmt, null);
//            PubAPI.printAllTables(stmt, null);

            testCustomerConstraints(conn, stmt, null);
            testAttendanceConstraints(conn, stmt, null);
            testReviewConstraints(conn, stmt, null);
            testEndorsementConstraints(conn, stmt, null);

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

    private static void insertTestMovie(Connection conn, Statement stmt, PreparedStatement pstmt){
        try{
            stmt.executeUpdate("delete from Movie");
            pstmt = conn.prepareStatement("insert into Movie (Title, MovieID) values (?, ?)");
            pstmt.setString(1, "Frozen");
            pstmt.setInt(2, 1001);
            pstmt.execute();

            pstmt.setString(1, "Frozen II");
            pstmt.setInt(2, 1002);
            pstmt.execute();

            System.out.println("Inserted 2 movies for test:");
            PubAPI.printTable(stmt,null,"Movie");


        } catch (SQLException e){

        }
    }

    public static void testCustomerConstraints(Connection conn, Statement stmt, PreparedStatement pstmt) throws SQLException {
        System.out.println("======= Test for Constraints for Table Customer =======");
        String name = "Elsa";
        String name2 = "Anna";
        String name3 = "Olaf";
        String email_good = "elsa@gmail.com";
        String email2 = "anna@gmail.com";
        String email3 = "olaf@gmail.com";
        String email_bad = "elsa@gmail";
        int id_good = 1111111;
        int id_good2 = 2222222;
        int id_good3 = 3333333;

        Date date = Biblio.convertToDate("2019-11-05");


        try {
            pstmt = conn.prepareStatement("insert into Customer (Name, Email, JoinedDate, CustomerID) values (?, ?, ?, ?)");
            System.out.println("--- Examples of valid input: ---");
            System.out.printf("| Name  |       Email       | JoinedDate | CustomerID | \n" +
                    "| %5s | %16s  | %s | %10d | \n", name, email_good, date, id_good);
            System.out.printf("| %5s | %16s  | %s | %10d | \n", name2, email2, date, id_good2);
            System.out.printf("| %5s | %16s  | %s | %10d | \n", name3, email3, date, id_good3);
            pstmt.setString(1, name);
            pstmt.setString(2, email_good);
            pstmt.setDate(3, date);
            pstmt.setInt(4, id_good);
            pstmt.execute();
            pstmt.setString(1, name2);
            pstmt.setString(2, email2);
            pstmt.setInt(4, id_good2);
            pstmt.execute();
            pstmt.setString(1, name3);
            pstmt.setString(2, email3);
            pstmt.setInt(4, id_good3);
            pstmt.execute();

            System.out.println("Created 3 new customers:");
            PubAPI.printTable(stmt,null, "Customer");


        } catch (SQLException e) {
            System.err.println("Error: SQLException in Test.testCustomerConstraints()");

        }

        try {
            System.out.println("--- An example of invalid email input: ---");
            System.out.printf("| Name  |       Email       | JoinedDate | CustomerID | \n" +
                    "| %5s | %16s  | %s | %10d | \n", name, email_bad, date, id_good);
            pstmt.setString(1, name);
            pstmt.setInt(4, id_good);
            pstmt.setString(2, email_bad);
            pstmt.execute();
        } catch (SQLException e) {
            System.err.println("Error: SQLException in Test.testCustomerConstraints()");

        }


        System.out.println("==== End of Test: Constraints for Table Customer =====\n\n");

    }

    public static void testAttendanceConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Customer =======");

        int cid = 1111111;
        int cid2 = 2222222;
        int cid_notExist = 1111112;
        int mid = 1001;
        int mid2 = 1002;
        Date date_good = Biblio.convertToDate("2019-11-06");
        Date date_bad = Biblio.convertToDate("2019-11-04");

        try{
            pstmt = conn.prepareStatement("insert into Attendance (MovieId, AttendanceDATE, CustomerId) values (?, ?, ?)");
            System.out.println("--- An example of valid input: ---" );
            System.out.printf("| MovieID  | AttendanceDate | CustomerID | \n" +
                    "| %7d | %14s  | %10d | \n", mid,date_good, cid);
            System.out.printf("| %7d | %14s  | %10d | \n", mid2,date_good, cid);
            System.out.printf("| %7d | %14s  | %10d | \n", mid,date_good, cid2);

            pstmt.setInt(1, mid);
            pstmt.setDate(2, date_good);
            pstmt.setInt(3, cid);
            pstmt.execute();
            pstmt.setInt(1, mid2);
            pstmt.execute();

            pstmt.setInt(1, mid);
            pstmt.setInt(3, cid2);
            pstmt.execute();
            System.out.println("\nCreated 3 new attendances:");
            PubAPI.printTable(stmt, null, "Attendance");


        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        try{
            System.out.println("\n--- An example of entering a non-existing customer: ---" );
            System.out.printf("| MovieID  | AttendanceDate | CustomerID | \n" +
                    "| %7d | %14s  | %10d | \n", mid,date_good, cid_notExist);

            pstmt.setInt(3, cid_notExist);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        try{
            System.out.println("\n--- An example of entering an invalid attendance date : ---" );
            System.out.printf("| MovieID  | AttendanceDate | CustomerID | \n" +
                    "| %7d | %14s  | %10d | \n", mid,date_bad, cid);

            pstmt.setDate(2, date_bad);
            pstmt.setInt(3, cid);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testAttendanceConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Attendance =====\n\n");

    }

    public static void testReviewConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Review =======");
        int cid = 1111111;
        int cid2 = 2222222;
        int mid = 1001;
        int mid2 = 1002;
        int mid_not_watched = 1003;
        int rid = 111111111;
        int rid2 = 222222222;
        int rate_good = 5;
        int rate_bad = 0;

        String review = "A good movie!";
        Date date_good = Biblio.convertToDate("2019-11-06");
        Date date_good2 = Biblio.convertToDate("2019-11-08");
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

            pstmt.setInt(1, cid2);
            pstmt.setInt(5, rid2);
            pstmt.execute();

            System.out.println("Inserted 2 new reviews: ");
            CustomerHelper.showInformation(stmt, "Review", "MovieID", mid);

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
                            "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid2, date_tooEarly, rate_good, rid2, review);

            pstmt.setInt(2, mid2);
            pstmt.setDate(3, date_tooEarly);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of a review date too late ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid2, date_tooLate, rate_good, rid2, review);
            pstmt.setDate(3, date_tooLate);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of an invalid rating ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid2, date_good, rate_bad, rid2, review);
            pstmt.setDate(3, date_good);
            pstmt.setInt(4, rate_bad);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        try{
            System.out.println("\n--- An example of customer review the same movie more than once ---" );
            System.out.printf("| CustomerID | MovieID | ReviewDate | Rating | ReviewID  | Review \n" +
                    "| %10d | %7d | %10s | %6d | %d | %s\n", cid, mid, date_good2, rate_good, rid2, review);

            pstmt.setInt(2, mid);
            pstmt.setDate(3, date_good2);
            pstmt.setInt(4, rate_good);
            pstmt.execute();


        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testReviewConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Review =====\n\n");
    }

    static void testEndorsementConstraints(Connection conn, Statement stmt, PreparedStatement pstmt){
        System.out.println("======= Test for Constraints for Table Endorsement =======");
        int rid = 111111111;
        int rid2 = 222222222;
        int cid = 1111111;
        int cid2 = 2222222;
        int cid3 = 3333333;
        Date date_good = Biblio.convertToDate("2019-11-06");
        Date date_tooEarly = Biblio.convertToDate("2019-11-05");
        Date date_tooLate = Biblio.convertToDate("2019-11-10");
        try{
            pstmt = conn.prepareStatement("insert into Endorsement (ReviewID, CustomerID, EndorsementDate) values (?, ?, ?)");
            System.out.println("\n--- An example of valid input: ---" );
            System.out.printf("| ReviewID  | CustomerID | EndorsementDate | \n" +
                            "| %d | %10d | %15s |\n", rid, cid3, date_good);
            pstmt.setInt(1, rid);
            pstmt.setInt(2, cid3);
            pstmt.setDate(3, date_good);
            pstmt.execute();
            System.out.println("Inserted new endorsement: ");
            CustomerHelper.showInformation(stmt, "Endorsement", "ReviewID", rid);

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testEndorsementConstraints()");
        }

        try{
            System.out.println("\n--- An example of review is closed for voting : ---" );
            System.out.printf("| ReviewID  | CustomerID | EndorsementDate | \n" +
                    "| %d | %10d | %15s |\n", rid, cid2, date_tooLate);
            pstmt.setDate(3, date_tooLate);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testEndorsementConstraints()");
        }

        try{
            System.out.println("\n--- An example when a customer tries to endorsed a review by himself/herself : ---" );
            System.out.printf("| ReviewID  | CustomerID | EndorsementDate | \n" +
                    "| %d | %10d | %15s |\n", rid, cid, date_good);
            pstmt.setDate(3, date_good);
            pstmt.setInt(2, cid);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testEndorsementConstraints()");
        }

        try{
            System.out.println("\n--- An example when a customer endorsed more than one review for a movie within one day: ---" );
            System.out.printf("| ReviewID  | CustomerID | EndorsementDate | \n" +
                    "| %d | %10d | %15s |\n", rid2, cid3, date_good);
            pstmt.setInt(1, rid2);
            pstmt.setInt(2, cid3);
            pstmt.execute();

        } catch (SQLException e){
            System.err.println("Error: SQLException in Test.testEndorsementConstraints()");
        }

        System.out.println("==== End of Test: Constraints for Table Endorsement =====\n\n");
    }

}
