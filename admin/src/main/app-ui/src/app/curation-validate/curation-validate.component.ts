import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface, MappingValues} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormGroup} from "@angular/forms";

declare var swal: any;


@Component({
  selector: 'app-curation-validate',
  templateUrl: './curation-validate.component.html',
  styles: [``]
})
export class CurationValidateComponent implements OnInit {

  public data;
  public mappings = [];

  public dataSource;
  public entityType;

  public dataExists = false;
  public dataLabels;
  public columnHeaders = [];

  public selectedRow;
  public selectedEntity: any;
  public report = null;

  public pageRange: number[];

  // Selected Fields
  public selectedDetails: any;
  public selectedEntityId: any;
  public selectedEntityType: string;
  public selectedSrc: any;

  public showNotif: boolean = false;
  public showFilter: boolean = false;

  public pageSize;
  public pageOptions = ['2', '3', '5', '10', '15', '20', '25'];
  public userPage: number;

  public mappingStatus: any;
  public pageOptionSize: string;

  public dataTypes = [];

  constructor(private router: Router,
              private route: ActivatedRoute,
              private _mappingService: MappingService,
              private gs: GeneralService) {
  }

  ngOnInit() {

    this.getDataTypes();

    // From the current url snapshot, get the source parameter and assign to the dataSource property
    this.route.paramMap.subscribe(
        params => {

          var page = params.get('page');
          var type = localStorage.getItem('_entityType');
          var size = localStorage.getItem('_pageSize');
          var status = 'orphaned';
          var source = null;

          this.pageSize = size;
          this.mappingStatus = status;
          this.dataSource = source;

          this.userPage = (page == null) ? 0 : parseInt(page);

          // If no page value submitted, set page value as §first page
          page = (page == null) ? "1" : page;
          size = (size == null) ? "5" : size;
          type = (type == null) ? "diagnosis" : type;

          this.pageOptionSize = size;
          this.entityType = type;


          this.manageOrphanedData(page, size, type, status, source);
        }
    )


  };


  manageOrphanedData(page, size, type, status, source) {

    this.columnHeaders = [];
    this.mappings = [];


    this._mappingService.getManagedTerms(type, source, page, size, status)
        .subscribe(
            data => {

              this.data = data;

              console.log(this.data.totaPages);

              // This receives the mappings node of the json in required format
              let mappings = this.data.mappings;

              //console.log(mappings);

              // Build Column Headers If data is not empty
              if (mappings.length > 0) {

                // Transfer mappingLabel for this entityType to the template
                this.dataLabels = mappings[0].mappingLabels;

                // Convert mapping Labels from CamelCase to Normal Case for Column Headers in Template
                this.dataLabels.forEach((mappingLabel) => {

                  this.columnHeaders.push(mappingLabel.replace(/([a-z])([A-Z])/g, '$1 $2'));
                });

                this.dataExists = true;
              }

              this.pageRange = this.gs.getNumbersInRange(this.data.beginIndex, this.data.endIndex);

              this.mappings = mappings;

            }
        );
  }



  getDataTypes(){

    var entityTypes =  ['diagnosis', 'treatment'];

    entityTypes.forEach((entity, index) => {
      this.dataTypes.push(
          {id: index, text: entity, checked: false}
      )
    })

  }










}
