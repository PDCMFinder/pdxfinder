/**
 * Created by csaba on 12/05/2017.
 */


//GLOBAL for displaying tooltips

var markerDefs = {};
markerDefs['ALK-EML4'] = 'Echinoderm microtubule-associated protein-like 4 (EML4) - Anaplastic lymphoma kinase (ALK) gene fusion';
markerDefs['BRAF']='B-Raf proto-oncogene, serine/threonine kinase';
markerDefs['BRAF V600E']='B-Raf proto-oncogene, serine/threonine kinase';
markerDefs['BRCA1']='Breast Cancer 1, DNA repair associated';
markerDefs['CD117 (c-Kit)']='KIT proto-oncogene receptor tyrosine kinase';
markerDefs['EGFR']='Epidermal growth factor receptor gene';
markerDefs['EGFR L858R']='Epidermal growth factor receptor gene';
markerDefs['EGFR T790M']='Epidermal growth factor receptor gene';
markerDefs['ER'] = 'Estrogen receptor ';
markerDefs['ERBB2 (HER2)']='Erb-b2 receptor tyrosine kinase 2';
markerDefs['KRAS']='KRAS proto-oncogene, GTPase';
markerDefs['MSI'] = 'Microsatellite instability ';
markerDefs['NRAS']='NRAS proto-oncogene, GTPase';
markerDefs['PIK3CA']='Phosphatidylinositol-4,5-bisphosphate 3-kinase catalytic subunit alpha';
markerDefs['PR'] = 'Progesterone receptor ';
markerDefs['ROS']='ROS proto-oncogene 1, receptor tyrosine kinase';

function init(){
    getMarkers();
}

function applyChosen(){
    jQuery(".chosen").chosen({width: "100%"});
}

function insertSpinner(){
    console.log('spinner');
    var div = jQuery("#resultsDiv");
    div.empty();
    if(div.hasClass("hidden")){
        div.removeClass("hidden");
    }
    div.append('<div class="row"><div class="col-md-2 col-md-offset-5"><div class="loader"></div>  Loading... </div></div></div>');
}

function search() {

    var q = jQuery("#pdxFinder").val();
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

    filters["diag"] = q;
    filters["markers"] = markers;
    filters["datasources"] = dataSources;
    filters["origintumortypes"] = originTumorTypes;

    insertSpinner();

    var ajaxrequest = jQuery.ajax({
        url : "/searchmodels/",
        type : "get",
        datatype: 'json',
        data: filters
    }).done(function(data) {
        displayResults(q,data);
    }).fail(function() {
        console.log("Error");
    });


}



function displayResults(q,data){
    var div = jQuery("#resultsDiv");
    var sources = [];

    if(div.hasClass("hidden")){
        div.removeClass("hidden");
    }
    div.empty();

    var rpanel = jQuery("<div/>");
    rpanel.addClass("panel panel-primary ");

    var tbody = jQuery("<tbody/>");
    //var markerString = '';
    for (var i in data){

        if (data[i].cancerGenomics != null && data[i].cancerGenomics.length > 0) {
            var m = data[i].cancerGenomics;
            /*markerString = '';

            for(var j=0;j<m.length;j++){
                markerString += '<span class="marker-description" data-toggle="tooltip" data-placement="top" title="' + markerDefs[m[j]] + '">' + m[j] + '</span>';
                if(j<m.length-1){
                    markerString+=', ';
                }
            }*/
        }

        var tr = jQuery('<tr><td><a href="/pdx/'+ data[i].dataSource+'/'+ data[i].modelId+
            '" target="_blank">'+data[i].modelId+'</a></td><td>'+data[i].diagnosis+'</td><td>'+ data[i].mappedOntology+'</td><td>'
            +data[i].tissueOfOrigin+'</td><td>'+data[i].dataSource+'</td></tr>');

        if(sources.indexOf(data[i].dataSource) == -1){
            sources.push(data[i].dataSource);
        }

        tbody.append(tr);


    }

    rpanel.append('<div class="panel-heading">You searched for "'+q+'". Found '+data.length+' result(s) in '+sources.length+' source(s).</div>');
    rpanel.append('<div class="panel-body"></div>');

    var resTable = jQuery("<table/>");
    resTable.attr("id","resultsTable");
    resTable.addClass("table table-striped no-footer");

    resTable.append('<thead><tr><th>Model ID</th><th>Histology</th>' +
        '<th>Ontology Mapping</th><th>Primary Tissue</th><th>Data source</th></tr></thead>');


    resTable.append(tbody);
    rpanel.append(resTable);
    div.append(rpanel);
    jQuery("#resultsTable").DataTable({
        language: {
            sSearch: "Filter:"
        }
    });

    $('[data-toggle="tooltip"]').tooltip();

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