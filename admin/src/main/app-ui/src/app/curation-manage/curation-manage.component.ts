import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface, MappingValues} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormGroup} from "@angular/forms";


declare var swal: any;

@Component({
    selector: 'app-manage',
    templateUrl: './curation-manage.component.html',
    styles: [``]
})
export class CurationManageComponent implements OnInit {

    private data;
    private mappings = [];

    private dataSource;
    private entityType;

    private dataExists = false;
    private dataLabels;
    private columnHeaders = [];

    private selectedRow;
    private selectedEntity: any;
    private report = null;

    private pageRange: number[];

    // Selected Fields
    private selectedDetails: any;
    private showNotif: boolean = false;
    private showFilter: boolean = false;

    private pageSize;
    private pageOptions = ['2', '3', '5', '10', '15', '20', '25'];
    private userPage: number;

    private mappingStatus: any;
    private pageOptionSize: string;

    private dataTypes = [];
    private statusList = [];
    private providersList = [];

    private providersList2 = [];

    private csvURL = "";

    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {
    }

    ngOnInit() {

        this.getProvidersList();

        this.getDataTypes();

        this.getStatusList();

        // From the current url snapshot, get the source parameter and assign to the dataSource property

        this.route.queryParamMap.subscribe(
            params => {

                var page = params.get('page');
                var type = params.get('type');
                var size = params.get('size');
                var status = params.get('status');
                var source = params.get('source');

                // If no page value submitted, set page value as first page
                page = (page == null) ? "1" : page;
                this.userPage = parseInt(page);

                this.pageSize = size;
                this.entityType = type;
                this.mappingStatus = status;
                this.dataSource = source;


                // Sete default values incase no value is specified
                page = (page == null) ? "1" : page;
                size = (size == null) ? "10" : size;
                status = (status == null) ? "" : status;
                type = (type == null) ? "diagnosis" : type;
                source = (source == null) ? null : source;
                this.pageOptionSize = size;


                this.manageCuratedData(page, size, type, status, source);

            }
        )


        // Load Fab Scripts
        this.gs.loadScript('../pdxfinder/dependencies/fab.js');
    };


    manageCuratedData(page, size, type, status, source) {

        this.csvURL = `${this._mappingService._exportUrl}?entity-type=${type}&page=${page}&status=${status}`


        console.log(this.csvURL);

        this.columnHeaders = [];
        this.mappings = [];

        this._mappingService.getManagedTerms(type, source, page, size, status)
            .subscribe(
                data => {

                    this.data = data;

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



    exportCSV(){

        const download = function (data) {

            const blob = new Blob([data], {type: 'text/csv'});
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
           // a.setAttribute('hidden', '');
           // a.setAttribute('download', 'download.csv');
           // document.body.appendChild(a);
           // a.click();
           // document.removeChild(a);

            a.href = url;
            a.download = "myFile.csv";
            a.click();
            window.URL.revokeObjectURL(url);
            a.remove();
        };



        const objectToCsv = function (data) {

            const csvRows = [];

            const headers = Object.keys(data[0]);
            csvRows.push(headers.join(','));

            //loop over rows
            for (const row of data){

               const values = headers.map(header =>{
                   const escaped = (''+row[header]).replace(/"/g, '\\"');
                   return `"${escaped}"`;
                });
               csvRows.push(values.join(','));
            }

            return csvRows.join('\n');

        }
        
        const data = this.mappings.map(row =>({

            dateCreated: row.dateCreated,
            entityId: row.entityId,
            entityType: row.entityType,
            mapType: row.mapType,
            mappedTermLabel: row.mappedTermLabel

        }));

        const csvData = objectToCsv(data);


        download(csvData);
    }
    


    getProvidersList(){

        this._mappingService.getCurationSummary(null)
            .subscribe(
                data => {

                    data.forEach((dData, index) =>{

                        this.providersList.push(dData.DataSource);

                        this.providersList2.push(
                            {id: index, text: dData.DataSource, checked: false}
                        )

                    })
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

    getStatusList(){

       var statArray = ['validated', 'created', 'orphaned', 'unmapped'];

        statArray.forEach((status, index) => {
            this.statusList.push(
                {id: index, text: status, checked: false}
            )
        })
    }


    // whenever filter is apllied doit as size dro down, reset page to 1
    newPageSize(pageSize) {

        localStorage.setItem('_pageSize', pageSize);

        //  Auto-Navigate away on page size change
        let newPage = (this.userPage <= 1) ? this.userPage + 1 : 1;

        this.router.navigate(
            ['/curation/manage'],
            { queryParams: {page: newPage, size: pageSize, type: this.entityType, status : this.mappingStatus, source: this.dataSource} }
        );

    }


    searchFilter(form) {

        var filter = form.value;
        this.entityType = (filter.type != "") ? filter.type : this.entityType;
        this.mappingStatus = (filter.status != "") ? filter.status : this.mappingStatus;
        this.dataSource = (filter.source != "") ? filter.source : this.dataSource;


        // Capture Selected Providers Check Box
        var sources = [];
        this.providersList2.forEach((provider)=>{

            if (provider.checked == true) {
                sources.push(provider.text);
            }
        })
        this.dataSource = (sources.length != 0) ? sources.join() : this.dataSource;


        // Capture Selected Curation Status Check Box
        var status = [];
        this.statusList.forEach((dStatus)=>{

            if (dStatus.checked == true) {
                status.push(dStatus.text);
            }
        })
        this.mappingStatus = (status.length != 0) ? status.join() : this.mappingStatus;


        // Capture Data Type Status Check Box
        var types = [];
        this.dataTypes.forEach((dType)=>{

            if (dType.checked == true) {
                types.push(dType.text);
            }
        })
        this.entityType = (types.length != 0) ? types.join() : this.entityType;


        this.router.navigate(
            ['/curation/manage'],
            { queryParams: {page: null, size: this.pageSize, type: this.entityType, status : this.mappingStatus, source: this.dataSource} }
        );

    }



    toggleDisplay(compType: string) {

        if (compType == 'notif') {

            this.showNotif = (this.showNotif == true) ? false : true;

        }else if (compType == 'filter'){

            this.showFilter = (this.showFilter == true) ? false : true;
        }
    }












}
