function getCancerTypesProviders() {
    // TODO: Replace URL with real API for providers and cancer types
    $.getJSON("https://gist.githubusercontent.com/meladawy/e6d959d887a67063952be5872432c304/raw/a25d7d813ffc39747599bb16b23e4bb0801b7973/providers_cancer_type.json", function(data) {
      var cancerTypesSource = $('#template-cancer-types').html();
      var providersSource = $('#template-providers').html();

      $('#cancer-types').html(Handlebars.compile(cancerTypesSource)(data));
      $('#providers').html(Handlebars.compile(providersSource)(data));
      
      // Reload tooltips. 
      $(document).foundation();
    })
    .error(function(err) {
      console.log(err);
    });
  }