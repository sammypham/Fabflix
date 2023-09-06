import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT sub.id, sub.title, sub.year, sub.director, sub.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = sub.id) AS sub2) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub2.star_name, sub2.star_id, COUNT(sub2.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId) AS sub2\n			LEFT JOIN stars_in_movies ON sub2.star_id = stars_in_movies.starId\n            WHERE stars_in_movies.movieId = sub.id\n			GROUP BY sub2.star_id\n			ORDER BY movieCount DESC, star_name ASC) AS sub3) AS stars\nFROM (\n    SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating\n    FROM movies m\n    JOIN genres_in_movies gim ON m.id = gim.movieId\n    LEFT JOIN ratings r ON r.movieId = m.id\n    ORDER BY r.rating DESC, m.title ASC\n    LIMIT 25\n) AS sub;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_rating = rs.getString("rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
