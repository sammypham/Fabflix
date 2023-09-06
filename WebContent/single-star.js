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
    let starInfoElement = jQuery("#star_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p>Star Name: " + resultData[0]["star_name"] + "</p>" +
        "<p>Date Of Birth: " + resultData[0]["star_dob"] + "</p>" +
        "<p>Starred In: </p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    const movieIdsArray = resultData[0]["movie_ids"].split(",");
    const moviesArray = resultData[0]["movies"].split("|");
    const movieYearsArray = resultData[0]["movie_years"].split(",");
    const moviesDirectorsArray = resultData[0]["movie_directors"].split(",");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < movieIdsArray.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th><a class=roundButton href=\"single-movie.html?id=" + movieIdsArray[i] + "\">" + moviesArray[i] + "</a></th>";
        rowHTML += "<th>" + movieYearsArray[i] + "</th>";
        rowHTML += "<th>" + moviesDirectorsArray[i] + "</th>";
        rowHTML += "<th><button class=roundButton id=add-to-cart onclick=handleAddToCart(this) data-movie-id=\'" + movieIdsArray[i] + "\' data-movie-title=\'" + moviesArray[i] + "'\'>Add</button></th>";
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
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});