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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.io.BufferedReader;
import java.util.Date;
import java.util.HashMap;

@WebServlet(name = "SubmitSearchServlet", urlPatterns = "/api/submit-search")
public class SubmitSearchServlet extends HttpServlet {
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
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        long lastAccessTime = session.getLastAccessedTime();

        JsonObject responseJsonObject = new JsonObject();

        String storedSearch = session.getAttribute("storedSearch").toString();

        if (storedSearch == null) {
            storedSearch = "";
        }

        responseJsonObject.addProperty("storedSearch", storedSearch);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        PrintWriter out = response.getWriter();

        StringBuilder payload = new StringBuilder();
        String line;

        BufferedReader reader = request.getReader();

        while ((line = reader.readLine()) != null) {
            payload.append(line);
        }

        session.setAttribute("storedSearch", payload);

        response.setStatus(200);
    }
}