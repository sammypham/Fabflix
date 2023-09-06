DELIMITER //

CREATE PROCEDURE add_movie(
  IN movie_title VARCHAR(100),
  IN movie_year INT,
  IN movie_director VARCHAR(100),
  IN star_name VARCHAR(100),
  IN star_birthYear INT,
  IN genre_name VARCHAR(32)
)
BEGIN
	DECLARE movie_id VARCHAR(100);
    DECLARE star_id VARCHAR(100);
    DECLARE genre_id INT;
    DECLARE existing_title VARCHAR(100);
    DECLARE new_star_id VARCHAR(10);
    DECLARE new_genre_id INT;
    DECLARE new_movie_id VARCHAR(10);

    -- Check if the movie already exists in the database
SELECT title into existing_title FROM movies WHERE title = movie_title AND year = movie_year and director = movie_director LIMIT 1;

-- If movie already exists exit
IF existing_title IS NOT NULL THEN
SELECT id into movie_id FROM movies WHERE title = movie_title AND year = movie_year and director = movie_director LIMIT 1;

SELECT CONCAT('Duplicate movie.\nID: ', movie_id) AS message;
ELSE
		-- Check if the star already exists in the database
SELECT s.id INTO star_id
FROM stars AS s
WHERE s.name = star_name LIMIT 1;

SELECT CONCAT("nm", CAST(SUBSTRING(MAX(id), 3) AS SIGNED) + 1) INTO new_star_id
FROM stars;

-- If the star does not exist, insert it into the database
IF star_id IS NULL THEN
			INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, star_name, star_birthYear);
			SET star_id = new_star_id;
END IF;

		-- Check if the genre already exists in the database
SELECT g.id INTO genre_id
FROM genres AS g
WHERE g.genreName = genre_name LIMIT 1;

SELECT MAX(g.id) + 1 INTO new_genre_id
FROM genres AS g;

-- If the genre does not exist, insert it into the database
IF genre_id IS NULL THEN
			INSERT INTO genres (id, genreName) VALUES (new_genre_id, genre_name);
			SET genre_id = new_genre_id;
END IF;

SELECT CONCAT(LEFT(MAX(id), 2), LPAD(CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) + 1, LENGTH(MAX(id)) - 2, '0')) INTO new_movie_id
FROM movies;

-- Insert the movie into the database
INSERT INTO movies (id, title, year, director) VALUES (new_movie_id, movie_title, movie_year, movie_director);

-- Associate the star with the movie
INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, new_movie_id);

-- Associate the genre with the movie
INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, new_movie_id);

-- Output success message
SELECT CONCAT('Movie added successfully.\nID: ', new_movie_id, '\nSTAR ID: ', star_id, '\nGENRE ID: ', genre_id) AS message;
END IF;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_star(
  IN star_name VARCHAR(100),
  IN star_birthYear INT
)
BEGIN
    DECLARE star_id VARCHAR(100);
    DECLARE new_star_id VARCHAR(10);

SELECT CONCAT("nm", CAST(SUBSTRING(MAX(id), 3) AS SIGNED) + 1) INTO new_star_id
FROM stars;

INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, star_name, star_birthYear);

-- Output success message
SELECT CONCAT('Star added successfully.\nID: ', new_star_id) AS message;
END //

DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_genre(
  IN genre_name VARCHAR(32)
)
BEGIN
    DECLARE genre_id INT;
    DECLARE new_genre_id INT;
    
	-- Check if the genre already exists in the database
SELECT g.id INTO genre_id
FROM genres AS g
WHERE g.genreName = genre_name LIMIT 1;

-- If the genre does not exist, insert it into the database
IF genre_id IS NOT NULL THEN
SELECT CONCAT('Duplicate genre.\nID: ', genre_id) AS message;
END IF;

SELECT MAX(g.id) + 1 INTO new_genre_id
FROM genres AS g;

INSERT INTO genres (id, genreName) VALUES (new_genre_id, genre_name);

-- Output success message
SELECT CONCAT('Genre added successfully.\nID: ', new_genre_id) AS message;
END //

DELIMITER ;