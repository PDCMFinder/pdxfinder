(window["webpackJsonp"] = window["webpackJsonp"] || []).push([["main"],{

/***/ "./src/$$_lazy_route_resource lazy recursive":
/*!**********************************************************!*\
  !*** ./src/$$_lazy_route_resource lazy namespace object ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

function webpackEmptyAsyncContext(req) {
	// Here Promise.resolve().then() is used instead of new Promise() to prevent
	// uncaught exception popping up in devtools
	return Promise.resolve().then(function() {
		var e = new Error("Cannot find module '" + req + "'");
		e.code = 'MODULE_NOT_FOUND';
		throw e;
	});
}
webpackEmptyAsyncContext.keys = function() { return []; };
webpackEmptyAsyncContext.resolve = webpackEmptyAsyncContext;
module.exports = webpackEmptyAsyncContext;
webpackEmptyAsyncContext.id = "./src/$$_lazy_route_resource lazy recursive";

/***/ }),

/***/ "./src/app/app-routing.module.ts":
/*!***************************************!*\
  !*** ./src/app/app-routing.module.ts ***!
  \***************************************/
/*! exports provided: AppRoutingModule, routingComponents */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppRoutingModule", function() { return AppRoutingModule; });
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "routingComponents", function() { return routingComponents; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var _curation_mapping_curation_mapping_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./curation-mapping/curation-mapping.component */ "./src/app/curation-mapping/curation-mapping.component.ts");
/* harmony import */ var _datasource_summary_datasource_summary_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./datasource-summary/datasource-summary.component */ "./src/app/datasource-summary/datasource-summary.component.ts");
/* harmony import */ var _dashboard_dashboard_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./dashboard/dashboard.component */ "./src/app/dashboard/dashboard.component.ts");
/* harmony import */ var _datasource_specific_datasource_specific_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./datasource-specific/datasource-specific.component */ "./src/app/datasource-specific/datasource-specific.component.ts");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};






var routes = [
    //{ path: '', redirectTo: '/dashboard', pathMatch: 'full' },
    { path: '', component: _dashboard_dashboard_component__WEBPACK_IMPORTED_MODULE_4__["DashboardComponent"] },
    { path: 'dashboard', component: _dashboard_dashboard_component__WEBPACK_IMPORTED_MODULE_4__["DashboardComponent"] },
    { path: 'curation', component: _curation_mapping_curation_mapping_component__WEBPACK_IMPORTED_MODULE_2__["CurationMappingComponent"] },
    { path: 'diagnosis-mapping', component: _datasource_summary_datasource_summary_component__WEBPACK_IMPORTED_MODULE_3__["DatasourceSummaryComponent"] },
    { path: 'diagnosis-mapping/:source', component: _datasource_specific_datasource_specific_component__WEBPACK_IMPORTED_MODULE_5__["DatasourceSpecificComponent"] }
];
var AppRoutingModule = /** @class */ (function () {
    function AppRoutingModule() {
    }
    AppRoutingModule = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["NgModule"])({
            imports: [_angular_router__WEBPACK_IMPORTED_MODULE_1__["RouterModule"].forRoot(routes)],
            exports: [_angular_router__WEBPACK_IMPORTED_MODULE_1__["RouterModule"]]
        })
    ], AppRoutingModule);
    return AppRoutingModule;
}());

var routingComponents = [_dashboard_dashboard_component__WEBPACK_IMPORTED_MODULE_4__["DashboardComponent"],
    _curation_mapping_curation_mapping_component__WEBPACK_IMPORTED_MODULE_2__["CurationMappingComponent"],
    _datasource_summary_datasource_summary_component__WEBPACK_IMPORTED_MODULE_3__["DatasourceSummaryComponent"], _datasource_specific_datasource_specific_component__WEBPACK_IMPORTED_MODULE_5__["DatasourceSpecificComponent"]];


/***/ }),

/***/ "./src/app/app.component.css":
/*!***********************************!*\
  !*** ./src/app/app.component.css ***!
  \***********************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = ""

/***/ }),

/***/ "./src/app/app.component.html":
/*!************************************!*\
  !*** ./src/app/app.component.html ***!
  \************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n<!-- Selector:app-side-nav-bar for Components from the side-nav-bar.component.html -->\n\n<app-side-nav-bar>\n  ...\n</app-side-nav-bar>\n\n\n<div id=\"page-wrapper\" class=\"gray-bg\">\n  <app-top-nav-bar>\n\n  </app-top-nav-bar>\n\n    <!-- ... The Router view goes here -->\n    <router-outlet>\n\n    </router-outlet>\n</div>\n"

/***/ }),

/***/ "./src/app/app.component.ts":
/*!**********************************!*\
  !*** ./src/app/app.component.ts ***!
  \**********************************/
