/**
 * Created by csaba on 12/05/2017.
 */


/**
 * Checks the filters and collects the parameters that are selected, t
 * hen constructs the url and redirects the user to that url
 */
function redirectPage(webFacetSections) {

    var no_parameters = true;
    var url = "?"

    var searchField = jQuery("#query");
    //check if main search field has anything
    if (searchField.val() != null && searchField.val() != "") {
        url += "query=" + searchField.val();
        no_parameters = false;
    }

    //check elements with filter class

    jQuery(".filter").each(function () {
        var id = jQuery(this).attr("id");

        //characters we want to see as values
        var reg = /[^A-Za-z0-9 _-]/;

        if (jQuery(this).is(':checked')) {

            var res = id.split("__");

            if (!no_parameters) {
                url = url + "&";
            }

            url = url + res[0] + "=" + res[1];
            no_parameters = false;

        }
        else if (jQuery(this).is("input:text")) {
            return;
        }
    });



    var twoParamUnlinkedFilters = getFiltersFromWebFacetSection(webFacetSections, 'TwoParamUnlinkedFilter');
    twoParamUnlinkedFilters.forEach(function (filterComponent) {

            options2List = filterComponent.options2;
            componentId1 = filterComponent.urlParam + "_" + (filterComponent.param1Name).toLowerCase();
            componentId2 = filterComponent.urlParam + "_" + (filterComponent.param2Name).toLowerCase();
            urlKey = filterComponent.urlParam;

            for (var i = 0; i < 19; i++) {

                var component1Choice = jQuery("#" + componentId1 + i);
                var component2Choices = jQuery("#" + componentId2 + i);

                if (component1Choice.val() != null && component1Choice.val() != "NULL") {

                    for (var j = 0; j < component2Choices.val().length; j++) {

                        if (!no_parameters) {
                            url = url + "&";
                        }

                        if (options2List.length == component2Choices.val().length) {

                            url += urlKey + "=" + component1Choice.val() + "___ALL";
                            no_parameters = false;
                            break;
                        } else {
                            url += urlKey + "=" + component1Choice.val() + "___" + component2Choices.val()[j];
                            no_parameters = false;

                        }
                    }
                }

            }
    });






    var twoParamLinkedFilters = getFiltersFromWebFacetSection(webFacetSections, 'TwoParamLinkedFilter');
    twoParamLinkedFilters.forEach(function (filterComponent) {

        options2ListMap = filterComponent.options2;
        componentId1 = filterComponent.urlParam + "_" + (filterComponent.param1Name).toLowerCase();
        componentId2 = filterComponent.urlParam + "_" + (filterComponent.param2Name).toLowerCase();
        urlKey = filterComponent.urlParam;

        for (var i = 0; i < 19; i++) {

            var component1Choice = jQuery("#" + componentId1 + i);
            var component2Choices = jQuery("#" + componentId2 + i);

            if (component1Choice.val() != null && component1Choice.val() != "") {

                options2List = options2ListMap[component1Choice.val()];

                for (var j = 0; j < component2Choices.val().length; j++) {

                    if (!no_parameters) {
                        url = url + "&";
                    }

                    if (options2List.length == component2Choices.val().length) {

                        url += urlKey + "=" + component1Choice.val() + "___ALL";
                        no_parameters = false;
                        break;
                    } else {
                        url += urlKey + "=" + component1Choice.val() + "___" + component2Choices.val()[j];
                        no_parameters = false;

                    }
                }
            }

        }
    });

    

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










/****************************************************************
 *         MULTI PARAM  FILTER SECTION STARTS                   *
 ****************************************************************/


function intializeFilters(webFacetSection, index) {

    var filterComponents = webFacetSection.filterComponents;

    // Retrieve All the FilterComponents and their contents
    filterComponents.forEach(function(filterComponent){

        if (filterComponent.type === 'TwoParamLinkedFilter' || filterComponent.type === 'TwoParamUnlinkedFilter'){

            dataList = filterComponent.options1;
            componentOneId = filterComponent.urlParam+"_"+(filterComponent.param1Name).toLowerCase();
            componentTwoId = filterComponent.urlParam+"_"+(filterComponent.param2Name).toLowerCase();
            filterButton = componentOneId+'_button';

            initializeTwoParamFilterComponents(dataList, componentOneId, componentTwoId,filterComponent.param2Name);

            //Add event listener to each TwoParamUnlinkedFilter filter class
            jQuery('#'+filterButton).click(function () {
                redirectPage(webFacetSections);
            });
        }
    });
}



function initializeTwoParamFilterComponents(dataList, componentOneId, componentTwoId, placeHolderText) {

    dataList = dataList.sort();

    placeHolderText = placeHolderText.charAt(0).toUpperCase() + placeHolderText.substr(1).toLowerCase()+"s";

    for (var i = 0; i <= 19; i++) {

        $('#' + componentOneId + i).autocomplete({
            source: [dataList]
        });

        $('#' + componentTwoId + i).change(function () {
            //console.log($(this).val());
        }).multipleSelect({
            placeholder: placeHolderText
        });
    }
}



function getFiltersFromWebFacetSection(webFacetSections, desiredFilterType) {

    filterComponentsArray = [];
    webFacetSections.forEach(getFilterComponents);

    function getFilterComponents(webFacetSection, index) {

        var filterComponents = webFacetSection.filterComponents;

        // Retrieve All the FilterComponents and their contents
        filterComponents.forEach(function(filterComponent) {

            if (filterComponent.type === desiredFilterType) {
                filterComponentsArray.push(filterComponent);
            }
        });
    }

    return filterComponentsArray;
}


function getOptions2FromWebFacetSection(filterType, filterUrlParam) {

    options2List = [];
    webFacetSections.forEach(getOptions2List);

    function getOptions2List(webFacetSection, index) {

        var filterComponents = webFacetSection.filterComponents;

        // Retrieve the specific optionList and their contents
        filterComponents.forEach(function(filterComponent) {

            if (filterComponent.type === filterType) {

                if (filterComponent.urlParam === filterUrlParam){

                    options2List = filterComponent.options2;
                }
            }

        });
    }
    return options2List;
}



function selectAllOptionsInMyComponent2(myContent, filterType, myComponent2Id,  options2List) {

    var newOptions = "";

    if (filterType === 'TwoParamLinkedFilter'){

        filterUrlParam = myComponent2Id.split('_')[0];
        options2Lista = getOptions2FromWebFacetSection(filterType, filterUrlParam);

        options2List = options2Lista[myContent.value];

    }else{

        // Remove the opening and closing square brackets
        options2List = options2List.substr(1).slice(0, -1);

        // Convert the resulting string array type
        options2List = options2List.split(",");
    }

    //Build a new Select > Option content with selected attribute
    for (var i = 0; i < options2List.length; i++) {
        newOptions += "<option value='" + options2List[i] + "' selected>" + options2List[i] + "</option>";
    }

    // Empty and reload the select Box with the selected options
    var select = $('#' + myComponent2Id);
    select.empty().append(newOptions);

    $(function () {
        $('#' + myComponent2Id).change(function () {
            //console.log($(this).val());
        }).multipleSelect({
            placeholder: ""
        });
    });
}


function displayMore(divId, startIndex) {


    for (var i = startIndex; i <= 19; i++) {

        var hiddenDiv = divId + '_hidden' + i;
        var textComponentId = divId + i;

        var hiddenDivDOM = document.getElementById(hiddenDiv);
        var textComponentDOM = document.getElementById(textComponentId);

        if (hiddenDivDOM.style.display === "none") {

            hiddenDivDOM.style.display = "block";
            textComponentDOM.value = "";
            break;
        }

    }
}


/****************************************************************
 *         MULTI PARAM FILTER SECTION ENDS             *
 ****************************************************************/


var civicDBGeneCache = {};
var civicDBVariantCache = {};

function formatCivicGeneLink(symbol, el, data) {
    var gene_id = data.result[0].gene_id;
    var civic_url = "https://civicdb.org/events/genes/" + gene_id + "/summary#gene";
    $(el).html('<a target="_blank" href="' + civic_url + '">' + symbol + ' <i class="icon icon-generic" data-icon="x"> </i></a>');
}

// Create links to the CivicDB resource from all genes shown in the mol char table
function linkGeneCivicdb(el, symbol) {
    if (symbol in civicDBGeneCache) {
        console.log("gene cache hit for " + symbol);
        // In cache already
        var data = civicDBGeneCache[symbol];
        if (data.result.length > 0) {
            formatCivicGeneLink(symbol, el, data);
        }
    } else {
        $.ajax({
            dataType: "json",
            url: "https://civicdb.org/api/variants/typeahead_results?limit=1&query=" + symbol,
            success: function (data) {
                civicDBGeneCache[symbol] = data;
                if (data.result.length > 0) {
                    formatCivicGeneLink(symbol, el, data);
                }
            },
            error: function () {
                civicDBGeneCache[symbol] = {'result': []};
            }
        });
    }
}

// Create links to the CivicDB resource from all variants shown in the mol char table
function linkVariantCivicdb(el, symbol) {
    $.ajax({
        dataType: "json",
        url: "https://civicdb.org/api/variants/typeahead_results?limit=1&query="+symbol,
        success: function(data){
            civicDBVariantCache[symbol] = data;
            if (data.result.length > 0) {
                var gene_id = data.result[0].gene_id;
                var variant_id = data.result[0].variant_id;
                var civic_url = "https://civicdb.org/events/genes/" + gene_id + "/summary/variants/" + variant_id + "/summary#variant";
                $(el).html('<a target="_blank" href="' + civic_url + '">' + symbol + ' <i class="icon icon-generic" data-icon="x"> </i></a>');
            }
        },
        error: function() {
            civicDBVariantCache[symbol] = {'result':[]};
        }
    });
}

// Function to interrogate civicDB to create the gene and variant links
function linkCivicdb(){
    console.log("Re-establishing civic DB links")
    var geneIndex = $('#molcharDataTable th:contains("HGNC Symbol")').index() + 1;
    var variantIndex = $('#molcharDataTable th:contains("Amino Acid Change")').index() + 1;
    var genes = $('#molcharDataTable tr td:nth-child(' + geneIndex + ')');
    console.log("genes:" + genes)
    var variants = $('#molcharDataTable tr td:nth-child(' + variantIndex + ')');
    console.log("variants:" + variants)
    genes.each(function() {
        var symbol = $(this).html();
        linkGeneCivicdb(this, symbol);
    });
    variants.each(function() {
        var symbol = $(this).html();
        linkVariantCivicdb(this, symbol);
    });
}


function getMolecularDataTable(clickedLink, clickedData){


    console.log(clickedLink);
    var idcomp = clickedLink.split("___");
    var titlecomp = clickedData.split("|");
    var id = idcomp[1];

    var url = "/data/getmoleculardata/"+id;

    var $targetDiv = jQuery('#variationTableData');
    $targetDiv.empty();

    $('#preLoader').show();

    fetch(url)
        .then(response => response.json())
        .then(json => displayMolecularDataTable(json, titlecomp))
        .catch(error => console.log(error))

}



function displayMolecularDataTable(tableData, clickedData){

    console.log("Headers length:"+tableData["tableHeaders"].length);

    var $targetDiv = jQuery('#variationTableData');

    var $table = jQuery('<table id="molcharDataTable" class="datatable-pdx pdx-table table-borderedPdx head-left" data-tabs id="example-tabs"/>');
    var $thead = jQuery('<thead/>');
    var $theadRow = jQuery('<tr>');
    var $tbody = jQuery('<tbody />');

    //add headers to table
    for(var i=0; i<tableData["tableHeaders"].length;i++){

        var $th = jQuery('<th>'+tableData["tableHeaders"][i]+'</th>');
        $theadRow.append($th);
    }

    
    //add datarows to table
    var rowCount = tableData["tableRows"].length;

    for(var j=0; j<rowCount; j++){

        $tr = jQuery('<tr class="tabs-title" style="float:none; text-transform: capitalize;">/');

        for(var k=0; k<tableData["tableRows"][j].length; k++){
            console.log("Rows "+tableData["tableRows"][j].length);
            $tr.append("<td>"+tableData["tableRows"][j][k]+"</td>");
        }
        
        $tbody.append($tr);
    }
    $thead.append($theadRow);
    $table.append($thead);
    $table.append($tbody);
    $targetDiv.append($table);


    customizeDatatable('molcharDataTable', clickedData[4]);


    $("#omicDataCount").html(rowCount);
    $("#clickedSampleId").html(clickedData[0]);
    $("#modelHistology").html(clickedData[1]);
    $("#clickedTumorType").html(clickedData[2]);
    $("#clickedPassage").html(clickedData[3]);
    $("#clickedTech").html(clickedData[4]);

    $('#hrTitle').attr('data-content', clickedData[4]);

    $('#preLoader').hide();


}




function customizeDatatable(dTable, presentData){

    $('#'+dTable).DataTable(
        {
            drawCallback : linkCivicdb(),
            info:     false,
            dom: '<"top"i>rt<"bottom"flp><"clear">',
            language: {
                lengthMenu: ' <select style="margin-top: 20px;">'+
                    '<option value="10">10 Data Entries </option>'+
                    '<option value="20">20 Data Entries </option>'+
                    '<option value="30">30 Data Entries </option>'+
                    '<option value="40">40 Data Entries </option>'+
                    '<option value="50">50 Data Entries </option>'+
                    '<option value="-1">Show All Entries </option>'+
                    '</select>'
            }
        }
    );

    $(".dataTables_filter").hide();
    $("#customSearch").html('<input style="height: 41px;" type="text" id="omicSearch" placeholder="Search'+presentData+' data">');

    oTable = $('#molcharDataTable').DataTable();
    $('#omicSearch').keyup(function(){
        oTable.search($(this).val()).draw();
    })

}