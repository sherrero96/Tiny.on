$(document).ready(
    function (){
        $("#uploaderCSV").submit(
            function(e){
                e.preventDefault();
                //var fil = document.getElementById("ficheroCsv");
                //var data = new FormData(this);
/*
                var file = document.getElementById("ficheroCsv");
                var uploadFile = new FormData();
                var files = $("#ficheroCsv").get(0).files;
                uploadFile.append("CsvDoc", files[0]);*/


                //var formData = new FormData(uploaderCSV);
                //var csv = ["http://www.marca.com"];
                //formData.append("file", new Blob([csv]), "csv.csv");
                $.ajax({
                    type: "POST",
                    url: "/csv",
                    data: new FormData(document.getElementById("uploaderCSV"), $(this).serialize()),
                    dataType: "html",
                    processData: false,
                    contentType: false,
                    //enctype: 'multipart/form-data',
                    success: function(total) {
                        //var total = 0;
                        var data = total.split(",");
                        //window.location='/download';
                        $("#result2").html(

                            "<h1>Numero URI's totales: </h1>"
                            + data[0]
                            +"<h1>Numero URI's acortadas: </h1>"
                            + data[1]
                            +
                            "<br><a href="
                            +data[2]
                            +">"
                            + "DESCARGAR FICHERO </a>"

                        );
                        //window.location = '/delete';
                    },
                    error: function () {
                        $("#result2").html(
                            "<h1>ERROR </h1>");
                    }
                });
            });
    });