/*! exports provided: AppComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppComponent", function() { return AppComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};

var AppComponent = /** @class */ (function () {
    function AppComponent() {
        this.title = 'PDX Finder Admin App';
    }
    AppComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-pdxFinder',
            template: __webpack_require__(/*! ./app.component.html */ "./src/app/app.component.html"),
            styles: [__webpack_require__(/*! ./app.component.css */ "./src/app/app.component.css")]
        })
    ], AppComponent);
    return AppComponent;
}());



/***/ }),

/***/ "./src/app/app.module.ts":
/*!*******************************!*\
  !*** ./src/app/app.module.ts ***!
  \*******************************/
/*! exports provided: AppModule */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "AppModule", function() { return AppModule; });
/* harmony import */ var _angular_platform_browser__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/platform-browser */ "./node_modules/@angular/platform-browser/fesm5/platform-browser.js");
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _app_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./app.component */ "./src/app/app.component.ts");
/* harmony import */ var _app_routing_module__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./app-routing.module */ "./src/app/app-routing.module.ts");
/* harmony import */ var _side_nav_bar_side_nav_bar_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! ./side-nav-bar/side-nav-bar.component */ "./src/app/side-nav-bar/side-nav-bar.component.ts");
/* harmony import */ var _top_nav_bar_top_nav_bar_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! ./top-nav-bar/top-nav-bar.component */ "./src/app/top-nav-bar/top-nav-bar.component.ts");
/* harmony import */ var _mapping_service__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! ./mapping.service */ "./src/app/mapping.service.ts");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");
/* harmony import */ var _datasource_specific_datasource_specific_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! ./datasource-specific/datasource-specific.component */ "./src/app/datasource-specific/datasource-specific.component.ts");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};









var AppModule = /** @class */ (function () {
    function AppModule() {
    }
    AppModule = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_1__["NgModule"])({
            declarations: [
                _app_component__WEBPACK_IMPORTED_MODULE_2__["AppComponent"],
                _side_nav_bar_side_nav_bar_component__WEBPACK_IMPORTED_MODULE_4__["SideNavBarComponent"],
                _top_nav_bar_top_nav_bar_component__WEBPACK_IMPORTED_MODULE_5__["TopNavBarComponent"],
                _app_routing_module__WEBPACK_IMPORTED_MODULE_3__["routingComponents"],
                _datasource_specific_datasource_specific_component__WEBPACK_IMPORTED_MODULE_8__["DatasourceSpecificComponent"]
            ],
            imports: [
                _angular_platform_browser__WEBPACK_IMPORTED_MODULE_0__["BrowserModule"],
                _angular_common_http__WEBPACK_IMPORTED_MODULE_7__["HttpClientModule"],
                _app_routing_module__WEBPACK_IMPORTED_MODULE_3__["AppRoutingModule"]
            ],
            providers: [_mapping_service__WEBPACK_IMPORTED_MODULE_6__["MappingService"]],
            bootstrap: [_app_component__WEBPACK_IMPORTED_MODULE_2__["AppComponent"]]
        })
    ], AppModule);
    return AppModule;
}());



/***/ }),

/***/ "./src/app/curation-mapping/curation-mapping.component.html":
/*!******************************************************************!*\
  !*** ./src/app/curation-mapping/curation-mapping.component.html ***!
  \******************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<div class=\"row wrapper border-bottom white-bg page-heading\">\n    <div class=\"col-lg-8\">\n        <h2>Curation Mappings</h2>\n        <ol class=\"breadcrumb\">\n            <li>\n                <a routerLink=\"/dashboard\">Dashboard </a>\n            </li>\n            <li class=\"active\">\n                Curation Mappings\n            </li>\n        </ol>\n    </div>\n</div>\n\n\n\n<div class=\"wrapper wrapper-content animated  bounceIn\">\n  <div class=\"p-w-md m-t-sm container\">\n\n\n    <div class=\"row\">\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content pdx-badge\" style=\"padding-bottom:10px;\" attr.pdxfinder-badge={{mappingCnt}}>\n            <a routerLink=\"/diagnosis-mapping\"> <img src=\"assets/icons/diagnosis.png\" class=\"icon\"> </a>\n            <div class='text-navy pdx-singleLine'> DIAGNOSIS MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/drug.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> DRUG MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/images.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> ... MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/images.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> ... MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/images.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> ... MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/images.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> ... MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n    </div>\n\n\n\n  </div>\n</div>"

/***/ }),

/***/ "./src/app/curation-mapping/curation-mapping.component.ts":
/*!****************************************************************!*\
  !*** ./src/app/curation-mapping/curation-mapping.component.ts ***!
  \****************************************************************/
