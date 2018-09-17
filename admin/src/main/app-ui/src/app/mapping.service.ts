import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {MappingInterface} from "./mapping-interface";
import {Observable, Subject, throwError} from "rxjs/index";
import {catchError} from "rxjs/internal/operators";

@Injectable({
  providedIn: 'root'
})
export class MappingService {

    //private _totalMappedUrl = "/api/mapping/diagnosis";
    //private _missingMappedUrl = "/api/missingmapping/diagnosis";

    private _totalMappedUrl = "/assets/data/mapped-diagnosis.json";
    private _missingMappedUrl = "/assets/data/unmapped-diag-slim.json";

    private _submitCurationUrl = "/api/diagnosis";

    public dataSubject = new Subject<any>();

    constructor(private http: HttpClient) { }

    //Retrieve Total mapped diagnosis
    connectTotalMappedStream(): Observable<MappingInterface[]>{

        return this.http.get<MappingInterface[]>(this._totalMappedUrl);

    }

    //Retrieve missing mapping diagnosis
    connectMissingMappingStream(): Observable<MappingInterface[]>{

        return this.http.get<MappingInterface[]>(this._missingMappedUrl);

    }

    componentsDataBus(data): void{
        this.dataSubject.next(data);
    }


    submitCuration (mappings: MappingInterface) {

        return this.http.post<any>(this._submitCurationUrl, mappings)
            .pipe(catchError(this.errorHandler));
    }


    errorHandler(error: HttpErrorResponse) {
        return throwError(error);
    }



}
