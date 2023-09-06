import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadWrite");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        HashMap<String, HashMap<String, String>> previousItems = (HashMap<String, HashMap<String, String>>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, HashMap<String, String>>();
        }
        // Log to localhost log
        request.getServletContext().log("cart price");

        float cartPrice = 0;

        for (String movieId : previousItems.keySet()) {
            HashMap<String, String> contents = previousItems.get(movieId);

            cartPrice += Integer.parseInt(contents.get("quantity")) * 5;
        }

        responseJsonObject.addProperty("cartPrice", Float.toString(cartPrice));

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String cardNum = request.getParameter("card-num");
            String firstName = request.getParameter("first-name");
            String lastName = request.getParameter("last-name");
            String expDate = request.getParameter("date");

            if (cardNum != null && cardNum.length() != 0 && firstName != null && firstName.length() != 0 && lastName != null && lastName.length() != 0 && expDate != null && expDate.length() != 0) {
                HttpSession session = request.getSession();

                String query = "SELECT *\nFROM creditcards\nWHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?;";

                PreparedStatement statement = conn.prepareStatement(query);

                statement.setString(1, cardNum);
                statement.setString(2, firstName);
                statement.setString(3, lastName);
                statement.setString(4, expDate);

                ResultSet rs = statement.executeQuery();

                JsonObject responseJsonObject = new JsonObject();

                if (rs.next()) {
                    HashMap<String, HashMap<String, String>> previousItems = (HashMap<String, HashMap<String, String>>) session.getAttribute("previousItems");

                    if (previousItems == null || previousItems.size() == 0) {
                        throw new RuntimeException("Cart is empty");
                    }
                    else {
                        String customerId = (request.getSession().getAttribute("customerId")).toString();
                        HashMap<String, HashMap<String, String>> totalPurchases = (HashMap<String, HashMap<String, String>>) session.getAttribute("totalPurchases");

                        if (totalPurchases == null) {
                            totalPurchases = new HashMap<String, HashMap<String, String>>();
                        }

                        for (String movieId : previousItems.keySet()) {
                            HashMap<String, String> contents = previousItems.get(movieId);

                            String insertion = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?);";

                            PreparedStatement newStatement = conn.prepareStatement(insertion, Statement.RETURN_GENERATED_KEYS);

                            newStatement.setString(1, customerId);
                            newStatement.setString(2, movieId);
                            newStatement.setString(3, (LocalDate.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                            newStatement.executeUpdate();
                            ResultSet newRs = newStatement.getGeneratedKeys();

                            if (newRs.next()) {
                                int id = newRs.getInt(1);

                                if (totalPurchases.containsKey(movieId) ) {
                                    HashMap<String, String> newContents = totalPurchases.get(movieId);
                                    newContents.put("quantity", String.valueOf(Integer.parseInt((newContents.get("quantity"))) + Integer.parseInt((previousItems.get(movieId)).get("quantity"))));

                                    totalPurchases.put(movieId, newContents);
                                } else {
                                    HashMap<String, String> newContents = new HashMap<String, String>();
                                    newContents.put("sale-id", String.valueOf(id));
                                    newContents.put("title", (previousItems.get(movieId)).get("title"));
                                    newContents.put("quantity", (previousItems.get(movieId)).get("quantity"));

                                    totalPurchases.put(movieId, newContents);
                                }
                            }
                        }

                        session.setAttribute("totalPurchases",  totalPurchases);
                        session.setAttribute("previousItems", new HashMap<String, HashMap<String, String>>());
                    }

                    response.getWriter().write(responseJsonObject.toString());

                } else {
                    throw new RuntimeException("Invalid payment information");
                }

            } else {
                throw new RuntimeException("Invalid payment information");
            }
        }
        catch (Exception e) {
            // Write error message JSON object to output
            System.out.println("ERROR");
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println(jsonObject.toString());
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
