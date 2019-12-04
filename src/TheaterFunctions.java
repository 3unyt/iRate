import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TheaterFunctions {

    public static void theaterView(Connection conn, Statement stmt, Scanner readUser){
        System.out.println("=========  Welcome! Theater Employee =========");

        int choice = 0;

        while(choice != 6){
            printTheaterOptions();
            choice = readUser.nextInt();
            readUser.nextLine();
            if (choice == 1) PubAPI.printTable(stmt, null, "Movie");
            else if (choice == 2) createMovieByUser(readUser, conn, stmt);
            else if (choice == 3) deleteMovieByUser(conn, stmt, readUser);
            else if (choice == 4) selectFreeTicketCustomer(conn, readUser);
            else if (choice == 5) selectCustomerWithGift(conn, stmt, readUser);
            else if (choice != 6) System.out.println("Invalid input! Please select from 1 to 6!");
        }
    }

    private static void printTheaterOptions(){
        System.out.println(
                "\n--------- Options for Theatre ------------\n" +
                "1. Print all movies\n" +
                "2. Insert a movie\n" +
                "3. Delete a movie\n" +
                "4. Select the author of a top-rated review\n" +
                "5. Select a lucky customer for today\n" +
                "6. Exit theater view\n" +
                "Enter your option: "
        );
    }


    /**
     * Create a movie from user input.
     * @param userInput
     * @param conn
     */
    public static void createMovieByUser(Scanner userInput, Connection conn, Statement stmt){
        System.out.print("Please input the movie title: ");
        String movieTitle = userInput.nextLine();

        int movieID = Biblio.generateID(0, conn);

        TheaterHelper.insertMovie(conn, movieTitle, movieID);
    }


    /**
     * Delete a movie from database. Ask the user to enter the movie id.
     * Confirm the movie title with user before deleting the movie.
     * @param conn
     * @param userInput
     * @return true if the movie is deleted, false otherwise
     */
    public static boolean deleteMovieByUser(Connection conn, Statement stmt, Scanner userInput){
        System.out.print("Please enter the movie id you want to delete: ");
        int movieID = userInput.nextInt();
        userInput.nextLine();
        if (!PubAPI.inTable(conn, stmt, "Movie", "MovieID", movieID)){
            System.out.println("Failed to delete! Movie not in table!");
            return false;
        }
        System.out.println("Are you sure you want to delete this movie (y/n)? ");
        CustomerHelper.showInformation(stmt, "Movie", "MovieID", movieID);
        String input = userInput.nextLine();
        if (input.equalsIgnoreCase("Y")){
            TheaterHelper.deleteMovie(stmt, movieID);
            return true;
        } else {
            System.out.println("Did not delete movie.");
            return false;
        }
    }



    public static void selectFreeTicketCustomer(Connection conn, Scanner userInput){
        System.out.println("------ Select Top Review Author --------");
        System.out.print("Please enter the date: ");
        String inputDate = userInput.nextLine();
        java.sql.Date date = Biblio.convertToDate(inputDate);
        if(date==null) return;
        int[] id_set = selectTopReviewCustomer(conn, date);
        int customerID = id_set[0];
        int reviewID = id_set[1];
        int movieID = id_set[2];
        int vote = id_set[3];
        System.out.printf("Congratulations to customer: %d\n" +
                        "   You have won a free movie ticket!\n" +
                        "   for writing a top-voted review %d for movie: %d\n" +
                        "   that has %d vote(s).", customerID, reviewID, movieID, vote);
    }

    /**
     * Select the top review customer from the database
     * @param conn
     * @param today
     * @return customer id of the author of the top-voted review
     */
    private static int[] selectTopReviewCustomer(Connection conn, java.sql.Date today){

        LocalDate latestReviewDateLocal = today.toLocalDate().minusDays(3);
        java.sql.Date latestReviewDate = java.sql.Date.valueOf(latestReviewDateLocal);

        PreparedStatement select_top_review_for_movie;
        PreparedStatement select_review_author;

        ResultSet rs;
        int movieID=0;
        int ReviewID=0;
        int author_id = 0;
        int topReviewID = 0;
        int totalMovie = 0;
        int maxVote = 0;
        ArrayList<Integer> movie_list;
        try{
            movie_list = selectMoviesFromReviews(conn, latestReviewDate);
            System.out.println("All movies that has been reviewed no later than " + latestReviewDate + ":");
            System.out.println(movie_list);
            select_top_review_for_movie = conn.prepareStatement(
                    "select reviewID, count(*) as vote " +
                            "from Endorsement " +
                            "    where ReviewID in " +
                            "            (select Review.ReviewID from Review " +
                            "            where MovieID = ? and ReviewDate <= ?) " +
                            "    group by ReviewID " +
                            "    order by vote desc "
                            );
            select_top_review_for_movie.setDate(2, latestReviewDate);

            select_review_author = conn.prepareStatement("select CustomerID from Review where ReviewID = ?");

            int vote;
            // for each movie, select the top voted review, assume there is no tie on voting
            for (Integer id : movie_list) {
                movieID = id;
                select_top_review_for_movie.setInt(1, movieID);

                rs = select_top_review_for_movie.executeQuery();

//                CustomerHelper.printTop(rs);

                if (rs.next()) {
                    ReviewID = rs.getInt("ReviewID");
                    vote = rs.getInt("vote");
                    if (vote > maxVote) {
                        maxVote = vote;
                        topReviewID = ReviewID;
                        totalMovie = movieID;
                    }
                }

            }

            // get the author for the top-rated reviews
            select_review_author.setInt(1, topReviewID);
            rs = select_review_author.executeQuery();
            if (rs.next()) {
                author_id = rs.getInt(1);
            }

        } catch (SQLException e){
            System.err.println("SQL error in method theaterFunctions.selectTopReviewCustomer().");
            e.printStackTrace();
            System.exit(1);
        }
        return new int[]{author_id, topReviewID, totalMovie, maxVote};

    }

    /**(yuting)
     * Select all movies that has been reviewed no later than latestReviewDate.
     * @param conn database connection
     * @param latestReviewDate the latest review date of the movie
     * @return an array list of movie id that has been reviewed no later than latestReviewDate.
     */
    private static ArrayList<Integer> selectMoviesFromReviews(Connection conn, java.sql.Date latestReviewDate){
        ResultSet rs = null;
        PreparedStatement select_movies_with_valid_reviews;


        ArrayList<Integer> movie_list = new ArrayList<>();
        int movieID;
        try {

            select_movies_with_valid_reviews = conn.prepareStatement(
                    "select MovieID from Review where ReviewDate <= ? group by MovieID"
            );
            select_movies_with_valid_reviews.setDate(1, latestReviewDate);
            rs = select_movies_with_valid_reviews.executeQuery();
            while (rs.next()) {
                // put result in movie list
                movieID = rs.getInt(1);
                movie_list.add(movieID);
            }

        } catch(SQLException e){

        }
        return movie_list;

    }

    /**(yuting)
     * Select a lucky customer for winning a free gift.
     * @param conn
     * @param userInput for theatre to input the current date
     */
    public static void selectCustomerWithGift(Connection conn, Statement stmt, Scanner userInput){
        System.out.println("--------- Select a Lucky Customer ---------");

        // input date
        System.out.print("Please enter the date: ");
        String inputDate = userInput.nextLine();
        java.sql.Date date = Biblio.convertToDate(inputDate);

        // get id of the lucky customer
        int customerID = selectLuckyCustomer(conn, date);

        // no customer voted today
        if (customerID==0){
            System.out.println("Sorry, there is no customer voted for reviews today.");
            return;
        }


        System.out.println("Congratulations to our lucky customer: ");
        CustomerHelper.showInformation(stmt, "Customer", "CustomerID", customerID);

    }

    /**(yuting)
     * Select a lucky customer from those who voted a review within a given date
     * @param conn database connection
     * @param date the given date
     * @return customer id of the lucky customer. 0 if there is no valid customer
     */
    private static int selectLuckyCustomer(Connection conn, java.sql.Date date){
        PreparedStatement selectAllCustomers;
        ResultSet rs = null;
        List<Integer> customer_list = new ArrayList<>();
        int customerID;
        try{
            selectAllCustomers = conn.prepareStatement("select customerID from Endorsement where endorsementDate = ?");
            selectAllCustomers.setDate(1, date);
            rs = selectAllCustomers.executeQuery();
            while (rs.next()) {
                // put result in movie list
                customerID = rs.getInt(1);
                customer_list.add(customerID);
            }
        } catch(SQLException e){
            System.err.println("SQL error in method theaterFunctions.selectLuckyCustomer().");
            System.exit(1);
        }
        if (customer_list.size() == 0){
            return 0;
        }

        // select a random customer from customer list
        Random random = new Random();
        int index = random.nextInt(customer_list.size());

        return customer_list.get(index);

    }




}
