fetch('includes/navbar.html')
    .then(response => response.text())
    .then(data => {
        document.querySelector('#navbar').innerHTML = data;

        $.ajax("api/submit-search", {
            method: "GET",
            success: handleSessionData
        });

        $('#autocomplete').keypress(function(event) {
            // keyCode 13 is the enter key
            if (event.keyCode == 13) {
                console.log("Enter pressed");

                // pass the value of the input box to the handler function
                window.location.href = "list.html?title=" + document.getElementById('autocomplete').value + "&year=&director=&star=&quantity=25&sort=0&page=1";
            }
        })

        // Initialize autocomplete
        $('#autocomplete').autocomplete({
            // documentation of the lookup function can be found under the "Custom lookup function" section
            lookup: function (query, doneCallback) {
                console.log("Looking up query: " + query);
                handleLookup(query, doneCallback)
            },
            onSelect: function(suggestion) {
                console.log("Selecting: " + suggestion.value);

                window.location.href = "single-movie.html?id=" + suggestion.data["movieId"];
            },
            // set delay time
            deferRequestBy: 300,
            // there are some other parameters that you might want to use to satisfy all the requirements
            // TODO: add other parameters, such as minimum characters
            minChars: 3
        }).autocomplete( "instance" )._renderItem = function( ul, item ) {
            console.log('test');
            // let term = this.term.split(' ').join('|');
            // let re = new RegExp("(" + term + ")", "gi") ;
            // let t = item.label.replace(re,"<strong>$1</strong>");
            //
            // return $( "<li></li>" )
            //     .data( "item.autocomplete", item )
            //     .append( "<div>" + t + "</div>" )
            //     .appendTo( ul );
        };

        console.log($('#autocomplete'));
    })
    .catch(error => {
        console.error('Error fetching navbar:', error);
    });

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData[0]["type"]);
    console.log(jsonData[1]);

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData[1] } );
}
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "search-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

// Get the anchor tag element by its id
function handleSessionData(resultDataString) {
    let backToSearch = document.getElementById("back");

    let resultDataJson = JSON.parse(resultDataString);

    let newhref = "list.html?" + resultDataJson["storedSearch"];

    backToSearch.setAttribute("href", newhref);
}

console.log("NAVBAR BABY");