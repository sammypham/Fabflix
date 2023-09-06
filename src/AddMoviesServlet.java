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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

@WebServlet(name = "AddMoviesServlet", urlPatterns = "/api/addmovie")
public class AddMoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
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
            String movie_title = request.getParameter("movie-title");
            String movie_year = request.getParameter("movie-year");
            String movie_director = request.getParameter("movie-director");
            String star_name = request.getParameter("star-name");
            String star_birthYear = request.getParameter("star-birthYear");
            String genre_name = request.getParameter("genre-name");

            String call = "CALL add_movie(?, ?, ?, ?, ?, ?)";

            PreparedStatement statement = conn.prepareCall(call);

            statement.setString(1, movie_title);
            statement.setString(2, movie_year);
            statement.setString(3, movie_director);
            statement.setString(4, star_name);

            if (star_birthYear.isBlank()) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setString(5, star_birthYear);
            }

            statement.setString(6, genre_name);

            statement.execute();

            ResultSet rs = statement.getResultSet();

            JsonObject jsonObject = new JsonObject();

            if (rs.next()) {
                jsonObject.addProperty("successMessage", rs.getString("message"));
            }

            rs.close();
            statement.close();

            out.write(jsonObject.toString());

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
