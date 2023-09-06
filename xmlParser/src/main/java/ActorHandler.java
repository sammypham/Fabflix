import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;

public class ActorHandler extends DefaultHandler {
    boolean bStagename = false;
    boolean bDob = false;
    String stagename = "";
    private Connection connection;

    private String insertTableSQL = "INSERT IGNORE INTO stars (id, name, birthYear)\nSELECT CONCAT(\'nm\', CAST(SUBSTRING(MAX(id), 3) AS SIGNED) + 1), ?, ?\nFROM stars;";
    private PreparedStatement preparedStatement = null;

    int[] batchResult = null;

    HashMap<String, Boolean> hashMap = new HashMap<>();
    HashMap<String, String> foundStars = new HashMap();

    BufferedWriter starDuplicate = null;

    int starInsertCount = 0;
    int starDuplicateCount = 0;

    int counter = 0;

    public ActorHandler(Connection connection) {

        this.connection = connection;

        try {
            this.connection.setAutoCommit(false);

            String query = "SELECT *\nFROM stars";

            PreparedStatement statement = this.connection.prepareStatement(query);

            statement.execute();

            ResultSet rs = statement.getResultSet();

            while (rs.next()) {
                hashMap.put(rs.getString("name") + rs.getString("birthYear"), true);
            }

            starDuplicate = new BufferedWriter(new FileWriter("starDuplicate.txt"));

            preparedStatement = this.connection.prepareStatement(insertTableSQL);
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

            connection.commit();

            starDuplicate.flush();

            starDuplicate.close();
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
            sp.parse("actors63.xml", this);

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
        if (qName.equalsIgnoreCase("stagename")) {
            bStagename = true;
        } else if (qName.equalsIgnoreCase("dob")) {
            bDob = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (bStagename) {
            stagename = new String(ch, start, length);
            bStagename = false;
        } else if (bDob) {
            try {
                String dob = new String(ch, start, length);

                if(dob.isBlank() || dob.equals("n.a.")) {
                    dob = "NULL";
                    preparedStatement.setNull(2, Types.INTEGER);
                } else {
                    preparedStatement.setInt(2, Integer.parseInt(dob));
                }

                if (hashMap.get(stagename + dob) != null) {
                    starDuplicate.write("name: " + stagename + ", dob: " + dob);
                    starDuplicate.newLine();
                    starDuplicateCount += 1;
                } else {
                    preparedStatement.setString(1, stagename);

                    preparedStatement.addBatch();
                    hashMap.put(stagename + dob, true);
                    starInsertCount += 1;
                    counter += 1;
                }

                if (counter > 500) {
                    preparedStatement.executeBatch();

                    connection.commit();
                    counter = 0;
                }

                bDob = false;
            } catch (Exception e) {
                String dob = new String(ch, start, length);

//                System.out.println("ERROR ENTRY name: " + stagename + ", birthYear: " + dob);
            }
        }
    }
}
