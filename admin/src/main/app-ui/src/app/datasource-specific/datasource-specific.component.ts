import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {MappingInterface} from "../mapping-interface";
import {GeneralService} from "../general.service";

@Component({
    selector: 'app-datasource-specific',
    templateUrl: './datasource-specific.component.html',
    styles: [``]
})
export class DatasourceSpecificComponent implements OnInit {

    public data;
    public mappings = [];

    public dataSource;
    public entityType;
    public entityTypeUrl;

    public dataExists = false;
    public dataLabels;
    public columnHeaders = [];

    public selectedRow: Number;
    public setClickedRow: Function;
    public selectedEntity: String;
    public errorMsg = "";

    public pageRange: number[];

    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {

        // This will allow navigation to respond param changes on thesame route path
        //this.router.routeReuseStrategy.shouldReuseRoute = () => false;
    }
    ngOnInit() {

        // From the current url snapshot, get the source parameter and assign to the dataSource property
        this.dataSource = this.route.snapshot.paramMap.get('source');
        this.entityTypeUrl = this.route.snapshot.paramMap.get('mapType');
        this.entityType = this.entityTypeUrl.split('-')[0];

        var page = this.route.snapshot.paramMap.get("page");
        var size = this.route.snapshot.paramMap.get("size");

        // If no page value submitted, set page value as first page
        page = (page == null) ? "1" : page;

        // If no size value submitted, set size value as five
        size = (size == null) ? "5" : size;

        this.route.queryParamMap.subscribe(
            params => {

            }
        )

        this.getUnmappedTerms(page, size);






        this.setClickedRow = function (index, entityId) {
            this.selectedRow = index;
            this.selectedEntity = entityId;
        }


        this._mappingService.dataSubject.subscribe(
            data => {

                console.log(data);

                for (var i = 0; i < this.mappings.length; i++) {

                    if (this.mappings[i].entityId == this.selectedEntity) {

                        this.mappings[i].mappedTermLabel = data.mappedTermLabel.toUpperCase();
                        this.mappings[i].mapType = data.mapType.toUpperCase();
                        this.mappings[i].justification = data.justification.toUpperCase();

                    }

                }

            }
        )


    };


    getUnmappedTerms(page, size){

        this._mappingService.getUnmappedTerms(this.entityType, this.dataSource, page, size)
            .subscribe(
                data => {

                    this.data = data;

                    // This receives the mappings node of the json in required format
                    let mappings = this.data.mappings;

                    console.log(this.data)

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
                    // console.log(this.pageRange);


                    var count: number = 0;
                    for (var i of mappings) {

                        if (mappings[count].mappingValues.DataSource.toUpperCase() === this.dataSource.toUpperCase()) {
                            this.mappings.push(mappings[count]);
                        }
                        count++;
                    }

                }
            );
    }

    showSuggestedMappings(id) {

        // this.router.navigate([`../${this.dataSource}`],{relativeTo: this.route, queryParams: {page: page}} )
        this.router.navigate(['suggested-mappings'], {relativeTo: this.route})
    }

    submitCuration() {

        console.clear();

        let curatedMappings: any = this.mappings;
        let mappingObject: MappingInterface = <MappingInterface>curatedMappings;

        this._mappingService.submitCuration(mappingObject)
            .subscribe(
                response => console.log('Success!', response),
                error => this.errorMsg = error.statusText
            )

        console.log(this.mappings);
    }


}
