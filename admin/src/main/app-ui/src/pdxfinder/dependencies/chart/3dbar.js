
function pdxFinderbarChart(title, chartData, cssID, categoryField, valueField, labelRotation) {


    AmCharts.addInitHandler(function(chart) {
        // check if there are graphs with autoColor: true set
        for(var i = 0; i < chart.graphs.length; i++) {
            var graph = chart.graphs[i];
            if (graph.autoColor !== true)
                continue;
            var colorKey = "autoColor-"+i;
            graph.lineColorField = colorKey;
            graph.fillColorsField = colorKey;
            for(var x = 0; x < chart.dataProvider.length; x++) {
                var color = chart.colors[x]
                chart.dataProvider[x][colorKey] = color;
            }
        }

    }, ["serial"]);


     var chart = AmCharts.makeChart(cssID, {
        "theme": "light",
        "type": "serial",
        "startDuration": 2,
        "dataProvider": chartData,
        "valueAxes": [{
            "position": "left",
            "title": title
        }],
        "graphs": [{
            "balloonText": "[[category]]: <b>[[value]]</b>",
            "fillColorsField": "color",
            "fillAlphas": 1,
            "lineAlpha": 0.1,
            "type": "column",
            "valueField": valueField,
            "showHandOnHover": true,
            "autoColor": true
        }],
        "depth3D": 20,
        "angle": 30,
        "chartCursor": {
            "categoryBalloonEnabled": true,
            "cursorAlpha": 0,
            "zoomable": true
        },
        "categoryField": categoryField,
        "categoryAxis": {
            "gridPosition": "start",
            "labelRotation": labelRotation,
            "labelFunction": function(valueText, serialDataItem, categoryAxis) {
                if (valueText.length > 10)
                    return valueText.substring(0, 10) + '...';
                else
                    return valueText;
            }
        },
        "listeners": [{
            "event": "clickGraphItem",
            "method": function(event) {
                var drillDownLink = '/diagnosis-mapping/'+event.item.category;
                window.open(drillDownLink,'_parent');
            }
        }],
        "export": {
            "enabled": true
        }


    });



}
