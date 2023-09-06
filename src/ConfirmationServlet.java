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

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        HashMap<String, HashMap<String, String>> totalPurchases = (HashMap<String, HashMap<String, String>>) session.getAttribute("totalPurchases");

        if (totalPurchases == null) {
            totalPurchases = new HashMap<String, HashMap<String, String>>();
        }

        JsonArray totalPurchasesJsonArray = new JsonArray();

        for (String movieId : totalPurchases.keySet()) {
            JsonObject movieJsonObject = new JsonObject();
            HashMap<String, String> contents = totalPurchases.get(movieId);

            movieJsonObject.addProperty("movie_id", movieId);
            movieJsonObject.addProperty("sale_id", contents.get("sale-id"));
            movieJsonObject.addProperty("quantity", contents.get("quantity"));
            movieJsonObject.addProperty("title", contents.get("title"));

            totalPurchasesJsonArray.add(movieJsonObject);
        }

        responseJsonObject.add("totalPurchases", totalPurchasesJsonArray);

        float cartPrice = 0;

        for (String movieId : totalPurchases.keySet()) {
            HashMap<String, String> contents = totalPurchases.get(movieId);

            cartPrice += Integer.parseInt(contents.get("quantity")) * 5;
        }

        responseJsonObject.addProperty("cartPrice", Float.toString(cartPrice));

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }
}
