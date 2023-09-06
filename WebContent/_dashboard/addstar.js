function addStar() {
    let starName = document.getElementById("star-name").value;
    let starBirthYear = document.getElementById("star-birthYear").value;

    $.ajax("../api/addstar", {
        method: "GET",
        data: "star-name=" + starName + "&star-birthYear=" + starBirthYear,
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