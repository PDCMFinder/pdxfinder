import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {enterView} from "@angular/core/src/render3/instructions";
import {Mapping} from "../mapping-interface";

@Component({
    selector: 'app-datasource-specific-suggestions',
    templateUrl: './datasource-specific-suggestions.component.html',
    styles: [``]
})
export class DatasourceSpecificSuggestionsComponent implements OnInit {

    private entityId;
    private selectedEntity = {};

    private dataLabels;
    private columnHeaders = [];

    private data = {
        DataSource: "",
        SampleDiagnosis : "",
        TumorType : "",
        OriginTissue : ""
    }

    private clickedSuggestionId: number;
    private showClickedDetails: boolean = false;
    private selectedSuggestion: Mapping;
    private clickedDetails;

    private olsUrl = 'https://www.ebi.ac.uk/ols/ontologies/ncit/terms?iri=';

    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService) { }

    ngOnInit() {


        // From the current url snapshot, get the source parameter and assign to the dataSource property
        this.route.params.subscribe(
            params => {


                this.toggleDetails(false);

                this.entityId = params['id'];

                // Retrieve the details of Mapping node with this entityId:
                this._mappingService.getMappingEntityById(this.entityId)
                    .subscribe(
                        data => {

                            this.selectedEntity = data;
                            this.data = data.mappingValues;


                            // Transfer mappingLabel for this entityType to the template
                            this.dataLabels = data.mappingLabels;

                            // Convert mapping Labels from CamelCase to Normal Case for Column Headers in Template
                            this.columnHeaders = [];
                            this.dataLabels.forEach((mappingLabel) => {
                                this.columnHeaders.push(mappingLabel.replace(/([a-z])([A-Z])/g, '$1 $2'));
                            });

                            this.sendDataToParent(this.selectedEntity);
                        }
                    );

            }
        )
    }

    sendDataToParent(data){
        this._mappingService.stringDataBus(data);
    }


    onSuggestionSubmit(suggestion){
        this._mappingService.componentsDataBus(suggestion);
    }


    getClickedSuggestion(suggestion: Mapping) {

        console.log(suggestion);


        this.clickedSuggestionId = suggestion.entityId;
        this.selectedSuggestion = suggestion;


        this.clickedDetails = (suggestion.entityType == 'diagnosis') ?
            suggestion.mappingValues.SampleDiagnosis : suggestion.mappingValues['TreatmentName'];


        this.toggleDetails(true);

        this._mappingService.eventDataBus('closeParentDetails');

    }


    toggleDetails(value: boolean) {

        this.showClickedDetails = value;
    }


}