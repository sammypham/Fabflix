import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "IndexServlet", urlPatterns = "/api/index")
public class IndexServlet extends HttpServlet {

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
        request.getServletContext().log("getting " + previousItems.size() + " items");

        JsonArray previousItemsJsonArray = new JsonArray();

        for (String movieId : previousItems.keySet()) {
            JsonObject movieJsonObject = new JsonObject();
            HashMap<String, String> contents = previousItems.get(movieId);

            movieJsonObject.addProperty("movie_id", movieId);
            movieJsonObject.addProperty("quantity", contents.get("quantity"));
            movieJsonObject.addProperty("title", contents.get("title"));

            previousItemsJsonArray.add(movieJsonObject);
        }

        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String item = request.getParameter("item");
        String title = request.getParameter("title");
        Integer add = Integer.parseInt(request.getParameter("add"));
        String delete = request.getParameter("del");

        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        HashMap<String, HashMap<String, String>> previousItems = (HashMap<String, HashMap<String, String>>) session.getAttribute("previousItems");

        if (delete != null && delete.equals("1")) {
            previousItems.remove(item);
        }
        else if (previousItems == null) {
            previousItems = new HashMap<String, HashMap<String, String>>();
            HashMap<String, String> contents = new HashMap<String, String>();

            contents.put("quantity", "1");
            contents.put("title", title);

            previousItems.put(item, contents);

            session.setAttribute("previousItems", previousItems);
        }
        else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (previousItems.get(item) != null) {
                    HashMap<String, String> contents = previousItems.get(item);
                    int newQuantity = Integer.parseInt(contents.get("quantity")) + add;

                    contents.put("quantity", Integer.toString(newQuantity));

                    if (newQuantity == 0) {
                        previousItems.remove(item);
                    } else {
                        previousItems.put(item, contents);
                    }
                }
                else {
                    HashMap<String, String> contents = new HashMap<String, String>();

                    contents.put("quantity", "1");
                    contents.put("title", title);

                    previousItems.put(item, contents);
                }
            }
        }

        JsonObject responseJsonObject = new JsonObject();

        JsonArray previousItemsJsonArray = new JsonArray();

        for (String movieId : previousItems.keySet()) {
            JsonObject movieJsonObject = new JsonObject();
            HashMap<String, String> contents = previousItems.get(movieId);

            movieJsonObject.addProperty("movie_id", movieId);
            movieJsonObject.addProperty("quantity", contents.get("quantity"));
            movieJsonObject.addProperty("title", contents.get("title"));

            previousItemsJsonArray.add(movieJsonObject);
        }

        responseJsonObject.add("previousItems", previousItemsJsonArray);

        response.getWriter().write(responseJsonObject.toString());
    }
}
