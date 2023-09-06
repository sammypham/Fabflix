document.addEventListener('DOMContentLoaded', function() {
    setTimeout(function() {
        let logoutButton = document.querySelector('#logout-button');

        logoutButton.addEventListener('click', function () {
            fetch('../LogoutServlet', {method: 'POST'})
                .then(function (response) {
                    if (response.ok) {
                        // Redirect to the desired page after logout
                        window.location.href = 'login.html';
                    } else {
                        console.error('Logout failed');
                    }
                })
                .catch(function (error) {
                    console.error('Error:', error);
                });
        });
    }, 200);
});