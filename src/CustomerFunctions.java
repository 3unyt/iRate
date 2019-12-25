import java.sql.*;
import java.util.Date;
import java.util.Scanner;

/**
 * This class is the main page of the customer view
 * First user need to login to an exit account or register a new account
 * Then there are 7 options for customer to choose.
 */
public class CustomerFunctions {
    static Date date = new Date();
    /**
     * Since our default data has last date of 2019-12-14, if you play this program too much days later than 2019-12-14
     * some options may not be allowed (for example, review, endorsement) due to some constraint.
     * You can change static variable today to 2019-12-14
     * by replace next line code with the following code: static java.sql.Date today = PubUtil.convertToDate("2019-12-14");
     */
    static java.sql.Date today = new java.sql.Date(date.getTime());
    static int id=-1;

    /**
     * Different operation for user's choice
     * @param conn
     * @param stmt
     * @param readUser
     */
    public static void userView(Connection conn, Statement stmt, Scanner readUser){
        //get the customer id
        while(id==-1){
            System.out.println("Are you a new user? (y/n)");
            String read= readUser.nextLine();
            //If it is a new user, create an account first
            if(read.equalsIgnoreCase("y")){
                if(createCustomer(conn, readUser)) {
                    System.out.println("Your account has been successfully registered!");
                    System.out.println();
                    //show new account information
                    PubAPI.showInformation(stmt, null,"Customer", "CustomerID", id);
                    System.out.println("Your customer ID is very important, please remember it for next time use.");
                }
            }
            //If it isn't a new user, check the old user id
            else {
                System.out.println("\nPlease enter your customer id: ");
                String inputID = readUser.nextLine();
                id=CustomerHelper.checkID(inputID, conn);
                if(id==-1) {
                    System.out.println("Wrong ID.");
                } else PubAPI.showInformation(stmt, null,"Customer","CustomerID", id);
            }
        }

        //after we got the customer id, list actions customer can do
        int choice = 0;

        while(choice != 7){
            customerMainPage();
            String input = readUser.nextLine();
            choice = PubUtil.validInt(input);
            if(choice < 1 || choice > 7){
                System.out.println("Invalid choice!");
            }
            else if(choice == 1){
                PubAPI.showInformation(stmt, null,"Customer","CustomerID", id);
            }
            else if(choice == 2){
                if(createAttendance(conn,readUser, stmt)) {
                    System.out.println("\nAttendance created success\n");
                    PubAPI.showInformation(stmt, null,"Attendance","CustomerID", id);
                }
            }
            else if(choice == 3){
                if(createReview(conn, readUser, stmt)){
                    System.out.println("\nReview created success\n");
                    PubAPI.showInformation(stmt, null,"Review","CustomerID", id);
                }
            }
            else if(choice == 4){
                if(createEndorsement(conn, readUser, stmt)) {
                    System.out.println("\nEndorsement created success\n\n");
                    PubAPI.showInformation(stmt, null,"Endorsement","CustomerID", id);
                }
            }
            else if(choice == 5){
                printTopRatedMovies(conn, stmt);
            }
            else if(choice == 6){
                System.out.println("\nYour account information: ");
                PubAPI.showInformation(stmt, null, "Customer","CustomerID", id);
                System.out.println("Are you sure you want to delete your account? (y/n)");
                String delete = readUser.nextLine();
                if (delete.equalsIgnoreCase("Y")){
                    deleteCustomer(stmt);
                }
                break;
            }
        }
        id=-1;
    }

    /**
     * The initial page for customer to choose options
     */
    public static void customerMainPage(){
        System.out.println("\n--------- Options for Theatre ------------");
        System.out.println("1. Show account information");
        System.out.println("2. Check in the movie you watched");
        System.out.println("3. Write a review");
        System.out.println("4. Endorse a review");
        System.out.println("5. Show highest rating movies");
        System.out.println("6. Delete my account");
        System.out.println("7. Exit customer page");
        System.out.print("Enter your choice: ");
    }

