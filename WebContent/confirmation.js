function handleConfirmationTable(resultArray) {
    let resultDataJson = JSON.parse(resultArray);;

    console.log(resultDataJson);

    let totalPurchases = resultDataJson["totalPurchases"];

    let cartBody = jQuery("#order-history-body");

    let rowHTML = "";
    let totalPrice = 0;

    for (let i = 0; i < totalPurchases.length; i++) {
        rowHTML += "<tr>";
        rowHTML += "<th>" + totalPurchases[i]["sale_id"] + "</th>"
        rowHTML += "<th><a class=roundButton href=\'single-movie.html?id=" + totalPurchases[i]["movie_id"] + "\'</a>" + totalPurchases[i]["title"] + "</th>";
        rowHTML += "<th>" + totalPurchases[i]["quantity"] + "</th>";
        rowHTML += "<th>$5.00</th>"
        rowHTML += "<th>$" + (5*parseInt(totalPurchases[i]["quantity"])).toFixed(2) + "</th>"
        rowHTML += "</tr>";

        totalPrice += (5*parseInt(totalPurchases[i]["quantity"]));
    }

    cartBody.append(rowHTML);

    totalPrice = totalPrice.toFixed(2);

    let totalPriceElement = document.getElementById("total-price");
    totalPriceElement.innerHTML = "$" + (totalPrice).toString();
    console.log(totalPriceElement.innerHTML);
}

$.ajax("api/confirmation", {
    method: "GET",
    success: handleConfirmationTable
});