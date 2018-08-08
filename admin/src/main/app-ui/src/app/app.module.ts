import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { AppRoutingModule, routingComponents } from "./app-routing.module";
import { SideNavBarComponent } from './side-nav-bar/side-nav-bar.component';
import { TopNavBarComponent } from './top-nav-bar/top-nav-bar.component';

@NgModule({
  declarations: [
    AppComponent,
    SideNavBarComponent,
    TopNavBarComponent,
      routingComponents
  ],
  imports: [
    BrowserModule,
      AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
