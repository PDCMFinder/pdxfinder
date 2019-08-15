import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface, MappingValues} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormGroup} from "@angular/forms";

import { saveAs } from 'file-saver';

declare var swal: any;

@Component({
    selector: 'app-curation-orphan',
    templateUrl: './curation-orphan.component.html',
    styles: [``]
})
export class CurationOrphanComponent implements OnInit {

    public data;
    public mappings = [];

    public dataSource;
    public entityType;

    public dataExists = false;
    public dataLabels;
    public columnHeaders = [];

    public selectedRow;
    public selectedEntity: any;
    public report = null;

    public pageRange: number[];

    // Selected Fields
    public selectedDetails: any;
    public selectedEntityId: any;
    public selectedEntityType: string;
    public selectedSrc: any;

    public showNotif: boolean = false;
    public showFilter: boolean = false;

    public pageSize;
    public pageOptions = ['2', '3', '5', '10', '15', '20', '25'];
    public userPage: number;

    public mappingStatus: any;
    public pageOptionSize: string;

    public dataTypes = [];

    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {
    }

    ngOnInit() {

        this.getDataTypes();

        // From the current url snapshot, get the source parameter and assign to the dataSource property
        this.route.paramMap.subscribe(
            params => {

                var page = params.get('page');
                var type = localStorage.getItem('_entityType');
                var size = localStorage.getItem('_pageSize');
                var status = 'orphaned';
                var source = null;

                this.pageSize = size;
                this.mappingStatus = status;
                this.dataSource = source;

                this.userPage = (page == null) ? 0 : parseInt(page);

                // If no page value submitted, set page value as §first page
                page = (page == null) ? "1" : page;
                size = (size == null) ? "5" : size;
                type = (type == null) ? "diagnosis" : type;

                this.pageOptionSize = size;
                this.entityType = type;


                this.manageOrphanedData(page, size, type, status, source);
            }
        )


        // Return Selected Data from DatasourceSpecificSuggestionsComponent Child Component this parent component
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


        // Get String Data from Child Component : Allows parent data Row to auto-selected when deeplinked suggestion url is visited
        this._mappingService.stringDataBusSubject.subscribe(
            data => {

                this.getClickedRow(data);
            }
        )

        // Load Fab Scripts
        this.gs.loadScript('../pdxfinder/dependencies/fab.js');
    };


    manageOrphanedData(page, size, type, status, source) {

        this.columnHeaders = [];
        this.mappings = [];


        this._mappingService.getManagedTerms(type, source, page, size, status)
            .subscribe(
                data => {

                    this.data = data;

                    console.log(this.data.totaPages);

                    // This receives the mappings node of the json in required format
                    let mappings = this.data.mappings;

                    //console.log(mappings);

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

                    this.mappings = mappings;

                }
            );
    }



    getDataTypes(){

        var entityTypes =  ['diagnosis', 'treatment'];

        entityTypes.forEach((entity, index) => {
            this.dataTypes.push(
                {id: index, text: entity, checked: false}
            )
        })

    }


    // whenever filter is apllied doit as size dro down, reset page to 1
    newPageSize(pageSize) {

        localStorage.setItem('_pageSize', pageSize);

        //  Auto-Navigate away on page size change
        let newPage = (this.userPage <= 1) ? this.userPage + 1 : 1;

        this.router.navigate([`curation/orphan/${newPage}`])

    }




    searchFilter(form) {

        var filter = form.value;
        this.entityType = (filter.type != "") ? filter.type : this.entityType;
        this.mappingStatus = (filter.status != "") ? filter.status : this.mappingStatus;
        this.dataSource = (filter.source != "") ? filter.source : this.dataSource;


        // Capture Data Type Status Check Box
        var types = [];
        this.dataTypes.forEach((dType)=>{

            if (dType.checked == true) {
                types.push(dType.text);
            }
        })
        this.entityType = (types.length != 0) ? types.join() : this.entityType;

        localStorage.setItem('_entityType', this.entityType);

        this.refreshPage();
    }

    refreshPage() {

        //  Auto-Navigate away on page size change
        let newPage = (this.userPage == 0) ? "1" : "";

        this.router.navigate([`curation/orphan/${newPage}`])
    }


    toggleDisplay(compType: string) {

        if (compType == 'notif') {

            this.showNotif = (this.showNotif == true) ? false : true;

        }else if (compType == 'filter'){

            this.showFilter = (this.showFilter == true) ? false : true;
        }
    }




    getClickedRow(mapping: Mapping) {

        this.selectedEntity = mapping;

        this.selectedEntityId = mapping.entityId;
        this.selectedRow = mapping.entityId;

        this.selectedDetails = (mapping.entityType == 'diagnosis') ?
            mapping.mappingValues.SampleDiagnosis : mapping.mappingValues['TreatmentName'];

        this.selectedSrc = mapping.mappingValues.DataSource;
        this.selectedEntityType = mapping.entityType;

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




    updateMappingEntity() {

        var validatedTerms = [];


        this.mappings.forEach((mapping) => {
            mapping.suggestedMappings = [];
            mapping.status = "created";

            if (mapping['mappedTermLabel'] != '-' && mapping['mappedTermUrl'] != null) {
                validatedTerms.push(mapping);
            }
        });

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
