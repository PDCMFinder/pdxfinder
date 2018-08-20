import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";

@Component({
  selector: 'app-datasource-specific-suggestions',
  templateUrl: './datasource-specific-suggestions.component.html',
  styles: [``]
})
export class DatasourceSpecificSuggestionsComponent implements OnInit {

    public entityId = [];
    public selectedEntity = [];

    public data = {
        DataSource: "",
        SampleDiagnosis : "",
        TumorType : "",
        OriginTissue : ""
    }



    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService) { }

  ngOnInit() {

      // From the current url snapshot, get the source parameter and assign to the dataSource property
      this.route.params.subscribe(
          params => {

              this.entityId = params['id'];

              // Retrieve the details of Mapping node with this entityId:
              this._mappingService.connectMissingMappingStream()
                  .subscribe(
                      data => {

                          let myData = data["mappings"]; // This receives the mappings node of the json in required format

                          var count:number = 0;
                          for (var i of myData) {

                              if (myData[count].entityId.toString() === this.entityId.toString() ){
                                  this.selectedEntity = myData[count];
                                  this.data = myData[count].mappingValues;
                              } count++;
                          }
                      }
                  );





          }
      )
  }

    onSuggestionSubmit(suggestion){
        this._mappingService.componentsDataBus(suggestion);
    }

}
