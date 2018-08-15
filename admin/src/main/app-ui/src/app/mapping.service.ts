import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {MappingInterface} from "./mapping-interface";
import {Observable} from "rxjs/index";

@Injectable({
  providedIn: 'root'
})
export class MappingService {

    //private _totalMappedUrl = "/api/mapping/diagnosis";
    //private _missingMappedUrl = "/api/missingmapping/diagnosis";

    private _totalMappedUrl = "/assets/data/mapped-diagnosis.json";
    private _missingMappedUrl = "/assets/data/diagnosis.json";


    constructor(private http: HttpClient) { }

    //Retrieve Total mapped diagnosis
    connectTotalMappedStream(): Observable<MappingInterface[]>{

        return this.http.get<MappingInterface[]>(this._totalMappedUrl);

    }

    //Retrieve missing mapping diagnosis
    connectMissingMappingStream(): Observable<MappingInterface[]>{

        return this.http.get<MappingInterface[]>(this._missingMappedUrl);

    }





}