/*! exports provided: CurationMappingComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "CurationMappingComponent", function() { return CurationMappingComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _mapping_service__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../mapping.service */ "./src/app/mapping.service.ts");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var CurationMappingComponent = /** @class */ (function () {
    function CurationMappingComponent(_mappingService) {
        this._mappingService = _mappingService;
    }
    CurationMappingComponent.prototype.ngOnInit = function () {
        var _this = this;
        this._mappingService.connectMissingMappingStream()
            .subscribe(function (data) {
            var myData = data["mappings"]; // This recieves the mappings node of the json in required format
            // Transform all d mappingValues node objects of each json to array format
            var count = 0;
            for (var _i = 0, myData_1 = myData; _i < myData_1.length; _i++) {
                var i = myData_1[_i];
                myData[count].mappingValues = Array.of(myData[count].mappingValues);
                count++;
            }
            _this.mappingCnt = count;
        });
    };
    CurationMappingComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-curation-mapping',
            template: __webpack_require__(/*! ./curation-mapping.component.html */ "./src/app/curation-mapping/curation-mapping.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [_mapping_service__WEBPACK_IMPORTED_MODULE_1__["MappingService"]])
    ], CurationMappingComponent);
    return CurationMappingComponent;
}());



/***/ }),

/***/ "./src/app/dashboard/dashboard.component.html":
/*!****************************************************!*\
  !*** ./src/app/dashboard/dashboard.component.html ***!
  \****************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<div class=\"wrapper wrapper-content animated bounceIn\">\n  <div class=\"p-w-md m-t-sm container\">\n\n    <div class=\"row\" style=\"margin-top:-20px;\">\n\n\n      <div class=\"col-lg-12\">\n        <div class=\"pdxfinder float-e-margins\">\n          <div class=\"pdxfinder-content\" >\n\n\n            <div>\n              <span class=\"pull-right text-right\">\n                <small>PDX Finder contains more than 800 PDX models from : <strong>JAX and IRCC</strong></small>\n                <br/>All Models: 162,862\n              </span>\n              <h3 class=\"font-bold no-margins\">\n                PDX Finder cancer by tissue\n              </h3>\n              <small>From all data sources.</small>\n            </div>\n\n            <div class=\"m-t-sm\">\n\n              <div class=\"row\" style=\"margin-top:-10px;\">\n                <div class=\"col-md-8\">\n                  <div id=\"chartdiv\" style=\"width: 100%; height:280px;\"></div>\n                </div>\n                <div class=\"col-md-4\" style=\"margin-top:20px;\">\n                  <ul class=\"stat-list m-t-lg\">\n                    <li>\n                      <h3 class=\"no-margins\">2,346</h3>\n                        <small>IRCC PDX Mouse Data </small>\n                        <div class=\"progress progress-mini\">\n                          <div class=\"progress-bar progress-bar-danger\" style=\"width: 30%;\"></div>\n                        </div>\n                    </li>\n                    <li>\n                      <h3 class=\"no-margins \">4,422</h3>\n                        <small>JAX PDX Mouse Data</small>\n                        <div class=\"progress progress-mini\">\n                          <div class=\"progress-bar progress-bar-info\" style=\"width: 60%;\"></div>\n                        </div>\n                    </li>\n                    <li>\n                      <h3 class=\"no-margins \">4,422</h3>\n                        <small>PDX Search last month</small>\n                        <div class=\"progress progress-mini\">\n                          <div class=\"progress-bar\" style=\"width: 80%;\"></div>\n                        </div>\n                    </li>\n                  </ul>\n                </div>\n              </div>\n\n            </div>\n\n            <div class=\"m-t-md\">\n              <small class=\"pull-right\">\n                <i class=\"ti-timer\"></i>\n                Updated on 30.07.2017\n              </small>\n              <small>\n                <strong> PDX report summary :</strong> The report has been changed over time, and last month reached a level over 50,000 counts\n              </small>\n            </div>\n\n          </div>\n        </div>\n      </div>\n\n    </div>\n\n\n\n\n\n    <div class=\"row\">\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <a routerLink=\"/curation\"> <img src=\"assets/icons/zoomaa.png\" class=\"icon\"> </a>\n            <div class='text-navy pdx-singleLine'> CURATION MAPPINGS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/vms.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> RAW DATA </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/Jira2.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> PDXFinder JIRA </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/Reports-icon.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> REPORTS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/SOPs.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> SOPs & METHODS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/finder.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> FINDER </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/03-app.jpg\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> GITHUB </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/contact.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> COMMUNITY CONTACTS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/SOPs.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> SOPs & METHODS </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/finder.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> FINDER </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/03-app.jpg\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> GITHUB </div>\n          </div>\n        </div>\n      </div>\n\n      <div class=\"col-lg-2 col-md-3 col-sm-4 col-xs-6\">\n        <div class=\"pdxfinder\">\n          <div class=\"pdxfinder-content\" style=\"padding-bottom:10px;\">\n            <img src=\"assets/icons/contact.png\" class=\"icon\">\n            <div class='text-navy pdx-singleLine'> COMMUNITY CONTACTS </div>\n          </div>\n        </div>\n      </div>\n    </div>\n\n\n\n  </div>\n</div>\n\n\n"

/***/ }),

