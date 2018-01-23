$(document).foundation();

$(document).ready(function () {
  // Datatable Initialize. 
  $('#example-datatable').DataTable({
    columnDefs: [ {
        targets: [ 0 ],
        orderData: [ 0, 1 ]
    }, {
        targets: [ 1 ],
        orderData: [ 1, 0 ]
    }, {
        targets: [ 4 ],
        orderData: [ 4, 0 ]
    } ]
  });

  $("[data-responsive-toggle]").click(function () {
    var targetMenuClass =  $(this).attr("data-responsive-toggle");

    // Add class "open" to the target menu.
    $("[class*='" + targetMenuClass + "']").toggleClass("open");
  });


  if($("select#varient").length > 0) {
    // Ajax Request for Varient Choices Select
    var varientRequest = function (callback) {
      fetch('https://gist.githubusercontent.com/meladawy/ebc4afc00f6c583437ff4cdee381f0d1/raw/45f9df2d298e0af19899884d5a00d16d126741e6/gistfile1.json')
        .then(function (response) {
          response.json().then(function (data) {
            callback(data.suggestions, 'value', 'label');
          });
        })
        .catch(function (error) {
          console.log(error);
        });
    };

    var selectVarientChoices = new Choices('select#varient', {
      removeItemButton: true,
      delimiter: ',',
    }); 

    selectVarientChoices.ajax(varientRequest);
  }

  // Initialize Choices.
  if($(".choices").length > 0) {
    var multiSelectChoices = new Choices('.choices[multiple]', {
      delimiter: ',',
      editItems: true,
      maxItemCount: 5,
      removeItemButton: true,
    });

    var singleSelectChoices = new Choices('.choices:not([multiple])', {
      removeItemButton: false,
    });
  }

  // When Click on View All Cancer Types 
  $("a.view-all-cancer-types").click(function(ev) {
    $("#cancer-types").attr("style", "height:350px; overflow:auto;");
    $(this).hide();
  });


});