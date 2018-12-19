/**
 * Created by csaba on 12/05/2017.
 */


/**
 * Checks the filters and collects the parameters that are selected, t
 * hen constructs the url and redirects the user to that url
 */
function redirectPage(webFacetSections){

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







    webFacetSections.forEach(buildURL);

    function buildURL(webFacetSection, index) {

        var filterComponents = webFacetSection.filterComponents;

        // Retrieve All the FilterComponents and their contents
        filterComponents.forEach(function(filterComponent){

            if (filterComponent.type === 'TwoParamUnlinkedFilter'){

                options1List = filterComponent.options1;
                options2List = filterComponent.options2;
                componentId1 = filterComponent.urlParam+"_"+(filterComponent.param1Name).toLowerCase();
                componentId2 = filterComponent.urlParam+"_"+(filterComponent.param2Name).toLowerCase();
                urlKey = filterComponent.urlParam;

                intializeTwoParamUnlinkedFilterOptionOneList(dataList, componentId1);
                intializeTwoParamUnlinkedFilterOptionTwoList(componentId2);



                for (var i=1; i<20; i++){

                    var component1Choice = jQuery("#"+componentId1+i);
                    var component2Choices = jQuery("#"+componentId2+i);

                    if (component1Choice.val() != null && component1Choice.val() != "NULL")
                    {

                        for (var j=0; j<component2Choices.val().length; j++){

                            if (!no_parameters) {
                                url = url + "&";
                            }

                            if(options2List.length == component2Choices.val().length){

                                url += urlKey+"=" + component1Choice.val() + "___ALL";
                                no_parameters = false;
                                break;
                            }else{
                                url += urlKey+"=" + component1Choice.val() + "___"+component2Choices.val()[j];
                                no_parameters = false;

                            }
                        }
                    }

                }


            }
        });
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







function intializeTwoParamUnlinkedFilterOptionOneList(dataList, componentId){

    dataList = dataList.sort();

    /*$('#'+componentId).autocomplete({
        source: [dataList]
    });*/

    for (var i = 1; i <= 20; i++) {
        $('#'+componentId + i).autocomplete({
            source: [dataList]
        });
    }
}

function intializeTwoParamUnlinkedFilterOptionTwoList(componentId){

    /*$('#'+componentId).change(function () {
        //console.log($(this).val());
    }).multipleSelect({
        placeholder: "Responses"
    });*/

    for (var i = 1; i <= 20; i++) {
        $('#'+componentId+ i).change(function () {
            //console.log($(this).val());
        }).multipleSelect({
            placeholder: "Responses"
        });
    }
}

function displayMore(divId) {

    for (var i = 2; i <= 20; i++) {

        var hiddenDiv = divId+'_hidden'+i;
        var textComponentId = divId+i;

        var hiddenDivDOM = document.getElementById(hiddenDiv);
        var textComponentDOM = document.getElementById(textComponentId);

        if (hiddenDivDOM.style.display === "none"){

            hiddenDivDOM.style.display = "block";
            textComponentDOM.value = "";
            break;
        }

    }
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








function clearFacet() {
    document.getElementById("pdxFinderFacet").reset();
}


