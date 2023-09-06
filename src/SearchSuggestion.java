import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@WebServlet("/search-suggestion")
public class SearchSuggestion extends HttpServlet {
	/*
	 * populate the Super hero hash map.
	 * Key is hero ID. Value is hero name.
	 */

	private DataSource dataSource;

	public void init(ServletConfig config) {
		try {
			dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbReadOnly");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}



	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (Connection conn = dataSource.getConnection()) {
			System.out.println(1);

			HttpSession session = request.getSession();

			// get the query string from parameter
			String query = request.getParameter("query");

			HashMap<String, JsonArray> storedSuggestions = (HashMap<String, JsonArray>) session.getAttribute("storedSuggestions");

			if (storedSuggestions == null) {
				storedSuggestions = new HashMap<String, JsonArray>();
			}

			if (storedSuggestions.get(query) == null) {
				// setup the response json array
				JsonArray jsonArray = new JsonArray();

				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("type", "BACKEND SUGGESTIONS");

				jsonArray.add(jsonObject);

				// return the empty json array if query is null or empty
				if (query == null || query.trim().isEmpty()) {
					response.getWriter().write(jsonArray.toString());
					return;
				}

				String[] search_terms = query.split(" ");
				StringBuilder search_query = new StringBuilder();

				for (String term : search_terms) {
					search_query.append("+").append(term).append("* ");
				}

				String sqlQuery = "SELECT *\nFROM movies as m\nWHERE MATCH(m.title) AGAINST(? IN BOOLEAN MODE)\nLIMIT 10;";

				PreparedStatement statement = conn.prepareStatement(sqlQuery);

				statement.setString(1, search_query.toString().trim());

				System.out.println(statement);

				ResultSet rs = statement.executeQuery();

				JsonArray suggestionArray = new JsonArray();

				while (rs.next()) {
					suggestionArray.add(generateJsonObject(rs.getString("id"), rs.getString("title")));
				}

				jsonArray.add(suggestionArray);

				System.out.println(jsonArray);

				storedSuggestions.put(query, suggestionArray);

				session.setAttribute("storedSuggestions", storedSuggestions);

				response.getWriter().write(jsonArray.toString());
			} else {
				System.out.println("EXISTS");
				JsonArray jsonArray = new JsonArray();

				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("type", "FRONT-END SUGGESTIONS");

				jsonArray.add(jsonObject);
				jsonArray.add(storedSuggestions.get(query));

				response.getWriter().write(jsonArray.toString());
			}
			
			// search on superheroes and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
			
//			for (Integer id : superHeroMap.keySet()) {
//				String heroName = superHeroMap.get(id);
//				if (heroName.toLowerCase().contains(query.toLowerCase())) {
//					jsonArray.add(generateJsonObject(id, heroName));
//				}
//			}

		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}
	
	/*
	 * Generate the JSON Object from hero to be like this format:
	 * {
	 *   "value": "Iron Man",
	 *   "data": { "heroID": 11 }
	 * }
	 * 
	 */
	private static JsonObject generateJsonObject(String movieID, String heroName) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", heroName);
		
		JsonObject additionalDataJsonObject = new JsonObject();
		additionalDataJsonObject.addProperty("movieId", movieID);
		
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}


}
