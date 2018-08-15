import { Component, OnInit } from '@angular/core';
import { ActivatedRoute  } from "@angular/router";

@Component({
  selector: 'app-datasource-specific',
  templateUrl: './datasource-specific.component.html',
  styles: [``]
})
export class DatasourceSpecificComponent implements OnInit {

  public dataSource;
  constructor(private route: ActivatedRoute) { }

  ngOnInit() {

      // From the current url snapshot, get the source parameter and assign to the dataSource property
      this.dataSource = this.route.snapshot.paramMap.get('source');

  }

}
