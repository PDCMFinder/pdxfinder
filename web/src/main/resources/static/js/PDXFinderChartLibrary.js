/**
 * Created by abayomi on 10/08/2017.
 */


var counta, dataSum = 0;

/* Start Compute the Sum of the JSON data for the Chart  */
function sumAll(jsonData)
{
    dataSum = 0;
    for (counta = 0; counta < jsonData.length; counta++)
    { dataSum += jsonData[counta].y;}

    return dataSum;
}
/* End Compute the Sum of the JSON data for the Chart */



/* Begin Draw the Cancer By System Chart  */
function cancerBySystemChart(chartData, cssID)
{

    cancerBySystemObject = JSON.parse(chartData);
    cssDisplayArea = cssID;

    Highcharts.chart(cssDisplayArea, {
        credits: false,
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {
            text: 'Cancers by system'
        },
        tooltip: {
            pointFormat: '{series.name}: <b>{point.y}</b> (<b>{point.percentage:.1f}%</b>)'
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: false
                },
                showInLegend: true
            }
        },
        series: [{
            name: 'System',
            colorByPoint: true,
            data: cancerBySystemObject
        }]
    });
}

/* End Draw the Cancer By System Chart  */








/* Begin Draw the Cancer By Histology Chart  */
function cancerByHistologyChart(chartData, cssID){

    cssDisplayArea = cssID;

    Highcharts.chart(cssDisplayArea, {
        credits: false,
        chart: {
            type: 'column'
        },
        title: {
            text: 'Cancers by histology'
        },
        xAxis: {
            type: 'category'
        },
        yAxis: {
            type: 'logarithmic',
            title: {
                text: 'Count of cancers by histology'
            }

        },
        legend: {
            enabled: false
        },
        plotOptions: {
            series: {
                borderWidth: 0,
                dataLabels: {
                    enabled: false,
                    formatter:function() {
                        var pcnt = (this.y / sumAll(this.series.data) ) * 100; // compute sum and percentage
                        return Highcharts.numberFormat(pcnt) + '% ';  // format to infuse percentage
                    }
                    //format: '{point.y/100}'
                }
            }
        },

        tooltip: {
            headerFormat: '<span style="font-size:11px">{series.name}</span><br>',
            pointFormat: '<span style="color:{point.color}">{point.name}</span>: <b>{point.y}</b> of total<br/>'
        },

        series: [{
            name: 'Cancers',
            colorByPoint: true,
            data: [{
                name: 'Carcinoma', y: 713, drilldown: 'Carcinoma'
            }, {
                name: 'Sarcoma', y: 45, drilldown: 'Sarcoma'
            }, {
                name: 'malignant glioma', y: 34, drilldown: null
            }, {
                name: 'melanoma', y: 16, drilldown: null
            }, {
                name: 'leukemia', y: 3, drilldown: null
            }, {
                name: 'lymphoma', y: 2, drilldown: null
            }, {
                name: 'neuroblastoma', y: 2, drilldown: null
            }, {
                name: 'malignant mesothelioma', y: 1, drilldown: null
            }, {
                name: 'teratoma', y: 1, drilldown: null
            }]
        }], drilldown: {
            series: [{
                name: 'Carcinoma',
                id: 'Carcinoma',
                data: [
                    ['adenocarcinoma', 532],
                    ['carcinoma', 87],
                    ['squamous cell carcinoma', 42],
                    ['non small cell carcinoma', 38],
                    ['small cell carcinoma', 5],
                    ['papillary serous adenocarcinoma', 3],
                    ['neuroendocrine carcinoma', 2],
                    ['carcinosarcoma', 1],
                    ['large cell carcinoma', 1],
                    ['papillary carcinoma', 1],
                    ['transitional cell carcinoma', 1],
                ]
            }, {
                name: 'Sarcoma',
                id: 'Sarcoma',
                data: [
                    ['sarcoma', 34],
                    ['rhabdomyosarcoma', 6],
                    ['osteosarcoma', 2],
                    ['carcinosarcoma', 1],
                    ['liposarcoma', 1],
                    ['spindle cell sarcoma', 1],
                ]
            }]
        }
    });

}

/* End Draw the Cancer By Histology Chart  */










/* Begin Draw the Cancer By Tissue Chart  */

function cancerByTissueChart(chartData,cssID){

    cssDisplayArea = cssID;

    Highcharts.chart(cssDisplayArea, {
        credits: false,
        chart: {
            type: 'column'
        },
        title: {
            text: 'Cancers by tissue'
        },
        xAxis: {
            type: 'category',
            labels: {
                rotation: -90,
                style: {
                    fontSize: '11px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        },
        yAxis: {
            type: 'logarithmic',
            title: {
                text: 'Count of cancers'
            }
        },
        legend: {
            enabled: false
        },
        tooltip: {
            pointFormat: 'Count: <b>{point.y}</b>'
        },
        series: [{
            name: 'Tissue',
            data: [
                ['colon cancer', 496],
                ['pancreatic cancer', 19],
                ['rectum cancer', 10],
                ['ampulla of vater cancer', 4],
                ['bile duct cancer', 4],
                ['cecum carcinoma', 3],
                ['liver cancer', 2],
                ['salivary gland cancer', 2],
                ['tonsil cancer', 2],
                ['anal cancer', 2],
                ['stomach cancer', 2],
                ['appendix cancer', 2],
                ['esophageal cancer', 1],
                ['duodenum cancer', 1],
                ['lung cancer', 77],
                ['brain cancer', 34],
                ['peripheral nervous system cancer', 1],
                ['retroperitoneal cancer ', 1],
                ['adrenal gland cancer', 1],
                ['breast cancer', 31],
                ['urinary bladder cancer', 29],
                ['kidney cancer', 10],
                ['skin cancer', 20],
                ['muscle cancer', 17],
                ['bone cancer', 12],
                ['lipomatous cancer', 1],
                ['ovarian cancer', 13],
                ['prostate cancer', 3],
                ['uterine cancer', 1],
                ['endometrial cancer', 1],
                ['germ cell cancer', 1],
                ['connective and soft tissue cancer', 13],
                ['hematologic cancer', 5],
                ['head cancer', 5],
                ['neck cancer', 1]
            ],
            dataLabels: {
                crop: false,
                overflow: 'none',
                padding: 2,
                enabled: false,
                rotation: -90,
                align: 'left',

                formatter:function() {
                    var pcnt = (this.y / sumAll(this.series.data) ) * 100; // compute sum and percentage
                    return Highcharts.numberFormat(pcnt) + '% ';  // format to infuse percentage
                },
                //format: '{point.y}'

                y: -5,
                style: {
                    fontSize: '11px',
                    fontFamily: 'Verdana, sans-serif'
                }
            }
        }]
    });

}

/* End Draw the Cancer By Tissue Chart  */