    /**
     * create new customer account
     * @param conn
     * @param readUser
     * @return
     */
    private static boolean createCustomer(Connection conn, Scanner readUser){
        System.out.println("\n\nRegistering an account...");
        System.out.println("\nPlease input an account name:");
        String name = readUser.nextLine();
        System.out.println();
        System.out.println("\nEnter your email address(for example: abcd@gmail.com): ");
        String email = readUser.nextLine();

        if(!DBFunctions.isValidEmail(email)) return false;
        id = PubUtil.generateID(1, conn);
        return CustomerHelper.insertCustomer(conn, name, email, id, today);
    }

    /**
     * create attendance of the movie customer watched
     * @param conn
     * @param readUser
     */
    private static boolean createAttendance(Connection conn, Scanner readUser, Statement stmt){
        //Show all movies and movie id
        System.out.println("\n\n");
        PubAPI.printTable(stmt,null, "Movie");
        System.out.println("Please enter the movie id of the movie you watched: ");

        String inputMid = readUser.nextLine();
        int mid = PubUtil.validInt(inputMid);
        if(mid == -1) return false;
        if(!PubUtil.taken(conn, mid, 0)) {
            System.out.println("Wrong movie id");
            return false;
        }

        System.out.println("The date you watched the movie: \nformat: YYYY-MM-DD, for example, 2019-11-30");
        String inputDate = readUser.nextLine();
        java.sql.Date theDate = PubUtil.convertToDate(inputDate);
        if(theDate == null) return false;

        return CustomerHelper.insertAttendance(conn, id, mid, theDate);
    }

    /**
     * create a review of the movie customer watched
     * @param conn
     * @param readUser
     * @param stmt
     */
    public static boolean createReview(Connection conn, Scanner readUser, Statement stmt){
        //Show all movies and movie id
        System.out.println("\n\n");
        PubAPI.printTable(stmt, null,"Movie");
        System.out.println("Please enter the movie id of the movie you want to review: ");

        String inputMid = readUser.nextLine();
        int mid = PubUtil.validInt(inputMid);
        if(mid==-1) return false;
        if(!PubUtil.taken(conn, mid, 0)) {
            System.out.println("Wrong movie id");
            return false;
        }

        System.out.println("1-5, 5 is the best and 1 is the worst. What's your rating for this movie?");
        String inputRate = readUser.nextLine();
        int rate = PubUtil.validInt(inputRate);
        if(rate < 1 || rate > 5) {
            System.out.println("Invalid rate!");
            return false;
        }

        System.out.println("Give some comment to this Movie: ");
        String review = readUser.nextLine();
        if(review.isEmpty()){
            System.out.println("Comment cannot be empty");
            return false;
        }

        int rid = PubUtil.generateID(2, conn);

        return CustomerHelper.insertReview(conn, id, mid, rid, rate, review, today);
    }

    /**
     * create an endorsement for a review
     * @param conn
     * @param readUser
     * @param stmt
     */
    public static boolean createEndorsement(Connection conn, Scanner readUser, Statement stmt){
        //Show all reviews
        System.out.println("\n\n");
        PubAPI.printTable(stmt,null, "Review");
        System.out.println("Please enter the review id you want to endorse: ");

        String inputRid = readUser.nextLine();
        int rid = PubUtil.validInt(inputRid);
        if(rid == -1) return false;

        return CustomerHelper.insertEndorsement(conn,rid, id, today);
    }


    /**
     * print 3 top rated movies
     */
    public static boolean printTopRatedMovies(Connection conn, Statement stmt){
        System.out.println("\n\n");
        ResultSet rs = CustomerHelper.getTopMovies(stmt);
        if(rs==null) return false;
        System.out.println("Top 3 rated movies: ");
        PubAPI.printTop(rs, 3);
        return true;
    }


    /**
     * Delete the customer account from database
     */
    public static void deleteCustomer(Statement stmt){
        try{
            stmt.execute("delete from Customer where CustomerID = "+ id);
            System.out.println("Deleted customer: " + id);

        } catch (SQLException e){
            System.err.println("Failed to delete account: " + id);
            e.printStackTrace();
        }
    }

}
