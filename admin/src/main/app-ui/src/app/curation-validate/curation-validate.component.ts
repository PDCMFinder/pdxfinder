import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {MappingService} from "../mapping.service";
import {Mapping, MappingInterface, MappingValues} from "../mapping-interface";
import {GeneralService} from "../general.service";
import {FormBuilder, FormGroup} from "@angular/forms";
import {HttpClient, HttpResponse, HttpEventType} from '@angular/common/http';

declare var swal: any;


@Component({
    selector: 'app-curation-validate',
    templateUrl: './curation-validate.component.html',
    styles: [``]
})
export class CurationValidateComponent implements OnInit {

    private data;
    private mappings = [];

    private report = null;

    private dataTypes = [];

    private selectedFiles: FileList;
    private currentFileUpload: File;
    private uploadedFilename: string;
    errorReport: string;

    private parsedCsvHead = [];
    private parsedCsvBody = [];
    private showCSV = false;

    constructor(private router: Router,
                private route: ActivatedRoute,
                private _mappingService: MappingService,
                private gs: GeneralService) {
    }

    ngOnInit() {

        this.getDataTypes();

    };


    getDataTypes() {

        var entityTypes = ['diagnosis', 'treatment'];

        entityTypes.forEach((entity, index) => {
            this.dataTypes.push(
                {id: index, text: entity, checked: false}
            )
        })

    }


    selectFile(event) {

        const userFile = event.target.files.item(0);


        if (userFile.type == 'text/csv') {

            this.displayUploadedCSV(event);

            this.selectedFiles = event.target.files;

            this.currentFileUpload = this.selectedFiles.item(0);

            //console.log(this.currentFileUpload);

            this.uploadedFilename = this.currentFileUpload.name;

            this.report = 'waiting';

        } else {
            this.report = 'failed';
            this.selectedFiles = null;
            this.errorReport = `${userFile.name} is an Invalid file type, pls upload CSV`;
            this.showCSV = false;
        }
    }


    displayUploadedCSV(event) {


        this.parsedCsvHead = [];
        this.parsedCsvBody = [];

        var reader = new FileReader();

        reader.readAsText(event.srcElement.files[0]);

        reader.onload = () => {

            const lines = reader.result.split('\n');

            lines.forEach((element, index) => {

                const cols: string[] = element.replace(/['"]+/g, '').split(',');

                if (index == 0) {

                    this.parsedCsvHead = cols;
                } else {
                    this.parsedCsvBody.push(cols);
                }
                
            });
        }

        this.showCSV = true;
    }


    upload() {

        this._mappingService.pushFileToStorage(this.currentFileUpload, 'diagnosis').subscribe(responseEntity => {

                console.log('File is completely uploaded!');
                console.log(responseEntity);
                this.report = 'success';

            },
            failedResponse => {

                this.report = 'failed';
                this.errorReport = `${failedResponse.error}`;

                console.log('File was not completely uploaded!');
                console.log(failedResponse);
            }
        );

        this.selectedFiles = null;

    }



    toggleReport(success: string) {
        this.report = null;
    }
}



/*



 */