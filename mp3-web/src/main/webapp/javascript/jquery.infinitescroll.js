(function($){
  $.fn.infinitescroll = function(options){
    return $(this).each(function(){
      var settings = $.extend(options);
      var el = $(settings.appendTo);
      var req = null;
      var maxReached = false;
      var infinityRunner = function(){
        if (settings.url !== null) {
          if (settings.force || (settings.triggerAt >= (el.attr("scrollHeight") - el.height() - el.scrollTop()))) {
            settings.force = false;
            
            // if the request is in progress, exit and wait for it to finish
            if (req && req.readyState < 4 && req.readyState > 0) {
              return;
            }
            $(settings.appendTo).trigger('infinitescroll.beforesend');
            req = $.get(settings.url, 'skip=' + settings.skip, function(data){
              if ((data !== '') && ($(data).size() > 1)) {
                if (settings.skip > 1) {
                  $(settings.appendTo).append($(data).next());
                }
                else {
                  $(settings.appendTo).html($(data).next());
                }
                settings.skip += settings.step;
                $(settings.appendTo).trigger('infinitescroll.finish');
              }
              else {
                maxReached = true;
                $(settings.appendTo).trigger('infinitescroll.maxreached');
              }
            }, 'html');
          }
        }
      };
      
      el.unbind("infinitescroll.scrollpage");
      el.unbind("scroll");
      
      el.bind("infinitescroll.scrollpage", function(e, skip){
        settings.skip = skip;
        settings.force = true;
        infinityRunner();
      });
      
      el.scroll(function(e){
        if (!maxReached) {
          infinityRunner();
        }
      });
      
      infinityRunner();
    });
  };
})(jQuery);
