$(document).ready(function () {

document.getElementById("fromDate").valueAsDate = new Date();
document.getElementById("toDate").valueAsDate = new Date();

    $.fn.serializeObject = function () {

        var o = {};

        var a = this.serializeArray();

        $.each(a, function () {

            if (o[this.name]) {

                if (!o[this.name].push) {

                    o[this.name] = [o[this.name]];

                }

                o[this.name].push(this.value || '');

            } else {

                o[this.name] = this.value || '';

            }

        });

        return o;

    };


    intializeOrReloadDataTable();

    $("#submit").click(function (event) {

        console.log("Submit 1");

        formSubmit(event);

    });

});



var dataTable = null;

function intializeOrReloadDataTable() {

    var fromDate = $("#fromDate").val();
    var toDate = $("#toDate").val();

    //$("#validateButton").on("click", function() {

           var visitorsData = document.getElementById("visitorsData");

        if (toDate < fromDate) {
            $("#message").text("End Date must be greater than or equal to  Start Date").css("color", "red");
                    visitorsData.style.display ='none';
            return;
        } else {
            $("#message").text("");
            visitorsData.style.display ='block';
        }
    //});

if (dataTable != null) {

        dataTable.clear().draw();

        dataTable.destroy();

    }
    var visitorsDataFooter = document.getElementById("visitorsDataFooter");
        visitorsDataFooter.style.display ='table-footer-group';

    $.ajax({

        url: 'http://localhost:8080/buckets/objects?fromDate='+fromDate+"&toDate="+toDate,

        type: "GET",

        contentType: "application/json; charset=utf-8",

        success: function (data) {

            var items = [];
            visitorsDataFooter.style.display = 'none';
            $.each(data, function (key, val) {

                console.log(key);

                console.log(val);

                var slNo = items.length + 1;

                items.push('<tr id="' + key + '"><td>' + slNo + '</td><td contenteditable="false">' + val.name + '</td><td contenteditable="false">' + val.number + '</td><td contenteditable="false">' + val.whomToMeet + '</td><td contenteditable="false">' + val.purpose + '</td><td contenteditable="false">' + val.parcel + '</td><td contenteditable="false">' + val.inTime + '</td><td contenteditable="false">' + val.outTime + '</td><td contenteditable="false">' + val.remark + '</td><td contenteditable="false"> <button class="btn btn-primary" onclick="editData(\'' + key + '\',\'' + encodeURIComponent(JSON.stringify(val)) + '\')"><i class="fa fa-edit"></i></button><button class="btn btn-danger" onclick="deleteData(\'' + key + '\')"><i class="fa fa-trash"></i></button> </td></tr>');

            });

            $('#visitorsDataBody').html(items.join(''));

            dataTable = $('#table-id').DataTable(
                {
                "paging": true,
                 "lengthMenu": [10, 25, 50],
                 "pageLength": 10,
                 "ordering": true,
                }
            );

        },

        error: function (data) {

            alert("Error " + data);

        }

    });

}



function deleteData(fileName) {

    $.ajax({

        url: 'http://localhost:8080/buckets/objects/' + fileName,

        type: "DELETE",

        contentType: "application/json; charset=utf-8",

        success: function (data) {

            intializeOrReloadDataTable();

        },

        error: function (data) {

            alert("Error " + data);

        }

    });

}


function downloadData() {
    var fromDate = $("#fromDate").val();
    var toDate = $("#toDate").val();
    $.ajax({

        url: 'http://localhost:8080/buckets/downloadObjects?fromDate='+fromDate+"&toDate="+toDate,

        type: "GET",

        contentType: "application/json; charset=utf-8",

        success: function (data) {

            var byteArray = data;

            var a = window.document.createElement('a');

            a.href = window.URL.createObjectURL(new Blob([byteArray], { type: 'application/octet-stream' }));

            a.download = "visitorDetails.csv";

            document.body.appendChild(a)

            a.click();

            document.body.removeChild(a)

        },

        error: function (data) {

            alert("Error " + data);

        }

    });

}


function editData(fileName, data) {

    console.log(fileName);

    var data = JSON.parse(decodeURIComponent(data));

    console.log(data.name);

    $("#fileName").val(fileName);

    $("#name").val(data.name);

    $("#number").val(data.number);

    $("#whomToMeet").val(data.whomToMeet);

    $("#purpose").val(data.purpose);

    $("#parcel").val(1);

    $("#inTime").val(data.inTime);

    $("#outTime").val(data.outTime);

    $("#remark").val(data.remark);

    toggleAddForm(true);

    return false;

}


