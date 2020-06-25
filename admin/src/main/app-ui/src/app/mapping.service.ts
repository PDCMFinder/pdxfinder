import { Injectable } from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpRequest, HttpEvent, HttpHeaders} from "@angular/common/http";
import {Mapping, MappingInterface} from "./mapping-interface";
import {Observable, Subject, throwError} from "rxjs/index";
import {catchError} from "rxjs/internal/operators";
import {SummaryInterface} from "./summary-interface";

@Injectable({
  providedIn: 'root'
})
export class MappingService {

    private devServer = "http://ves-ebi-bc.ebi.ac.uk:8081";
    private serverUrl = "http://localhost:8081"; // this.devServer; //

    private _summaryUrl = this.serverUrl+"/api/mappings/summary";
    private _mappingsUrl = this.serverUrl+"/api/mappings";
    public _exportUrl = this.serverUrl+"/api/mappings/export";

    private _uploadURL = this.serverUrl+"/api/mappings/uploads";


    public dataSubject = new Subject<any>();

    public stringDataBusSubject = new Subject<any>();

    public eventDataSubject = new Subject<any>();

    public errorReport = null;

    constructor(private http: HttpClient) { }


    getCurationSummary(maptype: string): Observable<SummaryInterface[]>{

        var curationType = (maptype == null) ? '' : `?entity-type=${maptype}`;

        const url = `${this._summaryUrl}${curationType}`;

        return this.http.get<SummaryInterface[]>(url);
    }



    getTerms(status: string, entityType: string, dataSource: string, page: string, size: string): Observable<MappingInterface[]>{

        const url = `${this._mappingsUrl}?mq=datasource:${dataSource}&entity-type=${entityType}&status=${status}&page=${page}&size=${size}`;

        return this.http.get<MappingInterface[]>(url);
    }



    getUnmappedTermsByType(entityType: string): Observable<MappingInterface[]>{

        const url = `${this._mappingsUrl}?entity-type=${entityType}&status=unmapped`;

        return this.http.get<MappingInterface[]>(url);
    }


    getTermsByStatus(status: string): Observable<MappingInterface[]>{

        const url = `${this._mappingsUrl}?status=${status}`;

        return this.http.get<MappingInterface[]>(url);
    }



    getManagedTerms(entityType: string, dataSource: string, page: string, size: string, status: string): Observable<MappingInterface[]>{

        var dsQuery = "";
        if (dataSource != null){
            dsQuery = `&mq=datasource:${dataSource}`;
        }

        const url = `${this._mappingsUrl}?entity-type=${entityType}&page=${page}&size=${size}&status=${status}${dsQuery}`;

        //console.log(url);

        return this.http.get<MappingInterface[]>(url);
    }


    getMappingEntityById(entityId: string): Observable<Mapping>{

        let url = `${this._mappingsUrl}/${entityId}`;

        return this.http.get<Mapping>(url);
    }

    getOLS(entityId: string): Observable<any>{

        let url = `${this._mappingsUrl}/ontologies?type=${entityId}`;

        return this.http.get<any>(url);
    }


    componentsDataBus(data): void{
        this.dataSubject.next(data);
    }



    stringDataBus(data): void{
        this.stringDataBusSubject.next(data);
    }


    eventDataBus(data): void{
        this.eventDataSubject.next(data);
    }


    updateEntity (mappings) {

        return this.http.put<any>(this._mappingsUrl, mappings)
            .pipe(catchError(this.errorHandler));
    }



    errorHandler2(error: HttpErrorResponse) {

        this.errorReport = error;

        return throwError(error);
    }


    errorHandler(error) {

        let errorMessage = '';
        if (error.error instanceof ErrorEvent) {
            // client-side error
            errorMessage = `Error: ${error.error.message}`;
        } else {
            // server-side error
            errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }

        return throwError(error);
    }



    pushFileToStorage(file: File, entityType: string): Observable<HttpEvent<{}>> {

        const formdata: FormData = new FormData();
        formdata.append('uploads', file);

        const url = `${this._uploadURL}?entity-type=${entityType}`;

        return this.http.post<any>(url, formdata)
            .pipe(
                catchError(this.errorHandler)
            );

    }



    connectToDataFlow() {

        return fetch('http://localhost:8081/api/mapping/diagnosis?ds=JAX')
            .then((res) => res.json())
            .then((data) => data)
            .catch(error => console.log(error));
    }



}







