/**
 * Created by csaba on 12/05/2017.
 */

function init(){
    getMarkers();
}

function applyChosen(){
    jQuery(".chosen").chosen({width: "100%"});
}

function search() {

    var q = jQuery("#searchField").val();
    var markers = [];
    var dataSources = [];
    var originTumorTypes = [];
    //var filters = "/";
    var filters = Object;

    jQuery("#markerSelect_chosen ul li.search-choice").each(function( index, element ){
        markers.push(jQuery(this).text());
    });

    jQuery("#sourceSelect_chosen ul li.search-choice").each(function( index, element ){
        dataSources.push(jQuery(this).text());
    });

    jQuery("#tumorTypeSelect_chosen ul li.search-choice").each(function( index, element ){
        originTumorTypes.push(jQuery(this).text());
    });
    /*
    filters+=markers.join(",");
    filters+="/";
    filters+=dataSources.join(",");
    filters+="/";
    filters+=originTumorTypes.join(",");
    filters+="/";
    */

    filters["diag"] = q;
    filters["markers"] = markers;
    filters["datasources"] = dataSources;
    filters["origintumortypes"] = originTumorTypes;

    if(q || markers.length>0 || dataSources.length>0 || originTumorTypes.length>0){

        var ajaxrequest = jQuery.ajax({
            url : "/searchsamples/",
            type : "get",
            datatype: 'json',
            data: filters
        }).done(function(data) {
            displayResults(q,data);
        }).fail(function() {
            console.log("Error");
        });

    }
}



function displayResults(q,data){
    var div = jQuery("#resultsDiv");

    if(div.hasClass("hidden")){
        div.removeClass("hidden");
    }
    div.empty();
    div.append('<div class="panel-heading">You searched for "'+q+'" and the results are displayed below.</div>');
    div.append('<div class="panel-body"></div>');

    var resTable = jQuery("<table/>");
    resTable.addClass("table table-striped no-footer");

    resTable.append('<thead><tr><th>Data source</th><th>Tumor ID</th><th>Diagnosis</th><th>Tissue of origin</th>' +
        '<th>Classification</th><th>Cancer genomics</th></tr></thead>');
    var tbody = jQuery("<tbody/>");

    for (var i in data){
        var tr = jQuery('<tr><td>'+data[i].dataSource+'</td><td><a href="/details/'+ data[i].tumorId+
            '">'+data[i].tumorId+'</a></td><td>'+data[i].diagnosis+'</td><td>'+ data[i].tissueOfOrigin+'</td><td>'
            +data[i].classification+'</td><td>Features: '+data[i].cancerGenomics.toString()+'</td></tr>');

        tbody.append(tr);
    }

    resTable.append(tbody);
    div.append(resTable);

}

function getMarkers(){

    var ajaxrequest = jQuery.ajax({
        url : "/getallmarkers",
        type : "get"
    }).done(function(data) {

        markers = data;
        markers.sort();

        var markerSelect = jQuery("#markerSelect");

        for(var i=0;i<markers.length;i++){
            markerSelect.append('<option>'+markers[i]+'</option>');

        }

        applyChosen();
    }).fail(function() {
        console.log("Error getting markers");
    });

}