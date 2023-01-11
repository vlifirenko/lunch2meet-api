QUnit.config.reorder = false;

var base_url = 'http://localhost:8080/XAPI';
function getJson(url, method, json, callback, bravaisHeader) {
  //var str = "{\"aid\":\"lrs\", \"tid\":\"test\", \"uid\":1}"
  var encStrign = 'ufEsQJOUkV7GKs98hXM9SSADPjIP9MHUMZQljZibRoIHNpzg8DgrlFvk8dYwrh3QrzSkjZRYhlHuOulDU+hzPHdvtUDmce1nd8BTRYiyhCjq9m+tdvaboHswyO/0jomBUL7skPYXTHamK+ejnu0jxwZP/YyyqRIu'
  bravaisHeader = (typeof bravaisHeader !== 'undefined') ? bravaisHeader : {"Bravais-Context": encStrign};
  bravaisHeader["X-Experience-API-Version"] = "1.0";
  $.ajax({
    type:        method,
    dataType:    "json",
    data:        JSON.stringify(json),
    headers:     bravaisHeader,
    //data:        json,
    contentType: "application/json",
    url:         url,
    success:     function(response, textStatus, jqXHR) {
      callback(response, jqXHR.status);
    },
    error: function(jqXHR) {
      callback(jqXHR, jqXHR.status);
    }
  });
}

function dropDb() {
  test("drop db", function() {
    stop();
    getJson(base_url + '/data/drop', 'GET', {}, function(response, status) {
      equal(parseInt(status), 200);
      start();
    });
  });
}
