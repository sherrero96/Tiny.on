$(document).ready(
    function () {
        console.log( "ready!" );
        // Obtain the parameters
        var parameters = window.location.search.substring(1).split("&");
        var temp = parameters[0].split("=");
        var numberVisits = parameters[0].split("=")[1];
        var ip = parameters[1].split("=")[1];
        var location = parameters[2].split("=")[1];
        var platform = parameters[3].split("=")[1].replace("+", " ");

        document.getElementById("visit").innerHTML = numberVisits;
        document.getElementById("ip").innerHTML = ip;
        document.getElementById("location").innerHTML = location;
        document.getElementById("platform").innerHTML = platform;

    }
);
