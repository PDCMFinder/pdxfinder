import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styles: [``]
})
export class DashboardComponent implements OnInit {

  constructor() { }

  ngOnInit() {



      this.loadScript('../pdxfinder/dependencies/chart/amcharts.js');
      this.loadScript('../pdxfinder/dependencies/chart/serial.js');
      this.loadScript('../pdxfinder/dependencies/chart/export.min.js');
      this.loadScript('../pdxfinder/dependencies/chart/light.js');
      this.loadScript('../pdxfinder/dependencies/chart/3dbar.js');
  }

    public loadScript(url: string) {
        const body = <HTMLDivElement> document.body;
        const script = document.createElement('script');
        script.innerHTML = '';
        script.src = url;
        script.async = false;
        script.defer = true;
        body.appendChild(script);
    }

}
