import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.File;
@WebServlet(name = "GenreBrowseServlet", urlPatterns = "/api/list")
public class GenreBrowseServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private FileWriter writer;

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
        // Retrieve parameter id from url request.
        String prefix = request.getParameter("prefix");
        String id = request.getParameter("genre");

        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        String quantity = request.getParameter("quantity");
        String sort = request.getParameter("sort");
        String page = request.getParameter("page");

        long startTime = System.nanoTime();

        String contextPath = request.getServletContext().getRealPath("/");
        String filePath = contextPath + "instance_log.txt";

        try {
            writer = new FileWriter(filePath, true);

            File file = new File(filePath);
            file.setReadable(true, false);
            file.setWritable(true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            long endConnectionTime = System.nanoTime();
            String TJ = String.valueOf(endConnectionTime - startTime);

            String query = "";
            PreparedStatement statement;
            ResultSet rs;

            String[] sortOptions = {
                    "ORDER BY m.title ASC, r.rating DESC",
                    "ORDER BY m.title ASC, r.rating ASC",
                    "ORDER BY m.title DESC, r.rating DESC",
                    "ORDER BY m.title DESC, r.rating ASC",
                    "ORDER BY r.rating ASC, m.title DESC",
                    "ORDER BY r.rating ASC, m.title ASC",
                    "ORDER BY r.rating DESC, m.title DESC",
                    "ORDER BY r.rating DESC, m.title ASC",
            };

            if ((title != null && title.length() != 0 && !title.equals("null")) || (year != null && year.length() != 0 && !year.equals("null")) || (director != null && director.length() != 0 && !director.equals("null")) || (star != null && star.length() != 0 && !star.equals("null"))) {
                String[] search_terms = title.split(" ");
                StringBuilder search_query = new StringBuilder();

                for (String term : search_terms) {
                    search_query.append("+").append(term).append("* ");
                }

                query += "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = m.id) AS sub) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId\n			WHERE sim.movieId = m.id) AS sub\n			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId\n			GROUP BY sub.star_id\n			ORDER BY movieCount DESC) AS sub2) AS stars\nFROM movies m\nJOIN genres_in_movies gim ON m.id = gim.movieId\nLEFT JOIN ratings r ON r.movieId = m.id\nWHERE";
//                query += " m.title LIKE \'%" + title + "%\'";
                query += " MATCH(m.title) AGAINST(\'" + search_query.toString().trim() + "\' IN BOOLEAN MODE)";

                if (year != null && year.length() != 0) {
                    query += " AND m.year = " + Integer.parseInt(year);
                }

                if (director != null && director.length() != 0) {
                    query += " AND m.director LIKE \'%" + director + "%\'";
                }

                if (star != null && star.length() != 0) {
                    query += "\n	AND EXISTS (\n		SELECT *\n		FROM stars s\n		JOIN stars_in_movies sim ON s.id = sim.starId\n		WHERE sim.movieId = m.id AND s.name LIKE \'%" + star + "%\'\n	  )";
                }

                query += "\n" + sortOptions[Integer.parseInt(sort)];
                query += "\nLIMIT " + quantity;
                query += " OFFSET " + (Integer.parseInt(page) - 1)*Integer.parseInt(quantity) + ";";

                statement = conn.prepareStatement(query);
//                System.out.println(statement);
                rs = statement.executeQuery();

            }
            else if (prefix != null && prefix.length() != 0 && prefix.equals("*")) {
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = m.id) AS sub) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId\n			WHERE sim.movieId = m.id) AS sub\n			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId\n			GROUP BY sub.star_id\n			ORDER BY movieCount DESC, star_name ASC) AS sub2) AS stars\nFROM movies m\nJOIN genres_in_movies gim ON m.id = gim.movieId\nLEFT JOIN ratings r ON r.movieId = m.id\nWHERE m.title NOT REGEXP \'^[[:alnum:]]\'";
                query += "\n" + sortOptions[Integer.parseInt(sort)];
                query += "\nLIMIT " + quantity;
                query += " OFFSET " + (Integer.parseInt(page) - 1)*Integer.parseInt(quantity) + ";";
                statement = conn.prepareStatement(query);
                rs = statement.executeQuery();
            }
            else if (prefix != null && prefix.length() != 0 && !prefix.equals("null")) {
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = m.id) AS sub) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId\n			WHERE sim.movieId = m.id) AS sub\n			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId\n			GROUP BY sub.star_id\n			ORDER BY movieCount DESC, star_name ASC) AS sub2) AS stars\nFROM movies m\nJOIN genres_in_movies gim ON m.id = gim.movieId\nLEFT JOIN ratings r ON r.movieId = m.id\nWHERE m.title LIKE \'" + prefix + "%\'";
                query += "\n" + sortOptions[Integer.parseInt(sort)];
                query += "\nLIMIT " + quantity;
                query += " OFFSET " + (Integer.parseInt(page) - 1)*Integer.parseInt(quantity) + ";";
                statement = conn.prepareStatement(query);
                rs = statement.executeQuery();
            }
            else if (id != null && id.length() != 0 && !id.equals("null")) {
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = m.id) AS sub) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId\n			WHERE sim.movieId = m.id) AS sub\n			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId\n			GROUP BY sub.star_id\n			ORDER BY movieCount DESC, star_name ASC) AS sub2) AS stars\nFROM movies m\nJOIN genres_in_movies gim ON m.id = gim.movieId\nLEFT JOIN ratings r ON r.movieId = m.id\nWHERE gim.genreId = ?";
                query += "\n" + sortOptions[Integer.parseInt(sort)];
                query += "\nLIMIT " + quantity;
                query += " OFFSET " + (Integer.parseInt(page) - 1)*Integer.parseInt(quantity) + ";";
                statement = conn.prepareStatement(query);
                statement.setString(1, id);
                rs = statement.executeQuery();
            }
            else {
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,\n	(SELECT GROUP_CONCAT(genre_name, \':\', genre_id ORDER BY genre_name)\n	FROM (\n		SELECT g.genreName AS genre_name, g.id AS genre_id\n		FROM genres g\n		JOIN genres_in_movies gim ON g.id = gim.genreId\n		WHERE gim.movieId = m.id) AS sub) AS genres,\n	(SELECT GROUP_CONCAT(star_name, \':\', star_id)\n	FROM (\n		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount\n		FROM(\n			SELECT s.name AS star_name, s.id AS star_id\n			FROM stars s\n			JOIN stars_in_movies sim ON s.id = sim.starId\n			WHERE sim.movieId = m.id) AS sub\n			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId\n			GROUP BY sub.star_id\n			ORDER BY movieCount DESC, star_name ASC) AS sub2) AS stars\nFROM movies m\nJOIN genres_in_movies gim ON m.id = gim.movieId\nLEFT JOIN ratings r ON r.movieId = m.id";
                query += "\n" + sortOptions[Integer.parseInt(sort)];
                query += "\nLIMIT " + quantity;
                query += " OFFSET " + (Integer.parseInt(page) - 1)*Integer.parseInt(quantity) + ";";

                statement = conn.prepareStatement(query);
                rs = statement.executeQuery(query);
            }

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

            long endTime = System.nanoTime();
            String TS = String.valueOf(endTime - startTime);

            writer.write(TJ +" " + TS + "\n");
            writer.flush();

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

    }
}