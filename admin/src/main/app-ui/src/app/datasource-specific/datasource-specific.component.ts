import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormControl} from "@angular/forms";

declare var swal: any;
declare var $: any;

@Component({
    selector: 'app-datasource-specific',
    templateUrl: './datasource-specific.component.html',
    styles: [``]
})
export class DatasourceSpecificComponent implements OnInit {

    private data;
    private mappings = [];

    private dataSource;
    private entityType;
    private entityTypeUrl;

    private dataExists = false;
    private dataLabels;
    private columnHeaders = [];

    private report = null;

    private pageRange: number[];

    // Selected Fields
    private selectedEntity;
    private selectedRow;
    private selectedEntityId: any;
    private selectedDetails: any;
    private selectedEntityType: string;
    private selectedURL: string;
    private selectedSrc: any;
    private olsTermSelected: boolean = false;
    private showNotif: boolean = false;

    private pageSize;
    private pageOptions = ['2', '3', '5', '10', '15', '20', '25'];
    private userPage: number;

    options: string[] = ['One', 'Two', 'Three'];

    private olsUrl = 'https://www.ebi.ac.uk/ols/ontologies/ncit/terms?iri=';
    private autoSuggestTextBox: string;

    private diagnosisOntology = [];
    private treatmentOntology = [];

    private mappingStatusToGet;


    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {

        // This will allow navigation to respond param changes on thesame route path
        // This.router.routeReuseStrategy.shouldReuseRoute = () => false;
    }

    ngOnInit() {

        // From the current url snapshot, get the source parameter and assign to the dataSource property
        this.dataSource = this.route.snapshot.paramMap.get('source');
        this.entityTypeUrl = this.route.snapshot.paramMap.get('mapType');
        this.entityType = this.entityTypeUrl.split('-')[0];

        var page = this.route.snapshot.paramMap.get("page");
        var size = localStorage.getItem('_pageSize');

        this.route.paramMap.subscribe(
            params => {

                this.mappingStatusToGet = params.get('page').split('-')[0];
                page = params.get('page').split('-')[1];

                // If no page value submitted, set page value as first page
                page = (page == null) ? "1" : page;
                this.userPage = parseInt(page);

                // If no size value submitted, set size value as five
                //size = (size == null) ? "5" : this.pageSize;

                this.getUnmappedTerms(page);
            }
        )


        console.log(this.mappingStatusToGet);

        // Get Data from Child Component
        this._mappingService.dataSubject.subscribe(
            data => {

                for (var i = 0; i < this.mappings.length; i++) {

                    if (this.mappings[i].entityId == this.selectedEntityId) {

                        this.mappings[i].mappedTermLabel = data.mappedTermLabel.toUpperCase();
                        this.mappings[i].mapType = data.mapType.toUpperCase();
                        this.mappings[i].justification = data.justification.toUpperCase();
                        this.mappings[i].mappedTermUrl = data.mappedTermUrl;

                    }
                }
            }
        )

        // Get String Data from Child Component :
        // This data is sent to the parent on load, so it allows parent data Row to be selected when deeplink url is visited
        this._mappingService.stringDataBusSubject.subscribe(
            data => {
                this.getClickedRow(data);
            }
        )


        this._mappingService.eventDataSubject.subscribe(
            data => {

                if(data == 'closeParentDetails'){
                    // this.showNotif = false;
                }
            }
        )



        this.getOLSTerms(this.entityType.toLowerCase());

    };





    getOLSTerms(entityType) {

        this._mappingService.getOLS(entityType)
            .subscribe(
                data => {

                    console.log(data);

                    if (entityType == 'diagnosis'){

                        this.diagnosisOntology = data;
                    }else {

                        this.treatmentOntology = data;
                    }

                    // transfer data out of observable
                    // localStorage.setItem('thisMapping', JSON.stringify(this.mappings));
                }
            );

    }




    getUnmappedTerms(page) {

        this.pageSize = localStorage.getItem('_pageSize') == null ? 5 : localStorage.getItem('_pageSize');

        this.toggleNotification(false);

        this.columnHeaders = [];
        this.mappings = [];

        this._mappingService.getTerms(this.mappingStatusToGet, this.entityType, this.dataSource, page, this.pageSize)
            .subscribe(
                data => {

                    this.data = data;

                    // This receives the mappings node of the json in required format
                    let mappings = this.data.mappings;

                    // Build Column Headers If data is not empty
                    if (mappings.length > 0) {

                        // Transfer mappingLabel for this entityType to the template
                        this.dataLabels = mappings[0].mappingLabels;

                        // Convert mapping Labels from CamelCase to Normal Case for Column Headers in Template
                        this.dataLabels.forEach((mappingLabel) => {

                            this.columnHeaders.push(mappingLabel.replace(/([a-z])([A-Z])/g, '$1 $2'));
                        });

                        this.dataExists = true;
                    }

                    this.pageRange = this.gs.getNumbersInRange(this.data.beginIndex, this.data.endIndex);

                    var count: number = 0;
                    for (var i of mappings) {

                        if (mappings[count].mappingValues.DataSource.toUpperCase() === this.dataSource.toUpperCase()) {
                            this.mappings.push(mappings[count]);
                        }
                        count++;
                    }

                    // transfer data out of observable
                    // localStorage.setItem('thisMapping', JSON.stringify(this.mappings));
                }
            );


    }



