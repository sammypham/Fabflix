import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class MainHandler extends DefaultHandler {
    boolean bFid = false;
    boolean bDirn = false;
    boolean bTitle = false;
    boolean bYear = false;
    boolean bCat = false;

    String movieId = "";
    String directorName = "";
    String movieTitle = "";
    String movieYear = "";
    List<String> genres = new ArrayList<>();

    private Connection connection;

    private String insertTableSQL = "INSERT IGNORE INTO movies (id, title, year, director) VALUES (?, ?, ?, ?);";
    private String insertGenreInMovieSQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (?, ?);";
    private String insertGenreSQL = "INSERT IGNORE INTO genres (id, genreName)\nSELECT max(id) + 1, ?\nFROM genres;";

    private PreparedStatement preparedStatement = null;
    private PreparedStatement preparedGIMStatement = null;
    private PreparedStatement preparedGenreStatement = null;

    int[] batchResult = null;

    HashMap<String, String> hashMap = new HashMap<>();
    public HashMap<String, Boolean> currentlyParsed = new HashMap<>();
    public HashMap<String, Integer> existingGenres = new HashMap<>();

    BufferedWriter movieDuplicates = null;
    BufferedWriter movieInconsistent = null;

    int currentMovieId = 0;

    int movieInsertCount = 0;
    int genreInsertCount = 0;
    int genreInMovieInsertCount = 0;
    int movieDuplicateCount = 0;
    int movieInconsistentCount = 0;
    int counter = 0;

    public MainHandler(Connection connection) {

        this.connection = connection;

        try {
            this.connection.setAutoCommit(false);

            String query = "SELECT *\nFROM movies";

            PreparedStatement statement = this.connection.prepareStatement(query);

            statement.execute();

            ResultSet rs = statement.getResultSet();

            while (rs.next()) {
                hashMap.put(rs.getString("title") + rs.getString("year") + rs.getString("director"), rs.getString("id"));
            }

            String query2 = "SELECT *\nFROM genres";

            PreparedStatement statement2 = this.connection.prepareStatement(query2);

            statement2.execute();

            ResultSet rs2 = statement2.getResultSet();

            while (rs2.next()) {
                existingGenres.put(rs2.getString("genreName"), Integer.parseInt(rs2.getString("id")));
            }

            String query3 = "SELECT max(id) AS maxId\nFROM movies";

            PreparedStatement statement3 = this.connection.prepareStatement(query3);

            statement3.execute();

            ResultSet rs3 = statement3.getResultSet();

            while (rs3.next()) {
                currentMovieId = Integer.parseInt(rs3.getString("maxId").substring(2));
            }

            movieDuplicates = new BufferedWriter(new FileWriter("movieDuplicates.txt"));
            movieInconsistent = new BufferedWriter(new FileWriter("movieInconsistent.txt"));

            preparedStatement = this.connection.prepareStatement(insertTableSQL);
            preparedGIMStatement = this.connection.prepareStatement(insertGenreInMovieSQL);
            preparedGenreStatement = this.connection.prepareStatement(insertGenreSQL);
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
            preparedGenreStatement.executeBatch();
            preparedGIMStatement.executeBatch();

            connection.commit();

            movieDuplicates.flush();
            movieInconsistent.flush();

            movieDuplicates.close();
            movieInconsistent.close();
            preparedStatement.close();
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
            sp.parse("mains243.xml", this);

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
        if (qName.equalsIgnoreCase("fid")) {
            bFid = true;
        } else if (qName.equalsIgnoreCase("dirn")) {
            bDirn = true;
        } else if (qName.equalsIgnoreCase("t")) {
            bTitle = true;
        } else if (qName.equalsIgnoreCase("year")) {
            bYear = true;
        } else if (qName.equalsIgnoreCase("cat")) {
            bCat = true;
        } else if (qName.equalsIgnoreCase("film")) {
            genres.clear();
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (bFid) {
            movieId = new String(ch, start, length);
            bFid = false;
        } else if (bDirn) {
            directorName = new String(ch, start, length);
            bDirn = false;
        } else if (bTitle) {
            movieTitle = new String(ch, start, length);
            bTitle = false;
        } else if (bYear) {
            movieYear = new String(ch, start, length);
            bYear = false;
        } else if (bCat) {
            genres.add(new String(ch, start, length));
            bCat = false;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            try {
                String moviedbID = "tt" + String.format("%07d", currentMovieId + 1);
                preparedStatement.setString(1, "tt" + String.format("%07d", currentMovieId + 1));
                preparedStatement.setString(2, movieTitle);
                preparedStatement.setInt(3, Integer.parseInt(movieYear));
                preparedStatement.setString(4, directorName);

                if (movieTitle.isBlank() || movieYear.isBlank() || directorName.isBlank() || genres.size() == 0) {
                    movieInconsistent.write(movieId + " " + movieTitle + " " + movieYear + " " + directorName + " " + genres);
                    movieInconsistent.newLine();

                    movieInconsistentCount += 1;
                } else if (hashMap.get(movieTitle + movieYear + directorName) != null || currentlyParsed.get(movieId) != null) {
                    movieDuplicates.write(movieId + " " + movieTitle + " " + movieYear + " " + directorName + " " + genres);
                    movieDuplicates.newLine();
                    movieDuplicateCount += 1;

                    if (hashMap.get(movieTitle + movieYear + directorName) != null) {
                        // Find & Add missing genres

                        for (int i = 0; i < genres.size(); i++) {
                            if (existingGenres.get(genres.get(i)) == null) {
                                preparedGenreStatement.setString(1, genres.get(i));
                                preparedGenreStatement.addBatch();
                                genreInsertCount += 1;
                                counter += 1;

                                existingGenres.put(genres.get(i), existingGenres.size() + 1);
                            }
                        }

                        // Find & Add Genres to Movies
                        for (int i = 0; i < genres.size(); i++) {
                            preparedGIMStatement.setInt(1, existingGenres.get(genres.get(i)));
                            preparedGIMStatement.setString(2, hashMap.get((movieTitle + movieYear + directorName)));
                            preparedGIMStatement.addBatch();
                            genreInMovieInsertCount += 1;
                            counter += 1;
                        }
                    }
                } else {
                    // Find & Add missing genres
                    for (int i = 0; i < genres.size(); i++) {
                        if (existingGenres.get(genres.get(i)) == null) {
                            preparedGenreStatement.setString(1, genres.get(i));
                            preparedGenreStatement.addBatch();
                            genreInsertCount += 1;
                            counter += 1;

                            existingGenres.put(genres.get(i), existingGenres.size() + 1);
                        }
                    }

                    // Find & Add Genres to Movies
                    for (int i = 0; i < genres.size(); i++) {
                        preparedGIMStatement.setInt(1, existingGenres.get(genres.get(i)));
                        preparedGIMStatement.setString(2, moviedbID);
                        preparedGIMStatement.addBatch();
                        genreInMovieInsertCount += 1;
                        counter += 1;
                    }

                    preparedStatement.addBatch();
                    movieInsertCount += 1;
                    currentMovieId += 1;
                    counter += 1;
                }

                if (counter > 500) {
                    preparedStatement.executeBatch();
                    preparedGenreStatement.executeBatch();
                    preparedGIMStatement.executeBatch();

                    connection.commit();
                    counter = 0;
                }

                currentlyParsed.put(movieId, true);
            } catch (Exception e) {
                try {
                    movieInconsistent.write(movieId);
                    movieInconsistent.newLine();

                    movieInconsistentCount += 1;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
