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

function searchTrack(query) {
  var searchUrl = "/servlet/search/" + query;
  showIndicator();
  setClear(query);
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

function showIndicator() {
  $("#indicator").show();
  $("#count").hide();
}

function setResults(data) {
  $("#results").html(data);
  showCount($(".resultCount").html());
}

function showCount(count) {
  $("#indicator").hide();
  var html = "Ei osumia.";
  if (count == 1) {
    html = "1 osuma.";
  } else if (count > 1) {
    html = count + " osumaa";
  }
  $("#count").show().html(html);
}

function setClear(query) {
  if (query == "") {
    $("#clear").hide();
  } else {
    $("#clear").show();
  }
}

function clearSearch() {
  searchString = "";
  $("#searchstring").val("");
  searchTrack("");
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
  $("#searchstring").focus();
});

