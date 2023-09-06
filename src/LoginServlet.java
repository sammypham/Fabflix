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
import java.sql.SQLException;
import java.net.URI;
import java.net.URISyntaxException;


import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login", "/api/employee", "/api/mobile-login"})
public class LoginServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        String contextPath = request.getContextPath();
        String trimmedUri = request.getRequestURI().substring(contextPath.length());

        // Verify reCAPTCHA
        if (trimmedUri.equals("/api/login") || trimmedUri.equals("/api/employee")) {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                out.println("<html>");
                out.println("<head><title>Error</title></head>");
                out.println("<body>");
                out.println("<p>recaptcha verification error</p>");
                out.println("<p>" + e.getMessage() + "</p>");
                out.println("</body>");
                out.println("</html>");

                out.close();
                return;
            }
        }

        response.setContentType("text/html"); // Response mime type

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

//            System.out.println("USERNAME: " + username);
//            System.out.println("PASSWORD: "+ password);

            try {
                if (trimmedUri.equals("/api/login") || trimmedUri.equals("/api/mobile-login")) {
                    String query = "SELECT *\nFROM customers\nWHERE customers.email = ?";

                    // Declare our statement
                    PreparedStatement statement = conn.prepareStatement(query);

                    // Set the parameter using the parameter index
                    statement.setString(1, username);

                    // Perform the query
                    ResultSet rs = statement.executeQuery();

                    boolean success = false;

                    if (rs.next()) {
                        // get the encrypted password from the database
                        String encryptedPassword = rs.getString("password");

                        // use the same encryptor to compare the user input password with encrypted password stored in DB
                        success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                    }

                /* This example only allows username/password to be test/test
                /  in the real project, you should talk to the database to verify username/password
                */
                    JsonObject responseJsonObject = new JsonObject();

                    if (success) {
                        // Login success:

                        // set this user into the session
                        request.getSession().setAttribute("user", new User(username));
                        request.getSession().setAttribute("customerId", rs.getString("id"));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");

                    } else {
                        // Login fail
                        responseJsonObject.addProperty("status", "fail");
                        // Log to localhost log
                        request.getServletContext().log("Login failed");

                        responseJsonObject.addProperty("message", "Invalid login credentials");
                    }

                    out.write(responseJsonObject.toString());
                } else if (trimmedUri.equals("/api/employee")) {
                    String query = "SELECT *\nFROM employees\nWHERE employees.email = ?";

                    // Declare our statement
                    PreparedStatement statement = conn.prepareStatement(query);

                    // Set the parameter using the parameter index
                    statement.setString(1, username);

                    // Perform the query
                    ResultSet rs = statement.executeQuery();

                    boolean success = false;

                    if (rs.next()) {
                        // get the encrypted password from the database
                        String encryptedPassword = rs.getString("password");

                        // use the same encryptor to compare the user input password with encrypted password stored in DB

                        if (encryptedPassword.equals(password)) {
                            success = true;
                        }
//                        success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                    }

                /* This example only allows username/password to be test/test
                /  in the real project, you should talk to the database to verify username/password
                */
                    JsonObject responseJsonObject = new JsonObject();

                    if (success) {
                        // Login success:

                        // set this user into the session
                        request.getSession().setAttribute("user", new User(username));
                        request.getSession().setAttribute("employeeId", rs.getString("email"));

                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "success");

                    } else {
                        // Login fail
                        responseJsonObject.addProperty("status", "fail");
                        // Log to localhost log
                        request.getServletContext().log("Login failed");

                        responseJsonObject.addProperty("message", "Invalid login credentials");
                    }

                    out.write(responseJsonObject.toString());
                }

            }
            catch (SQLException e) {
                // Handle any SQL exceptions that occur
                e.printStackTrace();
            }

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