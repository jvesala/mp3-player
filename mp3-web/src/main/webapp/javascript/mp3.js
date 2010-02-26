var emptySearch = "Kirjoita jotain hakeaksesi. Sata ensimm&auml;ist&auml; osumaa n&auml;ytet&auml;&auml;n.";
var searchString;
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
  $("#indicator").css("background-image", "none");
}

/**
function searchTrack(query) {
  if (query == "") {
    setResults(emptySearch);
    return;
  }
  $.get("/servlet/search/" + query, function(data) {
    if (query == searchString) {
      setResults(data);
    }
  });
}
*/


function searchTrack(query) {
  var searchUrl = "/servlet/search/" + query;

  $("#indicator").css("background-image", "url('reikajalka.gif')");

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
  setResults(emptySearch);
});

