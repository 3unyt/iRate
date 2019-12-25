import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class implements functions for TheaterFunctions class helper function
 */
public class TheaterHelper {

    /**
     * insert a movie into the table Movies
     * @param movieTitle the title of movie
     * @param movieID the movie id
     */
    public static void insertMovie(Connection conn, String movieTitle, int movieID){
        PreparedStatement movie;
        try{

            movie = conn.prepareStatement("insert into Movie (Title, MovieID) values (?, ?)");
            movie.setString(1, movieTitle);
            movie.setInt(2, movieID);
            movie.executeUpdate();

            System.out.println("Inserted new movie: ");
            Statement stmt = conn.createStatement();
            PubAPI.showInformation(stmt, null, "Movie", "MovieID", movieID);

        } catch (SQLException e){
            System.err.println("Failed to insert movie: " + movieTitle);
        }
    }

    /**
     * Delete a movie from database.
     * @param movieID
     * @return true if the movie is successfully deleted. False otherwise.
     */
    public static void deleteMovie(Statement stmt, int movieID){

        try{
            stmt.execute("delete from Movie where movieID = "+ movieID);
            System.out.println("Deleted movie: " + movieID);

        } catch (SQLException e){
            System.err.println("Failed to delete movie: " + movieID);
        }

    }
}
