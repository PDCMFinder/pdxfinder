import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class MappingService {

  private _url = "/api/missingmapping/diagnosis";

  constructor(private http: HttpClient) { }

    //Retrieve mapping data
    getMappings(){


    }
}
