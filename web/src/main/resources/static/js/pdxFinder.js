/**
 * Created by csaba on 12/05/2017.
 */




function updateFilters(ages, genders, cancersystem, datasources, tumortype){

    console.log("updating filters!");
    //characters we want to see as values
    var reg = /[^A-Za-z0-9 _-]/;

    var openAgeFacet = false;
    var openGenderFacet = false;
    var openDatasourceFacet = false;
    var openCancerBySystem = false;
    var openTumorTypeFacet = false;

    //check selected age bins
    if(ages != null && ages.length>0){

        jQuery.each(ages, function(key, value){

            var id = value.name;
            var selected = value.selected;

            //testing id for invalid characters
            if( reg.test(id)){
                console.log("skipping id: "+id);
                return;
            }

            if(selected){
                jQuery("#patient_age__"+id).prop('checked', true);
                openAgeFacet = true;
            }

            var count = " ("+value.count+")";
            jQuery("#patient_age__"+id).siblings("label").find("span").append(count);
            console.log(id+" "+count);

        });

        if(openAgeFacet){
            var ageFilterField = jQuery("li#age_filter > a.accordion-title");
            ageFilterField.click();
        }
    }




    //check selected gender options
    if(genders != null && genders.length>0){

        jQuery.each(genders, function(key, value){

            var id = value.name;
            var selected = value.selected;

            //testing id for invalid characters
            if( reg.test(id)){
                console.log("skipping id: "+id);
                return;
            }

            if(selected){
                jQuery("#patient_gender__"+id).prop('checked', true);
                openGenderFacet = true;
            }

            var count = " ("+value.count+")";
            jQuery("#patient_gender__"+id).siblings("label").find("span").append(count);
            console.log(id+" "+count);

        });

        if(openGenderFacet){
            var genderFilterField = jQuery("li#gender_filter > a.accordion-title");
            genderFilterField.click();
        }
    }


    //check selected cancer systems
    if(cancersystem != null && cancersystem.length>0){

        jQuery.each(cancersystem, function(key, value){

            var id = value.name;
            id = id.replace(" ","_");
            var selected = value.selected;

            //testing id for invalid characters
            if( reg.test(id)){
                console.log("skipping id: "+id);
                return;
            }

            if(selected){
                jQuery("#cancer_system__"+id).prop('checked', true);
                openCancerBySystem = true;
            }

            var count = " ("+value.count+")";
            jQuery("#cancer_system__"+id).siblings("label").find("span").append(count);

        });

        if(openCancerBySystem){
            var cancerSystemFilterField = jQuery("li#cancer_system_filter > a.accordion-title");
            cancerSystemFilterField.click();
        }
    }


    //check selected datasources
    if(datasources != null && datasources.length>0) {

        jQuery.each(datasources, function (key, value) {

            var id = value.name;
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#datasource__" + id).prop('checked', true);
                openDatasourceFacet = true;
            }

            var count = " (" + value.count + ")";
            jQuery("#datasource__" + id).siblings("label").find("span").append(count);

        });

        if (openDatasourceFacet) {
            var dsFilterField = jQuery("li#datasource_filter > a.accordion-title");
            dsFilterField.click();
        }


    }

    //check selected tumorTypes
    if(tumortype != null && tumortype.length>0) {

        jQuery.each(tumortype, function (key, value) {

            var id = value.name;
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#sample_tumor_type__" + id).prop('checked', true);
                openTumorTypeFacet = true;
            }

            var count = " (" + value.count + ")";
            jQuery("#sample_tumor_type__" + id).siblings("label").find("span").append(count);

        });

        if (openTumorTypeFacet) {
            var ttFilterField = jQuery("li#tumor_type_filter > a.accordion-title");
            ttFilterField.click();
        }


    }







}


function redirectPage(){

    var no_parameters = true;
    var url = "?"

    var searchField = jQuery("#query");

    if (searchField.val() != null && searchField.val() != "") {
        url+="query="+searchField.val();
        no_parameters = false;
    }


    //get all filters with values
    jQuery(".filter").each(function(){
        var id = jQuery(this).attr("id");

        //characters we want to see as values
        var reg = /[^A-Za-z0-9 _-]/;

        if (jQuery(this).is(':checked')){


            var res = id.split("__");

            if(!no_parameters){
                url = url+"&";
            }

            if( ! reg.test(res[1])){
                url = url+res[0]+"="+encodeURIComponent(res[1].replace("_"," "));
                no_parameters = false;

            }
        }
        else if(jQuery(this).is("input:text")){
            return;
        }


    });
    console.log(url);

    window.location.replace(url);
}


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
    rpanel.addClass("panel panel-primary lilPadding");

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
            '" target="_blank" class="pdxfinder-link" style="text-decoration:none">'+data[i].modelId+'</a></td><td>'+data[i].diagnosis+'</td><td>'+data[i].tissueOfOrigin+'</td><td>'+ data[i].mappedOntology+'</td><td>'+data[i].dataSource+'</td></tr>');

        if(sources.indexOf(data[i].dataSource) == -1){
            sources.push(data[i].dataSource);
        }

        tbody.append(tr);


    }

    var searchedFor = "";
    if(q){
        searchedFor = "You searched for '"+q+"'. ";
    }
    
    rpanel.append('<div class="panel-heading"><h5>'+searchedFor+'Found '+data.length+' result(s) in '+sources.length+' source(s). </h5></div>');
    rpanel.append('<div class="panel-body"></div>');

    var resTable = jQuery("<table/>");
    resTable.attr("id","resultsTable");
    resTable.addClass("table table-striped table-borderedPdx table-hover");

    resTable.append('<thead><tr><th>Model ID</th><th>Histology</th><th>Primary Tissue</th><th>Ontology Mapping</th><th>Data source</th></tr></thead>');


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