    ontologySuggest(entityType){

        var presentOntology = (entityType == 'diagnosis') ? this.diagnosisOntology : this.treatmentOntology;

        var data = this.mappings;
        var selectedId = this.selectedEntityId;

        var componentSelector = 'autocomplete';

        var dataArray = presentOntology.map(
            (data) => {
                return { value: data.label, data: data };
            }
        );

        // Initialize autocomplete:
        $(`.${componentSelector}`).autocomplete({

            lookup: dataArray,
            lookupFilter: function(suggestion, originalQuery, queryLowerCase) {
                var re = new RegExp('\\b' + $.Autocomplete.utils.escapeRegExChars(queryLowerCase), 'gi');
                return re.test(suggestion.value);
            },
            onSelect: (suggestion) => {

                for (var i = 0; i < this.mappings.length; i++) {

                    if (this.mappings[i].entityId == this.selectedEntityId) {

                        this.mappings[i].mappedTermLabel = suggestion.data.label;
                        this.mappings[i].mappedTermUrl = suggestion.data.url;
                        this.mappings[i].mapType = 'Inferred';
                        this.mappings[i].justification = 'Manual Curation';

                        this.selectedURL = suggestion.data.url;
                        this.olsTermSelected = true;
                    }
                }

            }
        });

    }


    getClickedRow(mapping: Mapping) {

        this.selectedEntity = mapping;

        this.selectedEntityId = mapping.entityId;
        this.selectedRow = mapping.entityId;

        this.selectedDetails = (mapping.entityType == 'diagnosis') ?
            mapping.mappingValues.SampleDiagnosis : mapping.mappingValues['TreatmentName'];

        this.selectedSrc = mapping.mappingValues.DataSource;
        this.selectedEntityType = mapping.entityType;


        this.autoSuggestTextBox = (mapping.mappedTermLabel === '-') ? '' : mapping.mappedTermLabel;
        this.selectedURL = mapping.mappedTermUrl;
        this.olsTermSelected = false;

        this.toggleNotification(true);

    }




    toggleNotification(value: boolean) {

        this.showNotif = value;
    }


    toggleReport(value: string) {

        this.report = null;

        if (value == 'success') {
            setTimeout(() => {
                this.refreshPage()
            }, 1000)
        }
    }

    newPageSize(pageSize) {

        localStorage.setItem('_pageSize', pageSize);

        //  Auto-Navigate away on page size change
        let newPage = (this.userPage <= 1) ? this.userPage + 1 : 1;

        this.router.navigate([`curation/${this.entityTypeUrl}/${this.dataSource}/${this.mappingStatusToGet}-${newPage}`])

    }

    refreshPage() {

        //  Auto-Navigate away on page size change
        let newPage = (this.userPage <= 1) ? this.userPage + 1 : 1;

        this.router.navigate([`curation/${this.entityTypeUrl}/${this.dataSource}/${newPage}`])

    }



    updateSkippedTerm() {

        var skippedTerms = [];
        var skippedTerm = {};

        skippedTerm = Object.assign(skippedTerm, this.selectedEntity);

        // Update the selected entity before submission
        skippedTerm['suggestedMappings'] = [];
        skippedTerm['status'] = "orphaned";

        skippedTerms.push(skippedTerm);

        this.sendDataForUpdate(skippedTerms);
    }


    updateMappingEntity() {

        var validatedTerms = [];


        this.mappings.forEach((mapping) => {
            mapping.suggestedMappings = [];
            mapping.status = "created";

            if (mapping['mappedTermLabel'] != '-' && mapping['mappedTermUrl'] != null) {
                validatedTerms.push(mapping);
            }
        });

        console.log(validatedTerms);

        this.sendDataForUpdate(validatedTerms);
    }


    sendDataForUpdate(validatedTerms){

        swal({
                title: "Are you sure?",
                text: "You may not be able to reverse this operation",
                imageUrl: 'assets/icons/question.jpg',
                showCancelButton: true,
                confirmButtonColor: '#03369D',
                confirmButtonText: 'YES',
                cancelButtonText: "NO",
                closeOnConfirm: false,
                closeOnCancel: false
            },
            (isConfirm) => {

                if (isConfirm) {

                    this._mappingService.updateEntity(validatedTerms)
                        .subscribe(
                            response => {

                                this.report = "success";
                                this.showNotif = false;
                                swal("Submitted!", "Your curation has been submitted", "success");
                                // console.log(response)
                            },
                            error => {

                                this.report = "failed";
                                swal("Failed", " The curation is invalid :)", "error");
                                //console.log(error.ok, error)
                            }
                        );
                } else {
                    swal("Cancelled", "The request was cancelled :)", "error");
                }
            })

    }

}
