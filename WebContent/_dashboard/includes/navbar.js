fetch('includes/navbar.html')
    .then(response => response.text())
    .then(data => {
        document.querySelector('#navbar').innerHTML = data;

        $.ajax("api/submit-search", {
            method: "GET",
            success: handleSessionData
        });
    })
    .catch(error => {
        console.error('Error fetching navbar:', error);
    });

// Get the anchor tag element by its id
function handleSessionData(resultDataString) {
    let backToSearch = document.getElementById("back");

    let resultDataJson = JSON.parse(resultDataString);

    console.log("hello");
    console.log(resultDataJson["storedSearch"]);

    let newhref = "list.html?" + resultDataJson["storedSearch"];
    console.log(backToSearch);

    backToSearch.setAttribute("href", newhref);
}

