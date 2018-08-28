/**
 * Created by csaba on 12/05/2017.
 */


/** This function updates the UI filter based on latest changes in USER CHOICE
 *
 * @param ages : A list of patients age selected by the user
 * @param genders : A list of patient genders/sex selected by the user
 * @param cancersystem : A list of cancer systems selected by the user
 * @param datasources : A list of data sources selced by the user
 * @param tumortype : A list of tumour types selected by the user
 *
 */
function updateFilters(ages, genders, cancersystem, datasources, tumortype) {

    console.log("updating filters!");
    //characters we want to see as values
    var reg = /[^A-Za-z0-9 _-]/;

    var openAgeFacet = false;
    var openGenderFacet = false;
    var openDatasourceFacet = false;
    var openCancerBySystem = false;
    var openTumorTypeFacet = false;

    //check selected age bins
    if (ages != null && ages.length > 0) {

        jQuery.each(ages, function (key, value) {

            var id = value.name;
            id = id.replace(" ", "_");
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#patient_age__" + id).prop('checked', true);
                jQuery("#patient_age__" + id).siblings("label").find("span").addClass("selected");
                openAgeFacet = true;
            }

            //Add a plus to patient age 90
            if (id == '90') {
                jQuery("#patient_age__" + id).siblings("label").find("span").append("+");
            }

            var count = " (" + value.count + " of " + value.totalCount + ")";
            // jQuery("#patient_age__"+id).siblings("label").find("span").append(count);


        });

        if (openAgeFacet) {
            var ageFilterField = jQuery("li#age_filter > a.accordion-title");
            ageFilterField.click();
        }
    }


    //check selected gender options
    if (genders != null && genders.length > 0) {

        jQuery.each(genders, function (key, value) {

            var id = value.name;
            id = id.replace(" ", "_");
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#patient_gender__" + id).prop('checked', true);
                jQuery("#patient_gender__" + id).siblings("label").find("span").addClass("selected");
                openGenderFacet = true;
            }

            var count = " (" + value.count + " of " + value.totalCount + ")";
            // jQuery("#patient_gender__"+id).siblings("label").find("span").append(count);

        });

        if (openGenderFacet) {
            var genderFilterField = jQuery("li#gender_filter > a.accordion-title");
            genderFilterField.click();
        }
    }


    //check selected cancer systems
    if (cancersystem != null && cancersystem.length > 0) {

        jQuery.each(cancersystem, function (key, value) {

            var id = value.name;
            id = id.replace(/ /g, "_");
            var selected = value.selected;

            console.log("system id:" + id);
            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#cancer_system__" + id).prop('checked', true);
                jQuery("#cancer_system__" + id).siblings("label").find("span").addClass("selected");
                openCancerBySystem = true;
            }

            var count = " (" + value.count + " of " + value.totalCount + ")";
            // jQuery("#cancer_system__"+id).siblings("label").find("span").append(count);

        });

        if (openCancerBySystem) {
            var cancerSystemFilterField = jQuery("li#cancer_system_filter > a.accordion-title");
            cancerSystemFilterField.click();
        }
    }


    //check selected datasources
    if (datasources != null && datasources.length > 0) {

        jQuery.each(datasources, function (key, value) {

            var id = value.name;
            id = id.replace(" ", "_");
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#datasource__" + id).prop('checked', true);
                jQuery("#datasource__" + id).siblings("label").find("span").addClass("selected");
                openDatasourceFacet = true;
            }

            var count = " (" + value.count + " of " + value.totalCount + ")";
            // jQuery("#datasource__" + id).siblings("label").find("span").append(count);

        });

        if (openDatasourceFacet) {
            var dsFilterField = jQuery("li#datasource_filter > a.accordion-title");
            dsFilterField.click();
        }


    }

    //check selected tumorTypes
    if (tumortype != null && tumortype.length > 0) {

        jQuery.each(tumortype, function (key, value) {

            var id = value.name;
            id = id.replace(" ", "_");
            var selected = value.selected;

            //testing id for invalid characters
            if (reg.test(id)) {
                console.log("skipping id: " + id);
                return;
            }

            if (selected) {
                jQuery("#sample_tumor_type__" + id).prop('checked', true);
                jQuery("#sample_tumor_type__" + id).siblings("label").find("span").addClass("selected");
                openTumorTypeFacet = true;
            }

            var count = " (" + value.count + " of " + value.totalCount + ")";
            // jQuery("#sample_tumor_type__" + id).siblings("label").find("span").append(count);

        });

        if (openTumorTypeFacet) {
            var ttFilterField = jQuery("li#tumor_type_filter > a.accordion-title");
            ttFilterField.click();
        }


    }

    // Check selected Molechular Characterization:
    var urlParams = new URLSearchParams(window.location.search);
    var dURLString = urlParams.toString();
    var openMarkerFacet = dURLString.search("mutation");
    if (openMarkerFacet != -1) {
        jQuery("li#marker_filter > a.accordion-title").click();
    }

    // Check if Dosing Study was selected
    var openDrugFacet = dURLString.search("drug");
    if (openDrugFacet != -1) {
        jQuery("li#drug_filter > a.accordion-title").click();
    }

}
/* End updateFilters function */




function redirectPage(){

    var no_parameters = true;
    var url = "?"

    var searchField = jQuery("#query");

    if (searchField.val() != null && searchField.val() != "") {
        url+="query="+searchField.val();
        no_parameters = false;
    }


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
                url = url+res[0]+"="+encodeURIComponent(res[1].replace(/_/g, ' '));
                no_parameters = false;

            }
        }
        else if(jQuery(this).is("input:text")){
            return;
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


