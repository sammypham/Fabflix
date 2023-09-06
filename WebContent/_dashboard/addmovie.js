function addMovie() {
    let movieTitle = document.getElementById("movie-title").value;
    let movieYear = document.getElementById("movie-year").value;
    let movieDirector = document.getElementById("movie-director").value;
    let starName = document.getElementById("star-name").value;
    let starBirthYear = document.getElementById("star-birthYear").value;
    let genreName = document.getElementById("genre-name").value;

    $.ajax("../api/addmovie", {
        method: "GET",
        data: "movie-title=" + movieTitle + "&movie-year=" + movieYear + "&movie-director=" + movieDirector + "&star-name=" + starName + "&star-birthYear=" + starBirthYear + "&genre-name=" + genreName,
        success: function(data, textStatus, jqXHR) {
            let resultDataJson = JSON.parse(jqXHR.responseText);

            alert(resultDataJson["successMessage"]);
        },
        error: function(jqXHR, textStatus, errorThrown) {
            let resultDataJson = JSON.parse(jqXHR.responseText);

            alert("ERROR: " + resultDataJson["errorMessage"]);
        }
    });
}