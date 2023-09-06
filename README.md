# s23-122b-sammyps

UCI ID: 81358885

UCI USER: sammyp

UCI EMAIL: sammyp@uci.edu

Demo Link (Project 1)
https://youtu.be/M7L7b2_xsKk

Demo Link (Project 2)
https://youtu.be/IvYOFngrpVc

Demo Link (Project 3)
None

Demo Link (Project 4)
https://youtu.be/YHolwReA-r0

Demo Link (Project 5)
https://youtu.be/Ax9aDvXAkxM


# Project 5
- # General
    - #### Team#: sammyps
    
    - #### Names: Sammy Pham
    
    - #### Project 5 Video Demo Link: https://youtu.be/Ax9aDvXAkxM

    - #### Instruction of deployment: N/A

    - #### Collaborations and Work Distribution: Solo


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    	- src/AddGenreServlet
    	- src/AddMoviesServlet
    	- src/AddStarServlet
    	- src/BrowseServlet
    	- src/GenreBrowseServlet
    	- src/LoginServlet
    	- src/MetadataServlet
    	- src/MoviesServlet
    	- src/PaymentServlet
    	- src/SearchSuggestion
    	- src/SingleMovieServlet
    	- src/SingleStarServlet
    	- src/StarsServlet
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    	- Connection pools are an optimization technique aimed to improve the performance and efficiency of database-driven applications. In the Fabflix project, connection pooling creates a pool of pre-initialized database connections to our moviedb database that can be reused by multiple clients. The purpose of this is that there are performance and resource improvements since the creation of a new database connection for each client request is a slow and resource intensive task. Connection pooling also guarantees that the number of concurrent database connections remain within the defined limits of the server, thus protects the server from overworking.
    	- CONNECTION POOL STEPS:
    	- Connection pooling works by first pre-initializing database connections to the backend, in our Fabflix project it is set to 100 connections total and 30 connections idle for two databases, moviedbReadWrite and moviedbReadOnly.
    	- Next, for every client request the server will take an open connection from the data pool choosing moviedbReadWrite or moviedbReadOnly depending on use case and handle the request for the client.
    	- Once the task is complete the connection is placed back in the pool to be reused by other client requests.
    
    - #### Explain how Connection Pooling works with two backend SQL.
    	- Connection Pool Creation: A connection pool is created for each database instance separately. So, you would have one connection pool for the master database and another connection pool for the slave database. Each connection pool maintains a pool of pre-initialized connections specific to its respective database.
    	- Connection Assignment: When a client requests a database connection, the connection pooling mechanism determines which database (master or slave) to assign the connection from. This decision can be based on various factors such as the nature of the operation, load balancing.
    	- Read Operations: For read operations that don't modify the data, the connection pooling mechanism can assign a connection from the connection pool associated with the slave database. This helps distribute the read workload across the slave database, offloading the master database and improving performance.
    	- Write Operations: Write operations that modify the data typically require synchronization and consistency, which is usually handled by the master database. Connection pooling can be configured to assign connections from the pool associated with the master database for such operations. This ensures that all write operations go through the master, maintaining data integrity.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    	- src/AddGenreServlet
    	- src/AddMoviesServlet
    	- src/AddStarServlet
    	- src/BrowseServlet
    	- src/GenreBrowseServlet
    	- src/LoginServlet
    	- src/MetadataServlet
    	- src/MoviesServlet
    	- src/PaymentServlet
    	- src/SearchSuggestion
    	- src/SingleMovieServlet
    	- src/SingleStarServlet
    	- src/StarsServlet
    	- WebContent/META-INF/context.xml
    	- WebContent/WEB-INF/web.xml

    - #### How read/write requests were routed to Master/Slave SQL?
    	- Two resources were defined moviedbReadWrite and moviedbReadOnly in context.xml and web.xml. Any servlets that handled statements with INSERT within them would connect to the moviedbReadWrite i.e. master instance, while any servlets that only read from the database could use either database that was available i.e. localhost.
    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    - log_processing.py is located in the /logs/ directory. To use run "python log_processing.py file1.txt file2.txt". The input of file2.txt is optional but essentially when added log_processing.py combines both text files and takes the average TS and TJ of the combined files.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | 139                         | 1.986794                                  | 0.110534                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | 135                         | 2.075173                                  | 0.098428                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | 236                         | 2.029573                                  | 0.185707                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | 167                         | 8.654944                                  | 6.742054                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | 140                         | 2.181897                                  |  0.108825                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | 136                         | 2.560860                                  | 0.071956                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | 1146                         | 933.242                                  | 364.385122                        | ??           |

