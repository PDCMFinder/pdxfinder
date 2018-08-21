import { Component, OnInit } from '@angular/core';
import {MappingService} from "../mapping.service";
import {GeneralService} from "../general.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styles: [``]
})
export class DashboardComponent implements OnInit {

  constructor(private gs: GeneralService) { }

  ngOnInit() {



      this.gs.loadScript('../pdxfinder/dependencies/chart/amcharts.js');
      this.gs.loadScript('../pdxfinder/dependencies/chart/serial.js');
      this.gs.loadScript('../pdxfinder/dependencies/chart/export.min.js');
      this.gs.loadScript('../pdxfinder/dependencies/chart/light.js');
      this.gs.loadScript('../pdxfinder/dependencies/chart/3dbar.js');
  }

}
