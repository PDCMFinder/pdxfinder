import { Component, OnInit } from '@angular/core';
import {MappingService} from "../mapping.service";

@Component({
  selector: 'app-curation-mapping',
  templateUrl: './curation-mapping.component.html',
  styles: [``]
})

export class CurationMappingComponent implements OnInit {

  public mappingData = [];

  constructor(private _mappingService: MappingService) { }

  ngOnInit() {
      this._mappingService.getMappings()
          .subscribe(data => this.mappingData = data);
  }

}