# Project 4

## (Project 4 Autosuggestion Query)
```
String sqlQuery = "SELECT *\nFROM movies as m\nWHERE MATCH(m.title) AGAINST(? IN BOOLEAN MODE)\nLIMIT 10;";

PreparedStatement statement = conn.prepareStatement(sqlQuery);

statement.setString(1, search_query.toString().trim());

System.out.println(statement);

ResultSet rs = statement.executeQuery();
```

# Project 3

## (Project 3 Files w/ Prepared Statements)
src/AddGenreServlet\
src/AddMoviesServlet\
src/BrowseServlet\
src/GenreBrowseServlet\
src/MetadataServlet\
src/MoviesServlet\
src/PaymentServlet\
src/SingleMovieServlet\
src/SingleStarServlet\
src/StarsServlet\

## (Project 3 Two Parsing Optimizations)
1. Use of batch inserts
```
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
...
if (counter > 500) {
    preparedStatement.executeBatch();

    connection.commit();
    counter = 0;
}
```

3. Use of hashmaps to store data in java
```
HashMap<String, Boolean> hashMap = new HashMap<>();
...
String query = "SELECT *\nFROM stars";

PreparedStatement statement = this.connection.prepareStatement(query);

statement.execute();

ResultSet rs = statement.getResultSet();

while (rs.next()) {
hashMap.put(rs.getString("name") + rs.getString("birthYear"), true);
}
...
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
```

## (Project 3 Inconsistent Data Reports)
xmlParser/movieDuplicates.txt\
xmlParser/movieEmpty.txt\
xmlParser/movieInconsistent.txt\
xmlParser/movieNotFound.txt\
xmlParser/starDuplicate.txt\
xmlParser/starNotFound.txt\

# Project 2

## (Project 2 Substring Search Design)

The following SQL Query searches for title: '%love%', year: 2005, director: '%jon%', star: '%ian%'

It returns a table with the following attributes 'id', 'title', 'year', 'director', 'rating', 'genres', 'stars'

'genres' => [genre.name:genre.id, ...],
'stars' => [star.name:star.id, ...]

```sql
SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating,
	(SELECT GROUP_CONCAT(genre_name, ':', genre_id ORDER BY genre_name)
	FROM (
		SELECT g.genreName AS genre_name, g.id AS genre_id
		FROM genres g
		JOIN genres_in_movies gim ON g.id = gim.genreId
		WHERE gim.movieId = m.id) AS sub) AS genres,
	(SELECT GROUP_CONCAT(star_name, ':', star_id)
	FROM (
		SELECT sub.star_name, sub.star_id, COUNT(sub.star_id) AS movieCount
		FROM(
			SELECT s.name AS star_name, s.id AS star_id
			FROM stars s
			JOIN stars_in_movies sim ON s.id = sim.starId
			WHERE sim.movieId = m.id) AS sub
			LEFT JOIN stars_in_movies ON sub.star_id = stars_in_movies.starId
			GROUP BY sub.star_id
			ORDER BY movieCount DESC) AS sub2) AS stars
FROM movies m
JOIN genres_in_movies gim ON m.id = gim.movieId
LEFT JOIN ratings r ON r.movieId = m.id
WHERE m.title LIKE '%love%' AND m.year = 2005 AND m.director LIKE '%Jon%'
	AND EXISTS (
		SELECT *
		FROM stars s
		JOIN stars_in_movies sim ON s.id = sim.starId
		WHERE sim.movieId = m.id AND s.name LIKE '%Ian%'
	  )
ORDER BY r.rating DESC, m.title ASC;
```
Query returns:
```
id: tt0451103
title: Love Lessons
year: 2005
director: Jon Stahl
rating: 8.6
genres: Comedy:6
stars: Ian Dudley:nm0240129,Todd Zelin:nm1564891, ..., Eric Milano:1497128
```
