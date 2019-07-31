import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CurationMappingComponent } from "./curation-mapping/curation-mapping.component";
import { DatasourceSummaryComponent} from "./datasource-summary/datasource-summary.component";
import { DashboardComponent } from "./dashboard/dashboard.component";
import {DatasourceSpecificComponent} from "./datasource-specific/datasource-specific.component";
import {DatasourceSpecificSuggestionsComponent} from "./datasource-specific-suggestions/datasource-specific-suggestions.component";


const routes: Routes = [
  //{ path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: '',   component: DashboardComponent },
    { path: 'dashboard',   component: DashboardComponent },
    { path: 'curation', component: CurationMappingComponent },
    { path: 'curation/:mapType',  component: DatasourceSummaryComponent},
    {
        path: 'curation/:mapType/:source',  component: DatasourceSpecificComponent,
        children: [ { path: 'suggested-mapping/:id', component: DatasourceSpecificSuggestionsComponent } ]
    }

   // children: [ { path: ':source', component: DatasourceSpecificComponent } ]
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
export const routingComponents = [DashboardComponent,
                                  CurationMappingComponent,
                                  DatasourceSummaryComponent, DatasourceSpecificComponent]



/*
mapping-summary
 */