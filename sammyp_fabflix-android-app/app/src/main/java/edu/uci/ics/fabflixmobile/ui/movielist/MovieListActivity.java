package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import edu.uci.ics.fabflixmobile.ui.home.MovieSearchActivity;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {

    private final String host = "10.0.2.2";
    private final String port = "8080";
    private final String domain = "cs122b_project1_war";
    private final String baseURL = "http://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movielist);

        Intent intent = getIntent();

        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        TextView pageNum = findViewById(R.id.pagenumber);
        Button nextButton = findViewById(R.id.nextpage);
        Button backButton = findViewById(R.id.backpage);
        Button homeButton = findViewById(R.id.searchhome);

        homeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent SearchPage = new Intent(MovieListActivity.this, MovieSearchActivity.class);
                // activate the list page.
                startActivity(SearchPage);
            }
        });

        pageNum.setText(intent.getStringExtra("page"));

        final StringRequest listRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/list" + "?title=" + intent.getStringExtra("title") + "&year=&director=&star=&quantity=10&sort=0&page=" + intent.getStringExtra("page"),
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("list.success", response);

                    final ArrayList<Movie> movies = new ArrayList<>();

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        if (jsonArray.length() == 10) {
                            nextButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent listPage = new Intent(MovieListActivity.this, MovieListActivity.class);

                                    listPage.putExtra("title", intent.getStringExtra("title"));
                                    listPage.putExtra("page", Integer.toString(Integer.parseInt(intent.getStringExtra("page")) + 1));

                                    startActivity(listPage);
                                }
                            });
                        } else {
                            nextButton.setEnabled(false);
                        }

                        if (Integer.parseInt(intent.getStringExtra("page")) <= 1) {
                            backButton.setEnabled(false);
                        } else {
                            backButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent listPage = new Intent(MovieListActivity.this, MovieListActivity.class);

                                    listPage.putExtra("title", intent.getStringExtra("title"));
                                    listPage.putExtra("page", Integer.toString(Integer.parseInt(intent.getStringExtra("page")) - 1));

                                    startActivity(listPage);
                                }
                            });
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject movie = (JSONObject) jsonArray.get(i);

                            String movie_id = (String) movie.get("movie_id");
                            String movie_title = (String) movie.get("movie_title");
                            Integer movie_year = Integer.parseInt((String) movie.get("movie_year"));
                            String movie_director = (String) movie.get("movie_director");

                            String[] genreList = movie.get("movie_genres").toString().split(",");
                            String movie_genres = "";

                            for (int j = 0; j < genreList.length; j++) {
                                movie_genres += genreList[j].split(":")[0] + ", ";
                            }

                            movie_genres = movie_genres.substring(0, movie_genres.length() - 2);

                            String[] starList = movie.get("movie_stars").toString().split(",");
                            String movie_stars = "";

                            for (int j = 0; j < starList.length; j++) {
                                movie_stars += starList[j].split(":")[0] + ", ";
                            }

                            movie_stars = movie_stars.substring(0, movie_stars.length() - 2);

                            movies.add(new Movie(movie_id, movie_title, movie_year, movie_director, movie_genres, movie_stars));
                        }

                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            Intent singleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);

                            singleMoviePage.putExtra("title", movie.getName() + " (" + movie.getYear() + ")");
                            singleMoviePage.putExtra("director", movie.getDirector());
                            singleMoviePage.putExtra("genres", movie.getFullGenres());
                            singleMoviePage.putExtra("stars", movie.getFullStars());

                            startActivity(singleMoviePage);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                },
                error -> {
                    System.out.println(2);
                    // error
                    Log.d("list.error", error.toString());
                });

        queue.add(listRequest);
    }
}