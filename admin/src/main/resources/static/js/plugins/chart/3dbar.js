var chart = AmCharts.makeChart("chartdiv", {
    "theme": "light",
    "type": "serial",
	"startDuration": 2,
    "dataProvider": [{
        "country": "Breast Cancer",
        "visits": 4025,
        "color": "#FF0F00"
    }, {
        "country": "Pancreatic",
        "visits": 1882,
        "color": "#FF6600"
    }, {
        "country": "Rectum",
        "visits": 1809,
        "color": "#FF9E01"
    }, {
        "country": "Bile Duct",
        "visits": 1322,
        "color": "#FCD202"
    }, {
        "country": "Liver",
        "visits": 1122,
        "color": "#F8FF01"
    }, {
        "country": "Salivary Gland",
        "visits": 1114,
        "color": "#B0DE09"
    }, {
        "country": "tonsil",
        "visits": 984,
        "color": "#04D215"
    }, {
        "country": "anal",
        "visits": 711,
        "color": "#0D8ECF"
    }, {
        "country": "Stomach",
        "visits": 665,
        "color": "#0D52D1"
    }, {
        "country": "appendix",
        "visits": 580,
        "color": "#2A0CD0"
    }, {
        "country": "esophageal",
        "visits": 443,
        "color": "#8A0CCF"
    }, {
        "country": "duodenum",
        "visits": 441,
        "color": "#CD0D74"
    }, {
        "country": "lung ",
        "visits": 395,
        "color": "#754DEB"
    }, {
        "country": "Brain",
        "visits": 386,
        "color": "#DDDDDD"
    }, {
        "country": "adrenal gland",
        "visits": 384,
        "color": "#999999"
    }, {
        "country": "Breast",
        "visits": 338,
        "color": "#333333"
    }, {
        "country": "Skin",
        "visits": 328,
        "color": "#000000"
    }],
    "valueAxes": [{
        "position": "left",
        "title": "Visitors"
    }],
    "graphs": [{
        "balloonText": "[[category]]: <b>[[value]]</b>",
        "fillColorsField": "color",
        "fillAlphas": 1,
        "lineAlpha": 0.1,
        "type": "column",
        "valueField": "visits",
        "showHandOnHover": true
    }],
    "depth3D": 20,
	"angle": 30,
    "chartCursor": {
        "categoryBalloonEnabled": false,
        "cursorAlpha": 0,
        "zoomable": true
    },
    "categoryField": "country",
    "categoryAxis": {
        "gridPosition": "start",
        "labelRotation": 0
    },
    "listeners": [{
        "event": "clickGraphItem",
        "method": function(event) {
            var drillDownLink = 'http://www.pdxfinder.org/data/search?cancer_system='+event.item.category;
            window.open(drillDownLink,'_blank');
        }
    }],
    "export": {
    	"enabled": true
     }

});