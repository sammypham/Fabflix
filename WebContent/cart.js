let cart = $("#cart");

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);

    let cartBody = jQuery("#cart-body");

    let rowHTML = "";
    let totalPrice = 0;

    for (let i = 0; i < resultArray.length; i++) {
        rowHTML += "<tr>";
        rowHTML += "<th><a class=roundButton href=\'single-movie.html?id=" + resultArray[i]["movie_id"] + "\'</a>" + resultArray[i]["title"] + "</th>";
        rowHTML += "<th><button class=roundButton id='sub-quantity' onclick='subtractQuantity(this)' data-movie-id=\'" + resultArray[i]["movie_id"] + "\' data-quantity=\'" + resultArray[i]["quantity"] + "\'>-</button><p id='quantity-" + resultArray[i]["movie_id"] + "'>" + resultArray[i]["quantity"] + "</p><button class=roundButton id='add-quantity' onclick='addQuantity(this)' data-movie-id=\'" + resultArray[i]["movie_id"] + "\' data-quantity=\'" + resultArray[i]["quantity"] + "\'>+</button></th>";
        rowHTML += "<th><button class=roundButton id='cart-delete' onclick='deleteItem(this)' data-movie-id=\'" + resultArray[i]["movie_id"] + "\'>Delete</button></th>";
        rowHTML += "<th>$5.00</th>"
        rowHTML += "<th>$" + (5*parseInt(resultArray[i]["quantity"])).toFixed(2) + "</th>"
        rowHTML += "</tr>";

        totalPrice += (5*parseInt(resultArray[i]["quantity"]));
    }

    cartBody.append(rowHTML);

    totalPrice = totalPrice.toFixed(2);

    let totalPriceElement = document.getElementById("total-price");
    totalPriceElement.innerHTML = "$" + (totalPrice).toString();
    console.log(totalPriceElement.innerHTML);
}

function subtractQuantity(button) {
    console.log("Subtract quantity");
    let addButtons = document.querySelectorAll("#add-quantity");
    let subButtons = document.querySelectorAll("#sub-quantity");

    for (let i = 0; i < addButtons.length; i++) {
        addButtons[i].disabled = true;
        subButtons[i].disabled = true;
    }

    $.ajax("api/index", {
        method: "POST",
        data: "item=" + button.dataset.movieId + "&add=" + "-1",
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);

            window.location.href = "cart.html";
        }
    });

}

function addQuantity(button) {
    console.log("Add quantity");
    let addButtons = document.querySelectorAll("#add-quantity");
    let subButtons = document.querySelectorAll("#sub-quantity");

    for (let i = 0; i < addButtons.length; i++) {
        addButtons[i].disabled = true;
        subButtons[i].disabled = true;
    }

    $.ajax("api/index", {
        method: "POST",
        data: "item=" + button.dataset.movieId + "&add=" + 1,
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);

            window.location.href = "cart.html";
        }
    });

}

function deleteItem(button) {
    console.log("Delete item");
    console.log(button.dataset.movieId);

    $.ajax("api/index", {
        method: "POST",
        data: "item=" + button.dataset.movieId + "&add=0" + "&del=" + 1,
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);

            window.location.href = "cart.html";
        }
    });
}

$.ajax("api/index", {
    method: "GET",
    success: handleSessionData
});
