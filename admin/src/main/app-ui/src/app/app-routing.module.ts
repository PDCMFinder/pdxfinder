import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { CurationMappingComponent } from "./curation-mapping/curation-mapping.component";
import { DatasourceSummaryComponent} from "./datasource-summary/datasource-summary.component";
import { DashboardComponent } from "./dashboard/dashboard.component";


const routes: Routes = [
  //{ path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: '',   component: DashboardComponent },
    { path: 'dashboard',   component: DashboardComponent },
  { path: 'curation', component: CurationMappingComponent },
  { path: 'summary',   component: DatasourceSummaryComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
export const routingComponents = [DashboardComponent,
                                  CurationMappingComponent,
                                  DatasourceSummaryComponent]