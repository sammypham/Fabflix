/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let genres = resultData[i]["movie_genres"].split(",");
        let stars = resultData[i]["movie_stars"].split(",");

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +="<th>" + (i + 1) + "</th>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a class=roundButton href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>";

        for (let j = 0; j < Math.min(3, genres.length); j++) {
            let genresSplit = genres[j].split(":");

            rowHTML += "<a class=roundButton href=\"list.html?genre=" + genresSplit[1] + "\">" + genresSplit[0] + "</a>";
        }

        rowHTML += "</th>";
        rowHTML += "<th>";

        for (let j = 0; j < Math.min(3, stars.length); j++) {
            let starsSplit = stars[j].split(":");

            rowHTML += "<a class=roundButton href=\"single-star.html?id=" + starsSplit[1] + "\">" + starsSplit[0] + "</a>";
        }

        rowHTML += "</th>";

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th><button class=roundButton id=add-to-cart onclick=handleAddToCart(this) data-movie-id=\'" + resultData[i]['movie_id'] + "\' data-movie-title=\'" + resultData[i]['movie_title'] + "'\'>Add</button></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

function handleAddToCart(button) {
    $.ajax("api/index", {
        method: "POST",
        data: "item=" + button.dataset.movieId + "&title=" + button.dataset.movieTitle + "&add=" + 1,
        success: resultDataString => {
            alert(button.dataset.movieTitle + " added to cart.");
        }
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});