/***/ "./src/app/dashboard/dashboard.component.ts":
/*!**************************************************!*\
  !*** ./src/app/dashboard/dashboard.component.ts ***!
  \**************************************************/
/*! exports provided: DashboardComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DashboardComponent", function() { return DashboardComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};

var DashboardComponent = /** @class */ (function () {
    function DashboardComponent() {
    }
    DashboardComponent.prototype.ngOnInit = function () {
        this.loadScript('../pdxfinder/dependencies/chart/amcharts.js');
        this.loadScript('../pdxfinder/dependencies/chart/serial.js');
        this.loadScript('../pdxfinder/dependencies/chart/export.min.js');
        this.loadScript('../pdxfinder/dependencies/chart/light.js');
        this.loadScript('../pdxfinder/dependencies/chart/3dbar.js');
    };
    DashboardComponent.prototype.loadScript = function (url) {
        var body = document.body;
        var script = document.createElement('script');
        script.innerHTML = '';
        script.src = url;
        script.async = false;
        script.defer = true;
        body.appendChild(script);
    };
    DashboardComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-dashboard',
            template: __webpack_require__(/*! ./dashboard.component.html */ "./src/app/dashboard/dashboard.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [])
    ], DashboardComponent);
    return DashboardComponent;
}());



/***/ }),

/***/ "./src/app/datasource-specific/datasource-specific.component.html":
/*!************************************************************************!*\
  !*** ./src/app/datasource-specific/datasource-specific.component.html ***!
  \************************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n<div class=\"row wrapper border-bottom white-bg page-heading\">\n  <div class=\"col-lg-8\">\n    <h2>Center Summary</h2>\n    <ol class=\"breadcrumb\">\n      <li>\n        <a routerLink=\"/dashboard\">...</a>\n      </li>\n      <li>\n        <a routerLink=\"/curation\">Curation Mappings</a>\n      </li>\n      <li>\n        <a routerLink=\"/diagnosis-mapping\">Center Summary</a>\n      </li>\n      <li class=\"active\">{{dataSource | uppercase}} </li>\n    </ol>\n  </div>\n</div>\n\n\n\n\n<div class=\"wrapper wrapper-content animated pulse\">\n  <div class=\"p-w-md m-t-sm col-lg-10 col-lg-offset-1\">\n\n\n    <div class=\"row\">\n      <div class=\"col-lg-12\">\n        <div class=\"pdxfinder float-e-margins\">\n          <div class=\"pdxfinder-title\">\n            <h5>UNAMAPPED DIAGNOSIS - <b style=\"color: #03369D\"> {{dataSource | uppercase}}  </b> </h5>\n          </div>\n          <div class=\"pdxfinder-content\">\n            <div class=\"table-responsive\">\n\n              <table class=\"table table-striped table-bordered table-hover dataTables-example\" >\n                <thead>\n                <tr>\n                  <th style=\"width:10px;\">S/N</th>\n                  <th> SOURCE </th>\n                  <th> DIAGNOSIS </th>\n                  <th> TUMOR TYPE </th>\n                  <th> PRIMARY TUMOR </th>\n                  <th> MAPPED TERM </th>\n                  <th> TYPE </th>\n                  <th>JUSTIFICATION</th>\n                </tr>\n                </thead>\n                <tbody>\n\n                <tr class=\"pdxfinder-clickable-rows\" *ngFor=\"let mapping of mappings; index as i\" >\n                  <td> {{i+1}} </td>\n                  <td> {{ mapping.mappingValues.DataSource | uppercase }} </td>\n                  <td> {{ mapping.mappingValues.SampleDiagnosis | uppercase }} </td>\n                  <td> {{ mapping.mappingValues.TumorType | uppercase }} </td>\n                  <td> {{ mapping.mappingValues.OriginTissue | uppercase }} </td>\n                  <td> </td>\n                  <td> </td>\n                  <td> </td>\n                </tr>\n\n                </tbody>\n              </table>\n\n            </div>\n\n          </div>\n        </div>\n      </div>\n    </div>\n\n  </div>\n</div>"

/***/ }),

/***/ "./src/app/datasource-specific/datasource-specific.component.ts":
/*!**********************************************************************!*\
  !*** ./src/app/datasource-specific/datasource-specific.component.ts ***!
  \**********************************************************************/
/*! exports provided: DatasourceSpecificComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DatasourceSpecificComponent", function() { return DatasourceSpecificComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
/* harmony import */ var _mapping_service__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ../mapping.service */ "./src/app/mapping.service.ts");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};



