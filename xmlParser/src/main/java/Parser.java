import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Parser {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");

            ActorHandler actorHandler = new ActorHandler(dbConnection);
            actorHandler.run();
            actorHandler.close();

            MainHandler mainHandler = new MainHandler(dbConnection);
            mainHandler.run();
            mainHandler.close();

            CastHandler castHandler = new CastHandler(dbConnection);
            castHandler.run();
            castHandler.close();

            dbConnection.close();

            System.out.println("Inserted " + actorHandler.starInsertCount + " stars.");
            System.out.println("Inserted " + mainHandler.genreInsertCount + " genres.");
            System.out.println("Inserted " + mainHandler.movieInsertCount + " movies.");
            System.out.println("Inserted " + mainHandler.genreInMovieInsertCount + " genres_in_movies.");
            System.out.println("Inserted " + castHandler.starInMovieInsertedCount + " stars_in_movies.");

            System.out.println(mainHandler.movieInconsistentCount + " movies inconsistent.");
            System.out.println(mainHandler.movieDuplicateCount + " movies duplicate.");
            System.out.println(castHandler.noStarsInMovieCount + " movies has no stars.");
            System.out.println(castHandler.movieNotFoundCount + " movies not found.");
            System.out.println(actorHandler.starDuplicateCount + " stars duplicate.");
            System.out.println(castHandler.starNotFoundCount + " stars not found.");

        } catch (ClassNotFoundException e) {
            // Handle the exception
            e.printStackTrace();
        } catch (SQLException e) {
            // Handle any SQL-related exceptions
            e.printStackTrace();
        }
    }
}