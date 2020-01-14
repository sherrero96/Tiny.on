$(document).ready(
    function (){
        $("#uploaderCSV").submit(
            function(e){
                e.preventDefault();

                var fichero = new FormData(document.getElementById("uploaderCSV"));

                $.ajax({
                    type: "POST",
                    url: "/csv",
                    data: fichero,
                    dataType: "html",
                    processData: false,
                    contentType: false,
                    success: function(total) {
                        var data1 = total.split(",");
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
                            var uriSalida = "/csvEscalable/" + data1[3];
                            $.ajax({
                                type: "POST",
                                url: uriSalida,
                                success: function(res) {
                                    if(res<0){
                                        $("#result2").html(
                                            "<h3>Ha ocurrido un error con su fichero.</h3>");
                                    }
                                    else{
                                        $("#result2").html(
                                            "<h3>El proceso ha sido completado. </h3>"
                                            + "<h4>Numero de URI's totales: </h4>"
                                            + data1[1]
                                            + "<h5>Puede descargar su fichero desde el siguiente enlace: </h5>"
                                            + "<a href="
                                            +data1[2]
                                            +">"
                                            + data1[2] + "</a>"
                                        );
                                        //window.location = '/delete';
                                    }

                                },
                                error: function () {
                                    $("#result2").html(
                                        "<h3>Ha ocurrido un error con su fichero.</h3>");
                                }
                            });
                        }
                        else{
                            $("#result2").html(
                                "<h3>El proceso ha sido completado. </h3>"
                                + "<h4>Numero de URI's totales: </h4>"
                                + data1[0]
                                + "<h5>Puede descargar su fichero desde el siguiente enlace: </h5>"
                                + "<a href="
                                +data1[1]
                                +">"
                                + data1[1] + "</a>"
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