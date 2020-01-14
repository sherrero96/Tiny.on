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
                var fichero = new FormData(document.getElementById("uploaderCSV"));

                //var formData = new FormData(uploaderCSV);
                //var csv = ["http://www.marca.com"];
                //formData.append("file", new Blob([csv]), "csv.csv");
                $.ajax({
                    type: "POST",
                    url: "/csv",
                    data: fichero,
                    dataType: "html",
                    processData: false,
                    contentType: false,
                    //enctype: 'multipart/form-data',
                    success: function(total) {
                        //var total = 0;
                        var data1 = total.split(",");
                        //window.location='/download';

                        if(data1[0] === "escalable"){
                            $("#result2").html(

                                "<h3>El fichero esta siendo procesado </h3>"
                                + "<h4>Numero de URI's totales: </h4>"
                                + data1[1]
                                + "<br>"
                                + "<h5>Puede descargar su fichero desde el siguiente enlace durante el proceso: </h5>"
                                + "<a href="
                                +data1[2]
                                +">"
                                + data1[2] +" </a>"

                            );
                            console.log("ANTES DE LA LLAMADA RECURSIVA")
                            var uriSalida = "/csvEscalable/" + data1[3];
                            $.ajax({
                                type: "POST",
                                url: uriSalida,
                                //enctype: 'multipart/form-data',
                                success: function(numAcortadas) {
                                    //var total = 0;
                                    $("#result2").html(
                                        "<h3>El proceso ha sido completado. </h3>"
                                        + "<h4>Numero de URI's totales: </h4>"
                                        + data1[1]
                                        + "<h5>Puede descargar su fichero desde el siguiente enlace </h5>"
                                        + "<br><a href="
                                        +data1[2]
                                        +">"
                                        + data1[2] + "</a>"
                                    );
                                    //window.location = '/delete';
                                },
                                error: function () {
                                    $("#result2").html(
                                        "<h3>Ha ocurrido un error con su fichero.</h3>");
                                }
                            });
                        }
                        else{
                            console.log("NO ESCALABLE, RESPUESTA")
                            $("#result2").html(

                                "<h4>Numero URI's totales: </h4>"
                                + data1[0]
                                +"<h4>Numero URI's acortadas: </h4>"
                                + data1[1]
                                +
                                "<br><a href="
                                +data1[2]
                                +">"
                                + "DESCARGAR FICHERO </a>"


                            );
                        }
                        //window.location = '/delete';
                    },
                    error: function () {
                        $("#result2").html(
                            "<h1>ERROR </h1>");
                    }
                });
            });
    });