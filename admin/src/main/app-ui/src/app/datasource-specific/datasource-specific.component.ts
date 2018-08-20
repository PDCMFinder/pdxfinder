import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";

@Component({
  selector: 'app-datasource-specific',
  templateUrl: './datasource-specific.component.html',
  styles: [``]
})
export class DatasourceSpecificComponent implements OnInit {

  public dataSource;
  public mappings = [];

  public selectedRow : Number;
  public setClickedRow : Function;
  public selectedEntity: String;


  constructor(private router: Router,
              private route: ActivatedRoute,
              private _mappingService: MappingService) { }

  ngOnInit() {

      // From the current url snapshot, get the source parameter and assign to the dataSource property
      this.dataSource = this.route.snapshot.paramMap.get('source');

      this._mappingService.connectMissingMappingStream()
          .subscribe(
              data => {

                  let myData = data["mappings"]; // This receives the mappings node of the json in required format

                  var count:number = 0;
                  for (var i of myData) {

                      if (myData[count].mappingValues.DataSource.toUpperCase() === this.dataSource.toUpperCase() ){
                          this.mappings.push(myData[count]);
                      }
                      count++;
                  }

              }
          );

      this.setClickedRow = function(index, entityId){
          this.selectedRow = index;
          this.selectedEntity = entityId;
      }


      this._mappingService.dataSubject.subscribe(
          data => {

              for (var i=0; i < this.mappings.length; i++) {

                  if (this.mappings[i].entityId == this.selectedEntity){

                      this.mappings[i].mappedTerm = data.mappedTerm.toUpperCase();
                      this.mappings[i].mapType = data.mapType.toUpperCase();
                      this.mappings[i].justification = data.justification.toUpperCase();

                  }
                  console.clear();
                  console.log(this.mappings);

              }

          }
      )


  };



  showSuggestedMappings(id){

      //this.router.navigate(['/suggested-mappings',id])
      this.router.navigate(['suggested-mappings'],{relativeTo: this.route})
  }

}
