import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {CurationMappingComponent} from "./curation-mapping/curation-mapping.component";
import {DatasourceSummaryComponent} from "./datasource-summary/datasource-summary.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {DatasourceSpecificComponent} from "./datasource-specific/datasource-specific.component";
import {DatasourceSpecificSuggestionsComponent} from "./datasource-specific-suggestions/datasource-specific-suggestions.component";
import {CurationManageComponent} from "./curation-manage/curation-manage.component";
import {CurationOrphanComponent} from "./curation-orphan/curation-orphan.component";
import {CurationArchiveComponent} from "./curation-archive/curation-archive.component";
import {CurationValidateComponent} from "./curation-validate/curation-validate.component";


const routes: Routes = [
    //{ path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    {path: '', component: DashboardComponent},
    {path: 'dashboard', component: DashboardComponent},
    {path: 'curation', component: CurationMappingComponent},
    {path: 'curation/manage', component: CurationManageComponent},
    {path: 'curation/validate', component: CurationValidateComponent},

    {
        path: 'curation/orphan', component: CurationOrphanComponent,
        children: [{path: 'recommendation/:id', component: DatasourceSpecificSuggestionsComponent}]
    },
    {
        path: 'curation/orphan/:page', component: CurationOrphanComponent,
        children: [{path: 'recommendation/:id', component: DatasourceSpecificSuggestionsComponent}]
    },

    {path: 'curation/archive', component: CurationArchiveComponent},
    {path: 'curation/:mapType', component: DatasourceSummaryComponent},

    {
        path: 'curation/:mapType/:source/:page', component: DatasourceSpecificComponent,
        children: [{path: 'suggested-mapping/:id', component: DatasourceSpecificSuggestionsComponent}]
    }

    // children: [ { path: ':source', component: DatasourceSpecificComponent } ]
];

@NgModule({
    //imports: [RouterModule.forRoot(routes)],
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}

export const routingComponents = [DashboardComponent,
    CurationMappingComponent,
    DatasourceSummaryComponent, DatasourceSpecificComponent]


/*
@NgModule({
imports: [RouterModule.forRoot(routes, {
onSameUrlNavigation: 'reload'
})],
exports: [RouterModule]
})
export class AppRoutingModule {
}
 */