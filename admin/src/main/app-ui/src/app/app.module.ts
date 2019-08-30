import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {AppRoutingModule, routingComponents} from "./app-routing.module";
import {SideNavBarComponent} from './side-nav-bar/side-nav-bar.component';
import {TopNavBarComponent} from './top-nav-bar/top-nav-bar.component';
import {MappingService} from "./mapping.service";
import {HttpClientModule} from "@angular/common/http";
import {DatasourceSpecificComponent} from './datasource-specific/datasource-specific.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {DatasourceSpecificSuggestionsComponent} from "./datasource-specific-suggestions/datasource-specific-suggestions.component";
import {CurationManageComponent} from "./curation-manage/curation-manage.component";
import {CurationArchiveComponent} from './curation-archive/curation-archive.component';
import {CurationOrphanComponent} from './curation-orphan/curation-orphan.component';
import {CurationValidateComponent} from './curation-validate/curation-validate.component';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatAutocompleteModule, MatInputModule} from '@angular/material';


@NgModule({
    declarations: [
        AppComponent,
        SideNavBarComponent,
        TopNavBarComponent,
        routingComponents,
        DatasourceSpecificComponent,
        DatasourceSpecificSuggestionsComponent,
        CurationManageComponent,
        CurationArchiveComponent,
        CurationOrphanComponent,
        CurationValidateComponent,
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        FormsModule,
        AppRoutingModule,
        BrowserAnimationsModule,
        MatAutocompleteModule,
        MatInputModule,
        ReactiveFormsModule
    ],
    providers: [MappingService],
    bootstrap: [AppComponent]
})
export class AppModule {
}


