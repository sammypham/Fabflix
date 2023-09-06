function handleTotalPrice(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    document.getElementById("total-price").innerHTML = "Total Price: $" + (parseFloat(resultDataJson["cartPrice"])).toFixed(2);
}

function submitPayment() {
    let cardNumber = document.getElementById("card-number").value;
    let cardFirstName = document.getElementById("first-name").value;
    let cardLastName = document.getElementById("last-name").value;
    let cardDate = document.getElementById("date").value;

    console.log(cardNumber);
    console.log(cardFirstName);
    console.log(cardLastName);
    console.log(cardDate);

    $.ajax("api/payment", {
        method: "POST",
        data: "card-num=" + cardNumber + "&first-name=" + cardFirstName + "&last-name=" + cardLastName + "&date=" + cardDate,
        success: function(jqXHR) {
            window.location.href = "confirmation.html";
        },
        error: function(jqXHR, textStatus, errorThrown) {
            let resultDataJson = JSON.parse(jqXHR.responseText);

            alert("ERROR: " + resultDataJson["errorMessage"]);
        }
    });
}

$.ajax("api/payment", {
    method: "GET",
    success: handleTotalPrice
});