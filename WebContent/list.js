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
function handleResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    console.log(resultData.length);
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries

    if (document.getElementById("quantity_option").value > resultData.length) {
        document.getElementById("page-up").disabled = true;
    }

    console.log(resultData);

    for (let i = 0; i < Math.min(document.getElementById("quantity_option").value, resultData.length); i++) {
        const genresArray = resultData[i]["movie_genres"].split(",");
        const starsArray = resultData[i]["movie_stars"].split(",");

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

        for (let j = 0; j < Math.min(3, genresArray.length); j++) {
            const split = genresArray[j].split(":");

            rowHTML += "<a class=roundButton href=\"list.html?genre=" + split[1] + "&quantity=25&sort=0&page=1" + "\">" + split[0] + "</a>";
        }

        rowHTML += "</th>";
        rowHTML += "<th>";

        for (let j = 0; j < Math.min(3, starsArray.length); j++) {
            const split = starsArray[j].split(":");

            rowHTML += "<a class=roundButton href=\"single-star.html?id=" + split[1] + "\">" + split[0] + "</a>";
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

function submitSearch() {
    const searchTitle = encodeURIComponent(document.getElementById('search-title').value);
    const searchYear = encodeURIComponent(document.getElementById('search-year').value);
    const searchDirector = encodeURIComponent(document.getElementById('search-director').value);
    const searchStar = encodeURIComponent(document.getElementById('search-star').value);

    const quantity = document.getElementById('quantity_option').value;
    const sort = document.getElementById('sort_option').value;

    $.ajax("api/submit-search", {
        method: "POST",
        data: "title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&quantity=" + quantity + "&sort=" + sort + "&page=" + 1,
        success: function() {
            window.location.href = "list.html?title=" + searchTitle + "&year=" + searchYear + "&director=" + searchDirector + "&star=" + searchStar + "&quantity=" + quantity + "&sort=" + sort + "&page=" + 1;
        }
    });
};

function handlePageDown() {
    const page = parseInt(getParameterByName('page')) - 1;

    if (page > 0) {
        // disable the button
        const myButton = document.getElementById('page-down');
        myButton.disabled = true;

        // get the current URL
        const url = new URL(window.location.href);

        // update the "page" parameter with the new page number
        url.searchParams.set('page', page);

        // update the URL in the browser without reloading the page
        const newUrl = url.toString();
        window.history.pushState({path: newUrl}, '', newUrl);

        // change the page to the new URL
        window.location.replace(newUrl);

        // re-enable the button after a delay
        setTimeout(() => {
            myButton.disabled = false;
        }, 500);
    }
    else {
        document.getElementById('page-down').disabled = true;
    }
}

function handlePageUp() {
    // disable the button
    const myButton = document.getElementById('page-up');
    myButton.disabled = true;

    const page = parseInt(getParameterByName('page')) + 1;

    // get the current URL
    const url = new URL(window.location.href);

    // update the "page" parameter with the new page number
    url.searchParams.set('page', page);

    // update the URL in the browser without reloading the page
    const newUrl = url.toString();
    window.history.pushState({path: newUrl}, '', newUrl);

    // change the page to the new URL
    window.location.replace(newUrl);

    // re-enable the button after a delay
    setTimeout(() => {
        myButton.disabled = false;
        document.getElementById('page-down').disabled = false;
    }, 500);
}

// Get id from URL
let genreId = decodeURIComponent(getParameterByName('genre'));
let prefix = decodeURIComponent(getParameterByName('prefix'));

let title = decodeURIComponent(getParameterByName('title'));
let year = decodeURIComponent(getParameterByName('year'));
let director = decodeURIComponent(getParameterByName('director'));
let star = decodeURIComponent(getParameterByName('star'));

let quantity = decodeURIComponent(getParameterByName('quantity'));
let sort = decodeURIComponent(getParameterByName('sort'));
let page = parseInt(getParameterByName('page'));

let quantityDict = {"10": 0, "25": 1, "50": 2, "100": 3};

document.getElementById("quantity_option").selectedIndex = quantityDict[quantity];
document.getElementById("sort_option").selectedIndex = parseInt(sort);

document.getElementById("quantity_option").addEventListener("change", function() {
    const quantity = document.getElementById('quantity_option').value;

    // get the current URL
    const url = new URL(window.location.href);

    // update the "page" parameter with the new page number
    url.searchParams.set('quantity', quantity);
    url.searchParams.set('page', "1");

    // update the URL in the browser without reloading the page
    const newUrl = url.toString();
    window.history.pushState({path: newUrl}, '', newUrl);

    // change the page to the new URL
    window.location.replace(newUrl);
})

document.getElementById("sort_option").addEventListener("change", function() {
    const sort = document.getElementById('sort_option').value;

    // get the current URL
    const url = new URL(window.location.href);

    // update the "page" parameter with the new page number
    url.searchParams.set('sort', sort);

    // update the URL in the browser without reloading the page
    const newUrl = url.toString();
    window.history.pushState({path: newUrl}, '', newUrl);

    // change the page to the new URL
    window.location.replace(newUrl);
})

if (page <= 1) {
    document.getElementById('page-down').disabled = true;
}

$.ajax("api/submit-search", {
    method: "POST",
    data: "prefix=" + prefix + "&genre=" + genreId + "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star + '&quantity=' + quantity + '&sort=' + sort + '&page=' + page,
});

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/list?prefix=" + prefix + "&genre=" + genreId + "&title=" + title + "&year=" + year + "&director=" + director + "&star=" + star + '&quantity=' + quantity + '&sort=' + sort + '&page=' + page, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});