var searchString = "";
var search = null;

$.fn.textEvent = function(type, func) {
  this[type]("keyup input paste", function() {
    setTimeout((function(instance, method) {
      return function() {
        return method.apply(instance, arguments);
      };
    })(this, func), 10);
  });
};

function setResults(data) {
  $("#results").html(data);
  $("#indicator").hide();
}

function searchTrack(query) {
  var searchUrl = "/servlet/search/" + query;
  $("#indicator").show();

  $.ajax({
    url: searchUrl,
    dataType: "html",
    success: function(data) {
      if (query == searchString) {
        setResults(data);
        $("#results").scrollTop(0);
        $(window).infinitescroll({
          url: searchUrl,
          triggerAt: 150,
          skip: 50,
          step: 50,
          appendTo: "#results"
        });
      }
    }
  });
}

$(function() {
  $("#searchstring").textEvent('bind', function() {
    clearTimeout(search);
    search = setTimeout(function () {
      searchString = $("#searchstring").val();
      searchTrack(searchString);
    }, 600);
  });
  searchTrack("");
});

