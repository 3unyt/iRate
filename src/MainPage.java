import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

/**
 * This class is the main page of the iRate
 * There are 6 options.The first one is to initialize all tables. The second one is to load default data. The third one is to show
 * all tables. The forth one is to direct user to customer view page. The fifth one is
 * to direct user to theater view page. The last one is to exit the program.
 */
public class MainPage {

    public static void main(String[] args) throws SQLException {
        Connection conn = Tables.newConnection();
        Statement stmt = conn.createStatement();
        System.out.println();

        int option  = 0;
        Scanner readUser = new Scanner(System.in);
        while(option != 6){
            printOptions();
            String choice = readUser.nextLine();
            option = PubUtil.validInt(choice);
            if(option < 1 || option > 6){
                System.out.println("Invalid choice!");
            }
            else if(option == 1){
                Tables.initializeAll(stmt);
            }
            else if(option == 2){
                PubAPI.loadDefaultData(conn, stmt);
            }
            else if(option == 3){
                PubAPI.printAllTables(stmt, null);
            }
            else if(option == 4){
                CustomerFunctions.userView(conn, stmt, readUser);
            }
            else if(option == 5){
                TheaterFunctions.theaterView(conn, stmt, readUser);
            }
        }
        System.out.println("\nGOODBYE! SEE YOU NEXT TIME!");
        conn.close();

    }

    /**
     * The initial page for user to choose options
     */
    public static void printOptions(){
        System.out.println("\n--------------------------------------" +
                "\nWELCOME TO IRATE MOVIE RATING SYSTEM" +
                "\n--------------------------------------");
        System.out.println("1. Initialize all tables");
        System.out.println("2. Load sample data");
        System.out.println("3. Print all tables");
        System.out.println("4. Log in as customer");
        System.out.println("5. Log in as an employee at theater");
        System.out.println("6. Exit program");
        System.out.print("Enter your choice: ");
    }

}
