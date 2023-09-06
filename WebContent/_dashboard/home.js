function handleGenresResult(resultData) {
    console.log(resultData);

    let metadata_container = jQuery("#metadata-container");

    for (let i = 0; i < resultData.length; i++) {
        let htmlInsert = ""

        const columnArray = resultData[i]["column_info"].split(",");

        htmlInsert += "<h2>" + resultData[i]["table_name"] + "</h2>";
        htmlInsert += "<table class=\"table table-striped\" style=\"width: 500px;\">";
        htmlInsert += "<thead>";
        htmlInsert += "<th class='top'>Attribute</th>";
        htmlInsert += "<th class='top'>Type</th>";
        htmlInsert += "</thead>";
        htmlInsert += "<tbody>";

        for (let j = 0; j < columnArray.length; j++) {
            const dataSplit = columnArray[j].split(":");
            htmlInsert += "<tr>";
            htmlInsert += "<th>" + dataSplit[0] + "</th>";
            htmlInsert += "<th>" + dataSplit[1] + "</th>";
            htmlInsert += "</tr>";
        }

        htmlInsert += "</tbody>";
        htmlInsert += "</tr>";
        htmlInsert += "</table>";

        metadata_container.append(htmlInsert);
    }
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "../api/metadata", // Setting request url
    success: (resultData) => handleGenresResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
