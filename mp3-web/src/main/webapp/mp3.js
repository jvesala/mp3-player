  // Load jQuery
  google.load("jquery", "1");

  $.fn.textEvent = function(type, func) {
    this[type]("keyup input paste", function() {
      setTimeout((function(instance, method) {
        return function() {
          return method.apply(instance, arguments);
        };
      })(this, func), 10);
    });
  };