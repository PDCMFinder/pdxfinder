
import { Component, OnInit } from '@angular/core';
import {MappingService} from "../mapping.service";
import { from} from "rxjs/index";
import {groupBy, mergeMap, toArray} from "rxjs/internal/operators";
import {ActivatedRoute, Router} from '@angular/router';
import {GeneralService} from "../general.service";

@Component({
    selector: 'app-datasource-summary',
    templateUrl: './datasource-summary.component.html',
    styles: [``]
})
export class DatasourceSummaryComponent implements OnInit {

    constructor(private _mappingService: MappingService,
                private router: Router,
                private route: ActivatedRoute,
                private gs: GeneralService) { }

    public pdxStatArray =  {
        source: [],
        missing: [],
        total: [],
        validated: [],
        unvalidated: []
    };

    ngOnInit() {

        let tempArr = [];

        // Component to the Service using Reactive Observables
        this._mappingService.connectTotalMappedStream()
            .subscribe(
                data => {

                    // This receives the mappings node of the json in required format
                    let myData = data["mappings"];

                    // Emit Each Unmapped Diagnosis
                    const  source = from(myData);

                    // Group by Data Source
                    const groupedByImpl = source.pipe(
                        groupBy(missingMapping => missingMapping["mappingValues"].DataSource),
                        mergeMap(grouped => grouped.pipe(toArray())) // Return each item in the grouped array.
                    );

                    // Retrieve the data from the groupedByImpl (The Grouped By Implementation of the missing Mappings JSON)
                    const  subscribe = groupedByImpl.subscribe(
                        result =>  {

                            //console.log(result);
                            this.pdxStatArray.source.push(result[0]["mappingValues"].DataSource);
                            this.pdxStatArray.total.push(result.length);
                            this.pdxStatArray.validated.push(result.length);
                            this.pdxStatArray.unvalidated.push(0);
                        }
                    );

                }
            );


        this._mappingService.connectMissingMappingStream()
            .subscribe(
                data => {

                    // Group by Data Source
                    const groupedByImpl = from(data["mappings"]).pipe(
                        groupBy(missingMapping => missingMapping["mappingValues"].DataSource),
                        mergeMap(grouped => grouped.pipe(toArray())) // Return each item in the grouped array.
                    );

                    const  subscribe = groupedByImpl.subscribe(
                        result =>  {

                            tempArr.push(result[0]["mappingValues"].DataSource +"__"+result.length);

                            for (var i = 0;i < this.pdxStatArray.source.length; i++) {
                                for (var num = 0; num < tempArr.length; num++) {
                                    if ( tempArr[num].split("__")[0].toUpperCase() == this.pdxStatArray.source[i].toUpperCase() ){
                                        this.pdxStatArray.missing[i] = tempArr[num].split("__")[1]; break;
                                    }else{
                                        this.pdxStatArray.missing[i] = 0;
                                    }
                                }
                            }
                        }


                    );


                }
            );


        this.gs.loadScript('../pdxfinder/dependencies/chart/amcharts.js');
        this.gs.loadScript('../pdxfinder/dependencies/chart/serial.js');
        this.gs.loadScript('../pdxfinder/dependencies/chart/export.min.js');
        this.gs.loadScript('../pdxfinder/dependencies/chart/light.js');
        this.gs.loadScript('../pdxfinder/dependencies/chart/3dbar.js');
    }


    onSelect(source){

        this.router.navigate([source],{relativeTo: this.route})
    }


}