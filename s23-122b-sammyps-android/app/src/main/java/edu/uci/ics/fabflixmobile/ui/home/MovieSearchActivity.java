package edu.uci.ics.fabflixmobile.ui.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.movielist.MovieListActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

public class MovieSearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        Button searchButton = findViewById(R.id.search);

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent listPage = new Intent(MovieSearchActivity.this, MovieListActivity.class);

                TextView movieTitleObj = findViewById(R.id.movietitle);

                String searchQuery = movieTitleObj.getText().toString();
                listPage.putExtra("title", searchQuery);
                listPage.putExtra("page", "1");

                startActivity(listPage);
            }
        });


    }
}