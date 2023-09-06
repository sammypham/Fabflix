import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

public class CastHandler extends DefaultHandler {
    boolean bTitle = false;
    boolean bActor = false;
    String movieTitle = "";


    HashMap<String, String> hashMap = new HashMap<>();
    HashMap<String, String> hashMap2 = new HashMap<>();
    HashMap<String, Boolean> notFoundMovies = new HashMap();
    HashMap<String, String> foundStars = new HashMap();
    HashMap<String, Boolean> notFoundStars = new HashMap();

    HashMap<String, Boolean> moviesWithStars = new HashMap();

    private String insertStarSQL = "INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (?, ?);";
    private PreparedStatement preparedStatement = null;

    private Connection connection;

    BufferedWriter movieNotFound = null;
    BufferedWriter starNotFound = null;
    BufferedWriter movieEmpty = null;

    int starInMovieInsertedCount = 0;
    int movieNotFoundCount = 0;
    int starNotFoundCount = 0;

    int noStarsInMovieCount = 0;

    int counter = 0;

    public CastHandler(Connection connection) {
        this.connection = connection;

        try {
            this.connection.setAutoCommit(false);

            String query = "SELECT *\nFROM movies";

            PreparedStatement statement = this.connection.prepareStatement(query);

            statement.execute();

            ResultSet rs = statement.getResultSet();

            while (rs.next()) {
                hashMap.put(rs.getString("title"), rs.getString("id"));
                hashMap2.put(rs.getString("id"), rs.getString("title"));
                moviesWithStars.put(rs.getString("id"), false);
            }

            String query2 = "SELECT *\nFROM stars";

            PreparedStatement statement2 = this.connection.prepareStatement(query2);

            statement2.execute();

            ResultSet rs2 = statement2.getResultSet();

            while (rs2.next()) {
                foundStars.put(rs2.getString("name"), rs2.getString("id"));
            }

            String query3 = "SELECT *\nFROM stars_in_movies";

            PreparedStatement statement3 = this.connection.prepareStatement(query3);

            statement3.execute();

            ResultSet rs3 = statement3.getResultSet();

            while (rs3.next()) {
                if (moviesWithStars.get(rs3.getString("movieId")) != null) {
                    moviesWithStars.put(rs3.getString("movieId"), true);
                }
            }

            movieNotFound = new BufferedWriter(new FileWriter("movieNotFound.txt"));
            starNotFound = new BufferedWriter(new FileWriter("starNotFound.txt"));
            movieEmpty = new BufferedWriter(new FileWriter("movieEmpty.txt"));

            preparedStatement = this.connection.prepareStatement(insertStarSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        parseDocument();
    }

    public void close() {
        try {
            preparedStatement.executeBatch();

            starNotFound.flush();
            movieNotFound.flush();

            connection.commit();

            String deleteSQL = "DELETE\nFROM genres_in_movies\nWHERE movieId = ?";
            String deleteGenreInMovie = "DELETE\nFROM genre_in_movies gim\nWHERE gim.movieId = ?;";

            PreparedStatement deleteStatement = this.connection.prepareStatement(deleteSQL);
            PreparedStatement deleteGenreInMovieStatement = this.connection.prepareStatement(deleteGenreInMovie);

            moviesWithStars.forEach((key, value) -> {
                try {
                    if (value == false) {
                        movieEmpty.write(hashMap2.get(key));
                        movieEmpty.newLine();
                        noStarsInMovieCount += 1;

                        deleteGenreInMovieStatement.setString(1, key);
                        deleteGenreInMovieStatement.addBatch();

                        deleteStatement.setString(1, key);
                        deleteStatement.addBatch();

                        counter += 2;
                    }

                    if (counter > 500) {
                        deleteStatement.executeBatch();
                        deleteGenreInMovieStatement.executeBatch();

                        connection.commit();
                        counter = 0;
                    }
                } catch (Exception e) {

                }
            });

            deleteGenreInMovieStatement.executeBatch();
            deleteStatement.executeBatch();

            movieEmpty.flush();
            connection.commit();

            starNotFound.close();
            movieNotFound.close();
            movieEmpty.close();
            preparedStatement.close();
            this.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            javax.xml.parsers.SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("t")) {
            bTitle = true;
        } else if (qName.equalsIgnoreCase("a")) {
            bActor = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (bTitle) {
            movieTitle = new String(ch, start, length);
            bTitle = false;
        } else if (bActor) {
            try {
                String starName = new String(ch, start, length);

                if (foundStars.get(starName) == null && notFoundStars.get(starName) == null) {
                    starNotFound.write(starName);
                    starNotFound.newLine();

                    notFoundStars.put(starName, true);

                    starNotFoundCount += 1;
                }
                else if (hashMap.get(movieTitle) == null && notFoundMovies.get(movieTitle) == null) {
                    notFoundMovies.put(movieTitle, true);

                    movieNotFound.write(movieTitle);
                    movieNotFound.newLine();

                    movieNotFoundCount += 1;
                } else if (hashMap.get(movieTitle) != null && foundStars.get(starName) != null) {
                    preparedStatement.setString(1, foundStars.get(starName));
                    preparedStatement.setString(2, hashMap.get(movieTitle));

                    preparedStatement.addBatch();

                    if (moviesWithStars.get(hashMap.get(movieTitle)) != null) {
                        moviesWithStars.put(hashMap.get(movieTitle), true);
                    }

                    starInMovieInsertedCount += 1;

                    counter += 1;
                }

                if (counter > 2000) {
                    preparedStatement.executeBatch();
                    connection.commit();
                    counter = 0;
                }

                bActor = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
