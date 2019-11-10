$(document).ready(
    function () {
        $("#shortener").submit(
            function (event) {
                event.preventDefault();
                $.ajax({
                    type: "POST",
                    url: "/link",
                    data: $(this).serialize(),
                    success: function (msg) {
                        $("#result").html(
                            "<div class='alert alert-success lead'><a target='_blank' href='"
                            + msg.uri
                            + "'>"
                            + msg.uri
                            + "</a></div>"
                            + "<img src="
                            + getQRUri(msg.uri)
                            + " class=img-fluid >");
                    },
                    error: function () {
                        $("#result").html(
                            "<div class='alert alert-danger lead'>ERROR</div>");
                    }
                });
            });
    });

    function getQRUri(uri) {
      // var base_uri = uri.split('/')[0];
      var id = uri.split('/').pop();

      return window.location.origin + "/qr?id=" + id;
    }