var DatasourceSpecificComponent = /** @class */ (function () {
    function DatasourceSpecificComponent(route, _mappingService) {
        this.route = route;
        this._mappingService = _mappingService;
        this.mappings = [];
    }
    DatasourceSpecificComponent.prototype.ngOnInit = function () {
        var _this = this;
        // From the current url snapshot, get the source parameter and assign to the dataSource property
        this.dataSource = this.route.snapshot.paramMap.get('source');
        this._mappingService.connectMissingMappingStream()
            .subscribe(function (data) {
            var myData = data["mappings"]; // This recieves the mappings node of the json in required format
            var count = 0;
            for (var _i = 0, myData_1 = myData; _i < myData_1.length; _i++) {
                var i = myData_1[_i];
                if (myData[count].mappingValues.DataSource.toUpperCase() === _this.dataSource.toUpperCase()) {
                    _this.mappings.push(myData[count]);
                }
                count++;
            }
        });
    };
    DatasourceSpecificComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-datasource-specific',
            template: __webpack_require__(/*! ./datasource-specific.component.html */ "./src/app/datasource-specific/datasource-specific.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [_angular_router__WEBPACK_IMPORTED_MODULE_1__["ActivatedRoute"], _mapping_service__WEBPACK_IMPORTED_MODULE_2__["MappingService"]])
    ], DatasourceSpecificComponent);
    return DatasourceSpecificComponent;
}());



/***/ }),

/***/ "./src/app/datasource-summary/datasource-summary.component.html":
/*!**********************************************************************!*\
  !*** ./src/app/datasource-summary/datasource-summary.component.html ***!
  \**********************************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = " <div class=\"row wrapper border-bottom white-bg page-heading\">\n    <div class=\"col-lg-8\">\n        <h2>Center Summary</h2>\n        <ol class=\"breadcrumb\">\n            <li>\n                <a routerLink=\"/dashboard\">Dashboard</a>\n            </li>\n            <li>\n                <a routerLink=\"/curation\">Curation Mappings</a>\n            </li>\n            <li class=\"active\">\n                Unmapped Diagnosis\n            </li>\n        </ol>\n    </div>\n</div>\n\n<div class=\"wrapper wrapper-content animated rotateInUpLeft\">\n  <div class=\"p-w-md m-t-sm col-lg-10 col-lg-offset-1\">\n\n\n    <div class=\"row\">\n      <div class=\"col-lg-12\">\n        <div class=\"pdxfinder float-e-margins\">\n          <div class=\"pdxfinder-title\">\n            <h5>Unmapped Models By Data Source</h5>\n          </div>\n          <div class=\"pdxfinder-content\">\n            <div class=\"table-responsive\">\n\n                <table class=\"table table-striped table-bordered table-hover dataTables-example\" >\n                    <thead>\n                    <tr>\n                        <th style=\"width:10px;\">S/N</th>\n                        <th>SOURCE </th>\n                        <th>MISSING MAPPINGS </th>\n                        <th>TOTAL MAPPED</th>\n                        <th>VALIDATED</th>\n                        <th>UNVALIDATED</th>\n                    </tr>\n                    </thead>\n                    <tbody>\n\n                    <tr class=\"pdxfinder-clickable-rows\" *ngFor=\" let source of pdxStatArray.source; index as i \" (click)=\"onSelect(source)\">\n                        <td> {{i+1}} </td>\n                        <td> {{ source | uppercase }} </td>\n                        <td> {{pdxStatArray.missing[i]}} </td>\n                        <td> {{pdxStatArray.total[i]}} </td>\n                        <td> {{pdxStatArray.validated[i]}} </td>\n                        <td> {{pdxStatArray.unvalidated[i]}} </td>\n                    </tr>\n                    </tbody>\n                </table>\n\n            </div>\n\n          </div>\n        </div>\n      </div>\n    </div>\n\n  </div>\n</div>\n<!-- use container or blank for small screen and col-lg-10 col-lg-offset-1 for 17 inches upwards  swing tada wobble bounceIn (Up,Left,Right,Down) lightSpeedIn , flipInY, rotateInDownRight, rotateInUpLeft, slideInLeft, hinge, rollIn-->"

/***/ }),

/***/ "./src/app/datasource-summary/datasource-summary.component.ts":
/*!********************************************************************!*\
  !*** ./src/app/datasource-summary/datasource-summary.component.ts ***!
  \********************************************************************/