$(document).ready(function () {

    $flag = 1;

    $("#name").focusout(function () {

        if ($(this).val() == '') {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_name").text("* You have to enter your name!");

        }

        else {

            $(this).css("border-color", "#2eb82e");

            $('#submit').attr('disabled', false);

            $("#error_name").text("");

        }

    });

    $("#number").focusout(function () {

        $pho = $("#number").val();

        if ($(this).val() == '') {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_number").text("* You have to enter your Phone Number!");

        }

        else if ($pho.length != 10) {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_number").text("* Lenght of Phone Number Should Be Ten");

        }

        else if (!$.isNumeric($pho)) {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_number").text("* Phone Number Should Be Numeric");

        }

        else {

            $(this).css({ "border-color": "#2eb82e" });

            $('#submit').attr('disabled', false);

            $("#error_number").text("");

        }

    });


    $("#whomToMeet").focusout(function () {

        if ($(this).val() == '') {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_whomToMeet").text("* You have to enter Whom to meet!");

        }

        else {

            $(this).css("border-color", "#2eb82e");

            $('#submit').attr('disabled', false);

            $("#error_whomToMeet").text("");

        }

    });


    $("#inTime").focusout(function () {

        if ($(this).val() == '') {

            $(this).css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_inTime").text("* You have to enter your In Time");

        }

        else {

            $(this).css("border-color", "#2eb82e");

            $('#submit').attr('disabled', false);

            $("#error_inTime").text("");

        }

    });


});




function formSubmit(event) {

    if ($("#name").val() == '') {

        $("#name").css("border-color", "#FF0000");

        $('#submit').attr('disabled', true);

        $("#error_name").text("* You have to enter your Name!");

    }

    if ($("#number").val() == '') {

        $("#number").css("border-color", "#FF0000");

        $('#submit').attr('disabled', true);

        $("#error_number").text("* You have to enter your Phone Number!");

    }

    if ($("#whomToMeet").val() == '') {

            $("#whomToMeet").css("border-color", "#FF0000");

            $('#submit').attr('disabled', true);

            $("#error_whomToMeet").text("* You have to enter Whom to meet!");

        }


    if ($("#inTime").val() == '') {

        $("#inTime").css("border-color", "#FF0000");

        $('#submit').attr('disabled', true);

        $("#error_inTime").text("* You have to enter your In Time");

    }

    if($("#inTime").val() != null && $("#outTime").val() != null && $("#inTime").val() != '' && $("#outTime").val() != '') {
    if ($("#outTime").val() < $("#inTime").val()) {
                $("#inTime").css("border-color", "#FF0000");
                $("#error_inTime").text("To Time must be greater than or equal to  In Time");
                return;
            } else {
                $("#error_inTime").text("");
            }
    }

    event.preventDefault();

    var data = JSON.stringify($("#postCall").serializeObject());

    console.log(data);

    if(data.name != '') {
       $.ajax({

               url: 'http://localhost:8080/buckets/objects',

               type: "POST",

               data: data,

               dataType: "text",

               contentType: "application/json; charset=utf-8",

               success: function (data) {

                if(data == "Ok") {
                    document.getElementById("postCall").reset();

                   var successObj = $(".success")

                   successObj.toggleClass("display-success");

                   successObj.html("Data Saved Successfully");

                   setTimeout(function() {
                       successObj.toggleClass("display-success");
                   }, 3000)

                   intializeOrReloadDataTable();
               }
                   console.log(data);

               },

               error: function (data) {

                   alert("Error " + data);

               }

           });
    }

};

function toggleAddForm(isEdit) {
  var x = document.getElementById("contentBody");

  var y = document.getElementById("searchcontainer");

var addId = document.getElementById("addId");

var exportId = document.getElementById("exportId");

  if (isEdit || x.style.display === "none") {
    x.style.display = "block";
    y.style.display = "none";
    exportId.style.display = "none";
    $('#addId').html("View <i class='fa fa-eye'></i>")
  } else {
    x.style.display = "none";
    y.style.display = "block";
    exportId.style.display = "block";
    $('#addId').html("Add <i class='fa fa-plus'></i>")
  }
}



