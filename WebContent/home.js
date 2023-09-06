function handleGenresResult(resultData) {
    console.log("handleGenresResult: populating genres table from resultData");

    let movieGenreBodyElement = jQuery("#movie_genres_body");
    let rowHTML = "";
    rowHTML += "<tr><td>";

    for (let i = 0; i < resultData.length; i++) {
        let genreId = resultData[i]["movie_id"];
        let genreName = resultData[i]["movie_genre"];

        rowHTML += "<a class=roundButton href=\"list.html?genre=" + genreId + "&quantity=25&sort=0&page=1" + "\">" + genreName + "</a>";
    }

    rowHTML += "</td></tr>";

    movieGenreBodyElement.append(rowHTML);
}

function submitSearch() {
    const searchTitle = encodeURIComponent(document.getElementById('search-title').value);
    const searchYear = encodeURIComponent(document.getElementById('search-year').value);
    const searchDirector = encodeURIComponent(document.getElementById('search-director').value);
    const searchStar = encodeURIComponent(document.getElementById('search-star').value);

    window.location.href = "list.html?title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&quantity=" + 25 + "&sort=" + 0 + "&page=" + 1;
};


// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse", // Setting request url
    success: (resultData) => handleGenresResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});