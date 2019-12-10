$(document).ready(
    function (){
        $("#uploaderCSV").submit(
            function(e){
                e.preventDefault();
                //var fil = document.getElementById("ficheroCsv");
                //var data = new FormData(this);

                //var file = document.getElementById("ficheroCsv");
                //var uploadFile = new FormData();
                //var files = $("#ficheroCsv").get(0).files;
                //uploadFile.append("CsvDoc", files[0]);


                var formData = new FormData(uploaderCSV);
                var csv = ["http://www.marca.com"];
                formData.append("file", new Blob([csv]), "csv.csv")
                $.ajax({
                    type: "POST",
                    url: "/csv",
                    data: formData,
                    processData: false,
                    contentType: false,
                    enctype: 'multipart/form-data',
                    success: function (total) {
                        var correctas = document.getElementById("uriCorrectas")
                        var totales = document.getElementById("uriTotal")
                        console.log("Correctas: " + correctas)
                        console.log("Totales: " + totales)
                        $("#result2").html(
                            "<h1>Numero URI's totales: </h1>"
                            + total[0]
                            +"<h1>Numero URI's acortadas: </h1>"
                            + total[1]);
                    },
                    error: function () {
                        $("#result2").html(
                            "<h1>ERROR </h1>");
                    }
                });
            });
    });