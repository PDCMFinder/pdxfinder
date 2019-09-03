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

    private autoSuggestTextBox: string;


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

                page = params.get('page');

                // If no page value submitted, set page value as first page
                page = (page == null) ? "1" : page;
                this.userPage = parseInt(page);

                // If no size value submitted, set size value as five
                //size = (size == null) ? "5" : this.pageSize;

                this.getUnmappedTerms(page);
            }
        )


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

    };


    getUnmappedTerms(page) {

        this.pageSize = localStorage.getItem('_pageSize') == null ? 5 : localStorage.getItem('_pageSize');

        this.toggleNotification(false);

        this.columnHeaders = [];
        this.mappings = [];

        this._mappingService.getUnmappedTerms(this.entityType, this.dataSource, page, this.pageSize)
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



    ontologySuggest(){

        var data = this.mappings;
        var selectedId = this.selectedEntityId;

        var componentSelector = 'autocomplete';

        var dataArray = this.dataList.map(
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
                        this.mappings[i].mapType = 'Direct';
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

        this.router.navigate([`curation/${this.entityTypeUrl}/${this.dataSource}/${newPage}`])

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

       // this.sendDataForUpdate(validatedTerms);
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


    dataList = [
        {
            "id": 41761,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3262",
            "label": "Cancer",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 639,
            "synonyms": [],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41762,
            "url": "http://purl.obolibrary.org/obo/NCIT_C4741",
            "label": "Neoplasm by Morphology",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 639,
            "synonyms": [
                "Neoplasm by Morphology"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41763,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3263",
            "label": "Neoplasm by Site",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 639,
            "synonyms": [
                "Neoplasm by Site"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41764,
            "url": "http://purl.obolibrary.org/obo/NCIT_C6974",
            "label": "Neoplasm of Uncertain Histogenesis",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Neoplasm of Uncertain Histogenesis",
                "Tumor of Uncertain Histogenesis",
                "Tumor of Uncertain Origin",
                "Neoplasm of Uncertain Origin"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41765,
            "url": "http://purl.obolibrary.org/obo/NCIT_C6971",
            "label": "Meningothelial Cell Neoplasm",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Tumor of Meningothelial Cells",
                "Meningothelial Cell Tumor",
                "Primary Meningeal Neoplasm",
                "Meningothelial Cell Neoplasm",
                "Primary Meningeal Tumor"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41766,
            "url": "http://purl.obolibrary.org/obo/NCIT_C6930",
            "label": "Mixed Neoplasm",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Mixed Neoplasm",
                "Mixed Tumor"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41767,
            "url": "http://purl.obolibrary.org/obo/NCIT_C27134",
            "label": "Hematopoietic and Lymphoid Cell Neoplasm",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Hematologic Cancer",
                "Hematopoietic and Lymphoid Neoplasms",
                "HEMOLYMPHORETICULAR TUMOR, MALIGNANT",
                "Hematopoietic Neoplasm",
                "Hematopoietic Neoplasms including Lymphomas",
                "Hematopoietic Cell Tumor",
                "Hematologic Malignancy",
                "Hematopoietic, Including Myeloma",
                "hematologic cancer",
                "Hematopoietic Cancer",
                "Hematologic Neoplasm",
                "Hematopoietic Tumor",
                "Hematopoietic malignancy, NOS",
                "Malignant Hematopoietic Neoplasm",
                "Hematological Tumor",
                "Hematological Neoplasm",
                "Malignant Hematologic Neoplasm",
                "Hematopoietic and Lymphoid Cell Neoplasm"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41768,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3786",
            "label": "Mesothelial Neoplasm",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Mesothelial Tumor",
                "Mesothelial Neoplasm"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41769,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3709",
            "label": "Epithelial Neoplasm",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 639,
            "synonyms": [
                "Epithelial Neoplasms, NOS",
                "Epithelioma",
                "Epithelial Neoplasm"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41770,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3708",
            "label": "Germ Cell Tumor",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Tumor of Germ Cell",
                "Tumor of the Germ Cell",
                "Neoplasm of Germ Cell",
                "Germ Cell Neoplasm",
                "germ cell tumor",
                "Neoplasm of the Germ Cell",
                "Germ Cell Tumor"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41771,
            "url": "http://purl.obolibrary.org/obo/NCIT_C3422",
            "label": "Trophoblastic Tumor",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Trophoblastic Neoplasms",
                "Trophoblastic Tumor",
                "Trophoblastic Neoplasm"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        },
        {
            "id": 41772,
            "url": "http://purl.obolibrary.org/obo/NCIT_C7068",
            "label": "Neoplastic Polyp",
            "directMappedSamplesNumber": 0,
            "indirectMappedSamplesNumber": 0,
            "synonyms": [
                "Neoplastic Polyp"
            ],
            "type": null,
            "description": null,
            "allowAsSuggestion": true,
            "subclassOf": null,
            "sampleMappedTo": null,
            "treatmentMappedTo": null
        }
    ];
}
