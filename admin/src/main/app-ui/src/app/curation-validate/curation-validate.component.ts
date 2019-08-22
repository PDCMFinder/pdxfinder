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

    public data;
    public mappings = [];

    public report = null;

    public dataTypes = [];

    public selectedFiles: FileList;
    public currentFileUpload: File;
    public uploadedFilename: string;

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

        if ( userFile.type == 'text/csv'){

            this.selectedFiles = event.target.files;

            this.currentFileUpload = this.selectedFiles.item(0);

            console.log(this.currentFileUpload);

            this.uploadedFilename = this.currentFileUpload.name;
            this.report = 'waiting';

        }

    }

    upload() {

        this._mappingService.pushFileToStorage(this.currentFileUpload).subscribe(event => {

            if (event instanceof HttpResponse) {

                console.log('File is completely uploaded!');
            }
        });

        this.selectedFiles = undefined;
        this.report = 'success';
    }


    toggleReport(success: string) {
        this.report = null;
    }
}