/*! exports provided: DatasourceSummaryComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "DatasourceSummaryComponent", function() { return DatasourceSummaryComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _mapping_service__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ../mapping.service */ "./src/app/mapping.service.ts");
/* harmony import */ var rxjs_index__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! rxjs/index */ "./node_modules/rxjs/index.js");
/* harmony import */ var rxjs_index__WEBPACK_IMPORTED_MODULE_2___default = /*#__PURE__*/__webpack_require__.n(rxjs_index__WEBPACK_IMPORTED_MODULE_2__);
/* harmony import */ var rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! rxjs/internal/operators */ "./node_modules/rxjs/internal/operators/index.js");
/* harmony import */ var rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3___default = /*#__PURE__*/__webpack_require__.n(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__);
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! @angular/router */ "./node_modules/@angular/router/fesm5/router.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};





var DatasourceSummaryComponent = /** @class */ (function () {
    function DatasourceSummaryComponent(_mappingService, router, route) {
        this._mappingService = _mappingService;
        this.router = router;
        this.route = route;
        this.pdxStatArray = {
            source: [],
            missing: [],
            total: [],
            validated: [],
            unvalidated: []
        };
    }
    DatasourceSummaryComponent.prototype.ngOnInit = function () {
        var _this = this;
        var tempArr = [];
        // Component to the Service using Reactive Observables
        this._mappingService.connectTotalMappedStream()
            .subscribe(function (data) {
            // This receives the mappings node of the json in required format
            var myData = data["mappings"];
            // Emit Each Unmapped Diagnosis
            var source = Object(rxjs_index__WEBPACK_IMPORTED_MODULE_2__["from"])(myData);
            // Group by Data Source
            var groupedByImpl = source.pipe(Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["groupBy"])(function (missingMapping) { return missingMapping["mappingValues"].DataSource; }), Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["mergeMap"])(function (grouped) { return grouped.pipe(Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["toArray"])()); }) // Return each item in the grouped array.
            );
            // Retrieve the data from the groupedByImpl (The Grouped By Implementation of the missing Mappings JSON)
            var subscribe = groupedByImpl.subscribe(function (result) {
                //console.log(result);
                _this.pdxStatArray.source.push(result[0]["mappingValues"].DataSource);
                _this.pdxStatArray.total.push(result.length);
                _this.pdxStatArray.validated.push(result.length);
                _this.pdxStatArray.unvalidated.push(0);
            });
        });
        this._mappingService.connectMissingMappingStream()
            .subscribe(function (data) {
            // Group by Data Source
            var groupedByImpl = Object(rxjs_index__WEBPACK_IMPORTED_MODULE_2__["from"])(data["mappings"]).pipe(Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["groupBy"])(function (missingMapping) { return missingMapping["mappingValues"].DataSource; }), Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["mergeMap"])(function (grouped) { return grouped.pipe(Object(rxjs_internal_operators__WEBPACK_IMPORTED_MODULE_3__["toArray"])()); }) // Return each item in the grouped array.
            );
            var subscribe = groupedByImpl.subscribe(function (result) {
                tempArr.push(result[0]["mappingValues"].DataSource + "__" + result.length);
            });
            for (var i = 0; i < _this.pdxStatArray.source.length; i++) {
                for (var num = 0; num < tempArr.length; num++) {
                    if (tempArr[num].split("__")[0].toUpperCase() == _this.pdxStatArray.source[i].toUpperCase()) {
                        _this.pdxStatArray.missing[i] = tempArr[num].split("__")[1];
                        break;
                    }
                    else {
                        _this.pdxStatArray.missing[i] = 0;
                    }
                }
            }
        });
    };
    DatasourceSummaryComponent.prototype.onSelect = function (source) {
        this.router.navigate([source], { relativeTo: this.route });
    };
    DatasourceSummaryComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-datasource-summary',
            template: __webpack_require__(/*! ./datasource-summary.component.html */ "./src/app/datasource-summary/datasource-summary.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [_mapping_service__WEBPACK_IMPORTED_MODULE_1__["MappingService"], _angular_router__WEBPACK_IMPORTED_MODULE_4__["Router"], _angular_router__WEBPACK_IMPORTED_MODULE_4__["ActivatedRoute"]])
    ], DatasourceSummaryComponent);
    return DatasourceSummaryComponent;
}());



/***/ }),

/***/ "./src/app/mapping.service.ts":
/*!************************************!*\
  !*** ./src/app/mapping.service.ts ***!
  \************************************/
/*! exports provided: MappingService */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "MappingService", function() { return MappingService; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_common_http__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/common/http */ "./node_modules/@angular/common/fesm5/http.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};


var MappingService = /** @class */ (function () {
    function MappingService(http) {
        this.http = http;
        //private _totalMappedUrl = "/api/mapping/diagnosis";
        //private _missingMappedUrl = "/api/missingmapping/diagnosis";
        this._totalMappedUrl = "/assets/data/mapped-diagnosis.json";
        this._missingMappedUrl = "/assets/data/diagnosis.json";
    }
    //Retrieve Total mapped diagnosis
    MappingService.prototype.connectTotalMappedStream = function () {
        return this.http.get(this._totalMappedUrl);
    };
    //Retrieve missing mapping diagnosis
    MappingService.prototype.connectMissingMappingStream = function () {
        return this.http.get(this._missingMappedUrl);
    };
    MappingService = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Injectable"])({
            providedIn: 'root'
        }),
        __metadata("design:paramtypes", [_angular_common_http__WEBPACK_IMPORTED_MODULE_1__["HttpClient"]])
    ], MappingService);
    return MappingService;
}());



