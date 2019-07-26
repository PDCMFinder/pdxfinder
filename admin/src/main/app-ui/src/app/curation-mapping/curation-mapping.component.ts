import { Component, OnInit } from '@angular/core';
import {MappingService} from "../mapping.service";

@Component({
  selector: 'app-curation-mapping',
  templateUrl: './curation-mapping.component.html',
  styles: [``]
})

export class CurationMappingComponent implements OnInit {

    public unmappedDiagnosisCnt: number;
    public unmappedTreatmentCnt: number;

    constructor(private _mappingService: MappingService) { }

    ngOnInit() {

        this._mappingService.getUnmappedDiagnosis()
            .subscribe(
                data => {
                    this.unmappedDiagnosisCnt = data['totalElements'];
                }
            );

        this._mappingService.getUnmappedTreatment()
            .subscribe(
                data => {
                    this.unmappedTreatmentCnt = data['totalElements'];
                }
            );
    }
}




/*

Previous way of counting unmmapped diagnosis terms
                    let myData = data["mappings"]; // This recieves the mappings node of the json in required format
                    // Transform all d mappingValues node objects of each json to array format
                    var count:number = 0;
                    for (var i of myData) {
                        myData[count].mappingValues = Array.of(myData[count].mappingValues);
                        count++;
                    }
 */