/**
 * Created by csaba on 12/05/2017.
 */


/**
 * Checks the filters and collects the parameters that are selected, t
 * hen constructs the url and redirects the user to that url
 */
function redirectPage(){

    var no_parameters = true;
    var url = "?"

    var searchField = jQuery("#query");
    //check if main search field has anything
    if (searchField.val() != null && searchField.val() != "") {
        url+="query="+searchField.val();
        no_parameters = false;
    }

    //check elements with filter class

    jQuery(".filter").each(function(){
        var id = jQuery(this).attr("id");

        //characters we want to see as values
        var reg = /[^A-Za-z0-9 _-]/;

        if (jQuery(this).is(':checked')){

            var res = id.split("__");

            if(!no_parameters){
                url = url+"&";
            }

            url = url+res[0]+"="+res[1];
            no_parameters = false;

        }
        else if(jQuery(this).is("input:text")){
            return;
        }
    });

    //TODO: deal with two and three param filters here


    for (var i=1; i<20; i++){

        var geneFilter = jQuery("#geneFilter"+i);
        var variantFilter = jQuery("#variantFilter"+i);

        if (geneFilter.val() != null && geneFilter.val() != "")
        {
            var allVariants = getVariantSize(geneFilter.val());
            for (var j=0; j<variantFilter.val().length; j++){
                if (!no_parameters) {
                    url = url + "&";
                }
                if(allVariants.length == variantFilter.val().length){
                    url += "mutation=" + geneFilter.val() + "___MUT" + "___ALL";
                    no_parameters = false;
                    break;
                }else{
                    url += "mutation=" + geneFilter.val() + "___MUT" + "___"+variantFilter.val()[j];
                    no_parameters = false;

                }
            }
        }

    }


    for (var i=1; i<20; i++){

        var drugFilter = jQuery("#drugFilter"+i);
        var responseFilter = jQuery("#responseFilter"+i);

        if (drugFilter.val() != null && drugFilter.val() != "NULL")
        {
            var allResponses = drugResponseList;
            for (var j=0; j<responseFilter.val().length; j++){
                if (!no_parameters) {
                    url = url + "&";
                }
                if(allResponses.length == responseFilter.val().length){
                    url += "drug=" + drugFilter.val() + "___ALL";
                    no_parameters = false;
                    break;
                }else{
                    url += "drug=" + drugFilter.val() + "___"+responseFilter.val()[j];
                    no_parameters = false;

                }
            }
        }

    }


    // Add all diagnosis filters to the URL
    jQuery(".diagnosis").each(function () {
        var id = jQuery(this).attr("id");

        //characters we want to see as values
        var reg = /[^A-Za-z0-9 _-]/;

        var res = id.split("__");

        if (!no_parameters) {
            url = url + "&";
        }

        if (!reg.test(res[1])) {
            url = url + res[0] + "=" + encodeURIComponent(res[1].replace(/_/g, ' '));
            no_parameters = false;
        }
    });

    window.location.replace(url);
}



var geneticVar = 1;
var counter = 1;

function loadGeneTextFields(){

    var keysAreMarkers = Object.keys(mutatedMarkersAndVariants);
    $('#geneFilter1').autocomplete({
        source: [keysAreMarkers]
    });

    for (var i = 2; i <= 20; i++) {
        $('#geneFilter' + i).autocomplete({
            source: [keysAreMarkers]
        });
    }
}

function loadVariants(selectedMarker, compNumber) {
    var marker = selectedMarker.value;
    var valuesAreVariants = mutatedMarkersAndVariants[selectedMarker.value].sort();
    //alert(marker+"\n"+typeof valuesAreVariants);
    var newOptions = "";
    for (var i = 0; i < valuesAreVariants.length; i++) {
        newOptions += "<option value='" + valuesAreVariants[i] + "' selected>" + valuesAreVariants[i] + "</option>";
    }
    var select = $('#variantFilter' + compNumber);
    select.empty().append(newOptions);
    $(function () {
        $('#variantFilter' + compNumber).change(function () {
            console.log($(this).val());
        }).multipleSelect({
            placeholder: " variants"
        });
    });
}



function addMarkerAndVariants(param, startIndex) {
    if (startIndex != 2 && counter == 1) {
        geneticVar = startIndex;
    }
    geneticVar++;
    counter++;
    for (var i = startIndex; i <= 20; i++) {
        if ((param == 'AND' || param == 'OR') && geneticVar == i) {
            //$("#geneticVar"+i).show();
            document.getElementsByClassName("geneticVar" + i)[0].style.display = "block";
        }
    }
}


function getVariantSize(selectedMarker) {
    var valuesAreVariants = mutatedMarkersAndVariants[selectedMarker];
    return valuesAreVariants;
}




/* Drug Response region */
var dosingStudy = 1;
var dosingStudyCount = 1;
function loadDrugTextFields(){

    var drugs = drugsList.sort();
    $('#drugFilter1').autocomplete({
        source: [drugs]
    });

    for (var i = 2; i <= 20; i++) {
        $('#drugFilter' + i).autocomplete({
            source: [drugs]
        });
    }
}


function loadDrugResponse(compNumber) {

    var drugResponses = drugResponseList.sort();
    var newOptions = "";
    for (var i = 0; i < drugResponses.length; i++) {
        newOptions += "<option value='" + drugResponses[i] + "' selected>" + drugResponses[i] + "</option>";
    }
    var select = $('#responseFilter' + compNumber);
    select.empty().append(newOptions);
    $(function () {
        $('#responseFilter' + compNumber).change(function () {
            console.log($(this).val());
        }).multipleSelect({
            placeholder: " Responses"
        });
    });
}



function addDrugAndResponse(param, startIndex) {
    if (startIndex != 2 && dosingStudyCount == 1) {
        dosingStudy = startIndex;
    }
    dosingStudy++;
    dosingStudyCount++;
    for (var i = startIndex; i <= 20; i++) {
        if ((param == 'AND' || param == 'OR') && dosingStudy == i) {
            document.getElementsByClassName("dosingStudy" + i)[0].style.display = "block";
            $("#drugFilter"+i).val ("");
        }
    }
}

function clearFacet() {
    document.getElementById("pdxFinderFacet").reset();
}


