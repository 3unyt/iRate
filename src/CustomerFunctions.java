import java.sql.*;
import java.util.Date;
import java.util.Scanner;

public class CustomerFunctions {
    static Date date = new Date();
    static java.sql.Date today = new java.sql.Date(date.getTime());
    static CustomerHelper customer = new CustomerHelper();
    static int id=-1;

    /**
     * Different operation for user's choice
     * @param conn
     * @param stmt
     * @param readUser
     */
    public static void userView(Connection conn, Statement stmt,Scanner readUser){
        //get the customer id
        while(id==-1){
            System.out.println("Are you a new user? (y/n)");
            String read= readUser.nextLine();
            //If it is a new user, create an account first
            if(read.equals("y") || read.equals("Y")){
                if(createCustomer(conn, readUser)) {
                    System.out.println("Your account has been successfully registered!");
                    System.out.println();
                    //show new account information
                    customer.showInformation(stmt, "Customer", "CustomerID", id);
                    System.out.println("Your customer ID is very important, please remember it for next time use.");
                }
            }
            //If it isn't a new user, check the old user id
            else {
                System.out.println("\nPlease enter your customer id: ");
                String inputID = readUser.nextLine();
                id=customer.checkID(inputID, conn);
                if(id==-1) {
                    System.out.println("Wrong ID.");
                } else customer.showInformation(stmt, "Customer","CustomerID", id);
            }
        }

        //after we got the customer id, list actions customer can do
        int choice = 0;

        while(choice != 7){
            customerMainPage();
            String input = readUser.nextLine();
            choice = Biblio.validInt(input);
            if(choice < 1 || choice > 7){
                System.out.println("Invalid choice!");
            }
            else if(choice == 1){
                customer.showInformation(stmt, "Customer","CustomerID", id);
            }
            else if(choice == 2){
                if(createAttendance(conn,readUser, stmt)) {
                    System.out.println("\nAttendance created success\n");
                    customer.showInformation(stmt, "Attendance","CustomerID", id);
                }
            }
            else if(choice == 3){
                if(createReview(conn, readUser, stmt)){
                    System.out.println("\nReview created success\n");
                    customer.showInformation(stmt, "Review","CustomerID", id);
                }
            }
            else if(choice == 4){
                if(createEndorsement(conn, readUser, stmt)) {
                    System.out.println("\nEndorsement created success\n\n");
                    customer.showInformation(stmt, "Endorsement","CustomerID", id);
                }
            }
            else if(choice == 5){
                printTopRatedMovies(conn, stmt);
            }
            else if(choice == 6){
                System.out.println("\nYour account information: ");
                customer.showInformation(stmt, "Customer","CustomerID", id);
                System.out.println("Are you sure you want to delete your account? (y/n)");
                String delete = readUser.nextLine();
                if (delete.equalsIgnoreCase("Y")){
                    deleteCustomer(conn);
                }
            }
        }
    }

    public static void customerMainPage(){
        System.out.println("\n1. Show account information");
        System.out.println("2. Check in the movie you watched");
        System.out.println("3. Write a review");
        System.out.println("4. Endorse a review");
        System.out.println("5. Show highest rating movies");
        System.out.println("6. Delete my account");
        System.out.println("7. Exit customer page");
        System.out.println("Enter your choice: ");
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
        id = Biblio.generateID(1, conn);
//        if(!customer.validEmail(email)) return false;
        return customer.insertCustomer(conn, name, email, id, today);
    }

    /**
     * create attendance of the movie customer watched
     * @param conn
     * @param readUser
     */
    private static boolean createAttendance(Connection conn, Scanner readUser, Statement stmt){
        //Show all movies and movie id
        System.out.println("\n\n");
        CustomerHelper.showTable(stmt, "Movie");
        System.out.println("Please enter the movie id of the movie you watched: ");

        String inputMid = readUser.nextLine();
        int mid = Biblio.validInt(inputMid);
        if(mid == -1) return false;
        if(!Biblio.taken(conn, mid, 0)) {
            System.out.println("Wrong movie id");
            return false;
        }

        System.out.println("The date you watched the movie: \nformat: YYYY-MM-DD, for example, 2019-11-30");
        String inputDate = readUser.nextLine();
        java.sql.Date theDate = Biblio.convertToDate(inputDate);
        if(theDate == null) return false;

        return customer.insertAttendance(conn, id, mid, theDate);
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
        CustomerHelper.showTable(stmt, "Movie");
        System.out.println("Please enter the movie id of the movie you want to review: ");

        String inputMid = readUser.nextLine();
        int mid = Biblio.validInt(inputMid);
        if(mid==-1) return false;
        if(!Biblio.taken(conn, mid, 0)) {
            System.out.println("Wrong movie id");
            return false;
        }

        System.out.println("1-5, 5 is the best and 1 is the worst. What's your rating for this movie?");
        String inputRate = readUser.nextLine();
        int rate = Biblio.validInt(inputRate);
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

        int rid = Biblio.generateID(2, conn);

        return customer.insertReview(conn, id, mid, rid, rate, review, today);
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
        CustomerHelper.showTable(stmt, "Review");
        System.out.println("Please enter the review id you want to endorse: ");

        String inputRid = readUser.nextLine();
        int rid = Biblio.validInt(inputRid);
        if(rid == -1) return false;

        return customer.insertEndorsement(conn, id, rid, today);
    }


    /**
     * print top rated movies within a week
     */
    public static boolean printTopRatedMovies(Connection conn, Statement stmt){
        System.out.println("\n\n");
        ResultSet rs = CustomerHelper.getTopMovies(conn);
        if(rs==null) return false;
        System.out.println("Top 3 rated movies: ");
        CustomerHelper.printTop(rs);
        return true;
    }


    /**
     * Delete the customer account from database
     */
    public static void deleteCustomer(Connection conn){
        PreparedStatement deleteCustomerStatement;
        try{
            deleteCustomerStatement = conn.prepareStatement("delete from Customer where CustomerID = "+ id);
            deleteCustomerStatement.executeQuery();
            System.out.println("Deleted customer: " + id);

        } catch (SQLException e){
            System.err.println("Failed to delete account: " + id);
        }
    }

}
