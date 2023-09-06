function addGenre() {
    let genreName = document.getElementById("genre-name").value;

    $.ajax("../api/addgenre", {
        method: "GET",
        data: "genre-name=" + genreName,
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