/***/ }),

/***/ "./src/app/side-nav-bar/side-nav-bar.component.html":
/*!**********************************************************!*\
  !*** ./src/app/side-nav-bar/side-nav-bar.component.html ***!
  \**********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "<!-- Side Navigation menu here-->\n<nav class=\"navbar-default navbar-static-side\" role=\"navigation\">\n  <div class=\"sidebar-collapse\">\n    <a class=\"close-canvas-menu\"><i class=\"ti-close\" style=\"color: #ffffff;\"></i></a>\n\n\n    <ul class=\"nav metismenu\" id=\"side-menu\">\n\n      <li class=\"nav-header\">\n        <div class=\"dropdown profile-element\">\n          <span>\n                <img alt=\"image\" class=\"img-circle\" src=\"/assets/icons/user.jpg\" />\n          </span>\n          <a data-toggle=\"dropdown\" class=\"dropdown-toggle\" href=\"#\">\n                <span class=\"clear\">\n                  <span class=\"block m-t-xs\">\n                  <strong class=\"font-bold\" style=\"color:#efefef;\">Nathalie Conte</strong>\n                  </span>\n                  </span>\n          </a>\n        </div>\n        <div class=\"logo-element\">\n          PDX+\n        </div>\n      </li>\n\n      <li> <a routerLink=\"/dashboard\"><i class=\"ti-dashboard\"></i> <span class=\"nav-label\"> &nbsp; Dashboard </span></a> </li>\n      <li class=\"active\">\n        <a href=\"#\"><i class=\"ti-menu-alt\"></i> <span class=\"nav-label\"> &nbsp; Tool Box</span> </a>\n        <ul class=\"nav nav-second-level\">\n            <li><a routerLink=\"/curation\" class=\"active\">Curation Mappings</a></li>\n            <li><a routerLink=\"/diagnosis-mapping\" class=\"active\">Diagnosis Mappings</a></li>\n          <!--<li><a href=\"#\">PDX Finder Jira <span class=\"label label-primary pull-right\">8</span></a></li>\n          <li><a href=\"#\">Raw Data <span class=\"label label-primary pull-right\">2</span></a></li>\n          <li><a href=\"#\">Reports </a></li>\n          <li><a href=\"#\">SOPs & Methods </a></li>\n          <li><a href=\"#\">Finder </a></li>\n          <li><a href=\"#\">Github </a></li>\n          <li><a href=\"#\">Community Contacts </a></li>-->\n        </ul>\n      </li>\n\n      <li>\n        <a href=\"#\"><i class=\"ti-server\"></i> <span class=\"nav-label\"> &nbsp; Web Server </span> </a>\n        <ul class=\"nav nav-second-level\">\n            <li><a href=\"http://ves-ebi-bc.ebi.ac.uk\" target=\"_blank\" class=\"active\"> Dev Webapp </a></li>\n            <li><a href=\"http://ves-ebi-b4.ebi.ac.uk\" target=\"_blank\" class=\"active\"> Beta Webapp </a></li>\n            <li><a href=\"http://wp-p1m-b0.ebi.ac.uk\" target=\"_blank\" class=\"active\"> Production Webapp 1 </a></li>\n            <li><a href=\"http://wp-p1m-b0.ebi.ac.uk\" target=\"_blank\" class=\"active\"> Production Webapp 2 </a></li>\n        </ul>\n      </li>\n      <li> <a href=\"#\"><i class=\"ti-harddrive\"></i> <span class=\"nav-label\"> &nbsp; Manage IRCC Data</span></a> </li>\n      <li> <a href=\"#\"><i class=\"ti-harddrive\"></i>  <span class=\"nav-label\"> &nbsp; Manage JAX Data</span></a> </li>\n\n\n      <li class=\"pdxFinderActive2\">\n        <a target=\"_blank\" href=\"http://www.pdxfinder.org\"><i class=\"ti-tablet\"></i>  <span class=\"nav-label\"> &nbsp; PDX Finder Website</span> </a>\n      </li>\n      <li class=\"pdxFinderActive\">\n        <a target=\"_blank\" href=\"http://ves-ebi-bc.ebi.ac.uk:7474/browser\"><i class=\"ti-harddrives\"></i>  <span class=\"nav-label\"> &nbsp; Neo4j Database</span></a>\n      </li>\n    </ul>\n\n\n\n  </div>\n</nav>\n\n\n\n\n\n\n"

/***/ }),

