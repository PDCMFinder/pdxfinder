import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormGroup} from "@angular/forms";

declare var swal: any;

@Component({
    selector: 'app-manage',
    templateUrl: './curation-manage.component.html',
    styles: [``]
})
export class CurationManageComponent implements OnInit {

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
    public selectedEntityType: string;
    public selectedSrc: any;
    public showNotif: boolean = false;
    public showFilter: boolean = false;

    public pageSize;
    public pageOptions = ['2', '3', '5', '10', '15', '20', '25'];
    public userPage: number;

    public queryParamData: any;
    private mappingStatus: any;
    public pageOptionSize: string;


    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {
    }

    ngOnInit() {


        // From the current url snapshot, get the source parameter and assign to the dataSource property

        this.route.queryParamMap.subscribe(
            params => {


                this.manageCuratedData(this.userPage, this.pageSize, this.entityType, status, this.dataSource);

            }
        )


        // Load Fab Scripts
        this.gs.loadScript('../pdxfinder/dependencies/fab.js');
    };


    manageCuratedData(page, size, type, status, source) {

        this.columnHeaders = [];
        this.mappings = [];

        this._mappingService.getManagedTerms(type, source, page, size, status)
            .subscribe(
                data => {

                    this.data = data;

                    // This receives the mappings node of the json in required format
                    let mappings = this.data.mappings;

                    console.log(mappings);

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









}
