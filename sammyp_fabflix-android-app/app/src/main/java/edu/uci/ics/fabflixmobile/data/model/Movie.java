package edu.uci.ics.fabflixmobile.data.model;


import java.util.Arrays;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final Integer year;
    private final String director;
    private final String genres;
    private final String stars;
    private final String fullGenres;
    private final String fullStars;

    public Movie(String id, String name, Integer year, String director, String genres, String stars) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;

        String threeGenres = "";
        String threeStars = "";

        String[] genreArray = genres.split(",");
        String[] starsArray = stars.split(",");

        this.fullGenres = genres;
        this.fullStars = stars;

        for (int i = 0; i < Math.min(3, genreArray.length); i++) {
            threeGenres += genreArray[i] + ", ";
        }

        threeGenres = threeGenres.substring(0, threeGenres.length() - 2);

        for (int i = 0; i < Math.min(3, starsArray.length); i++) {
            threeStars += starsArray[i] + ", ";
        }

        threeStars = threeStars.substring(0, threeStars.length() - 2);

        this.genres = threeGenres;
        this.stars = threeStars;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getYear() { return year; }

    public String getDirector() {
        return director;
    }

    public String getGenres() {
        return genres;
    }

    public String getStars() {
        return stars;
    }

    public String getFullGenres() {
        return fullGenres;
    }

    public String getFullStars() {
        return fullStars;
    }
}