/***/ "./src/app/side-nav-bar/side-nav-bar.component.ts":
/*!********************************************************!*\
  !*** ./src/app/side-nav-bar/side-nav-bar.component.ts ***!
  \********************************************************/
/*! exports provided: SideNavBarComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "SideNavBarComponent", function() { return SideNavBarComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};

var SideNavBarComponent = /** @class */ (function () {
    function SideNavBarComponent() {
    }
    SideNavBarComponent.prototype.ngOnInit = function () {
    };
    SideNavBarComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-side-nav-bar',
            template: __webpack_require__(/*! ./side-nav-bar.component.html */ "./src/app/side-nav-bar/side-nav-bar.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [])
    ], SideNavBarComponent);
    return SideNavBarComponent;
}());



/***/ }),

/***/ "./src/app/top-nav-bar/top-nav-bar.component.html":
/*!********************************************************!*\
  !*** ./src/app/top-nav-bar/top-nav-bar.component.html ***!
  \********************************************************/
/*! no static exports found */
/***/ (function(module, exports) {

module.exports = "\n  <div class=\"row border-bottom\">\n\n    <nav class=\"navbar navbar-static-top white-bg\" role=\"navigation\" style=\"margin-bottom: 0\">\n      <div class=\"navbar-header\">\n        <button class=\"navbar-minimalize minimalize-styl-2 btn btn-primary\" href=\"#\" style=\"margin: 4px;\"><i class=\"ti-location-arrow\" style=\"font-size: 25px;\"></i>  </button>\n      </div>\n    </nav>\n\n  </div>\n"

/***/ }),

/***/ "./src/app/top-nav-bar/top-nav-bar.component.ts":
/*!******************************************************!*\
  !*** ./src/app/top-nav-bar/top-nav-bar.component.ts ***!
  \******************************************************/
/*! exports provided: TopNavBarComponent */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "TopNavBarComponent", function() { return TopNavBarComponent; });
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
var __decorate = (undefined && undefined.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (undefined && undefined.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};

var TopNavBarComponent = /** @class */ (function () {
    function TopNavBarComponent() {
    }
    TopNavBarComponent.prototype.ngOnInit = function () {
    };
    TopNavBarComponent = __decorate([
        Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["Component"])({
            selector: 'app-top-nav-bar',
            template: __webpack_require__(/*! ./top-nav-bar.component.html */ "./src/app/top-nav-bar/top-nav-bar.component.html"),
            styles: [""]
        }),
        __metadata("design:paramtypes", [])
    ], TopNavBarComponent);
    return TopNavBarComponent;
}());



/***/ }),

/***/ "./src/environments/environment.ts":
/*!*****************************************!*\
  !*** ./src/environments/environment.ts ***!
  \*****************************************/
/*! exports provided: environment */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "environment", function() { return environment; });
// This file can be replaced during build by using the `fileReplacements` array.
// `ng build ---prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
var environment = {
    production: false
};
/*
 * In development mode, to ignore zone related error stack frames such as
 * `zone.run`, `zoneDelegate.invokeTask` for easier debugging, you can
 * import the following file, but please comment it out in production mode
 * because it will have performance impact when throw error
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.


/***/ }),

/***/ "./src/main.ts":
/*!*********************!*\
  !*** ./src/main.ts ***!
  \*********************/
/*! no exports provided */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
__webpack_require__.r(__webpack_exports__);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! @angular/core */ "./node_modules/@angular/core/fesm5/core.js");
/* harmony import */ var _angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/platform-browser-dynamic */ "./node_modules/@angular/platform-browser-dynamic/fesm5/platform-browser-dynamic.js");
/* harmony import */ var _app_app_module__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! ./app/app.module */ "./src/app/app.module.ts");
/* harmony import */ var _environments_environment__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! ./environments/environment */ "./src/environments/environment.ts");




if (_environments_environment__WEBPACK_IMPORTED_MODULE_3__["environment"].production) {
    Object(_angular_core__WEBPACK_IMPORTED_MODULE_0__["enableProdMode"])();
}
Object(_angular_platform_browser_dynamic__WEBPACK_IMPORTED_MODULE_1__["platformBrowserDynamic"])().bootstrapModule(_app_app_module__WEBPACK_IMPORTED_MODULE_2__["AppModule"])
    .catch(function (err) { return console.log(err); });


/***/ }),

/***/ 0:
/*!***************************!*\
  !*** multi ./src/main.ts ***!
  \***************************/
/*! no static exports found */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(/*! /Users/abayomi/IdeaProjects/pdxfinder/admin/src/main/app-ui/src/main.ts */"./src/main.ts");


/***/ })

},[[0,"runtime","vendor"]]]);
//# sourceMappingURL=main.js.map