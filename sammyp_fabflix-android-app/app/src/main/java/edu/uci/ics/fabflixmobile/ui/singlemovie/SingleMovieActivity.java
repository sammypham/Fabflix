package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.home.MovieSearchActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SingleMovieActivity extends AppCompatActivity {

    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "cs122b_project1_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlemovie);

        Intent intent = getIntent();

        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        TextView movieTitle = findViewById(R.id.single_movie_title);
        TextView movieDirector = findViewById(R.id.single_movie_director);
        TextView movieGenres = findViewById(R.id.single_movie_genres);
        TextView movieStars = findViewById(R.id.single_movie_stars);

        movieTitle.setText(intent.getStringExtra("title"));
        movieDirector.setText("Director: " + intent.getStringExtra("director"));
        movieGenres.setText("Genres: " + intent.getStringExtra("genres"));
        movieStars.setText("Stars: " + intent.getStringExtra("stars"));
    }
}