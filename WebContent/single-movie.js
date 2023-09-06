/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");

    console.log(resultData);

    movieInfoElement.append("<p>" + resultData[0]["movie_title"]+ "</p>");

    // append two html <p> created to the h3 body, which will refresh the page
    console.log(resultData[0]);

    let genresArray = resultData[0]["movie_genres"].split(",");
    let starsArray = resultData[0]["movie_stars"].split(",");

    let movieTableBodyElement = jQuery("#movie_table_body");

    let htmlInsert = ""

    htmlInsert += "<tr>";
    htmlInsert += "<th>" + resultData[0]["movie_director"] + "</th>";
    htmlInsert += "<th>";

    for (let i = 0; i < genresArray.length; i++) {
        let genreArraySplit = genresArray[i].split(":");
        htmlInsert += "<a class=roundButton href=\"list.html?genre=" + genreArraySplit[1] + "&quantity=25&sort=0&page=1" + "\">" + genreArraySplit[0] + "</a>"
    }

    htmlInsert += "</th>";
    htmlInsert += "<th>";

    for (let i = 0; i < starsArray.length; i++) {
        let starsArraySplit = starsArray[i].split(":");
        htmlInsert += "<a class=roundButton href=\"single-star.html?id=" + starsArraySplit[1] + "\">" + starsArraySplit[0] + "</a>";
    }

    htmlInsert += "</th>";
    htmlInsert += "<th>" + resultData[0]["movie_rating"] + "</th>";
    htmlInsert += "<th><button class=roundButton id=add-to-cart onclick=handleAddToCart(this) data-movie-id=\'" + resultData[0]['movie_id'] + "\' data-movie-title=\'" + resultData[0]['movie_title'] + "'\'>Add</button></th>";
    htmlInsert += "</tr>";

    movieTableBodyElement.append(htmlInsert);

    console.log("handleResult: populating movie table from resultData");
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
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});