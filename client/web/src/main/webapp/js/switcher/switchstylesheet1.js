  jQuery(document).ready(function($) {
    
    $('.slide-out-div a.color-box').each(function (i) {
        var a = $(this);
        a.css({
            backgroundColor: '#' + a.attr('rel')
        })
    })    
    
    $('.slide-out-div a.pattern-box').each(function (i) {  
      var a = $(this);
        var patternUrl = 'url(images/pattern/' + a.attr('rel') + '.png)';
        a.css({
            backgroundImage: patternUrl
        })
    })
    
   var switcher_skins = $('.slide-out-div a.color-box');
   var switcher_link = $('#skins-switcher');
   switcher_skins.each(function(i) {
    var color = $(this).attr('rel');
    var defaultPattern = "url('../images/pattern/cloud.png')";
     
 
     
   });  
   
      
   switcher_skins.click(function(e) {
    var color = $(this).attr('rel');
    var skins;
    var defaultPattern = "url('../images/pattern/cloud.png')";
    
    if (color == "ff0099") {
      var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });   
    }
    if(color == "00cc33") {
       var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });   
    }
    if(color == "0099FF") {
      var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });    
    }
    if(color == "ffffff") {
      var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });     
    }
    if(color == "000000") {
      var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });    
    }
    if(color == "e1e1e1") {
      var atrrHref = switcher_link.attr('href');
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });       
    }     
    $.cookie("soul_cookie_pattern", null);   
    $.cookie("soul_cookie_bgimage",null);

    $.cookie("soul_cookie_color", color);  
    $.cookie("soul_cookie_skins", atrrHref);
    $.cookie("soul_cookie_defaultBg", defaultPattern);    
    return false;
   });  
   
  var color = $.cookie("soul_cookie_color");
  var soul_skins = $.cookie("soul_cookie_skins");
  var defaultPattern = $.cookie("soul_cookie_defaultBg");
  var pattern = $.cookie("soul_cookie_pattern");
  
  if (soul_skins) {
    $("#skins-switcher").attr("href",soul_skins);
    $('body').css({
        backgroundColor: '#' + color,
        backgroundImage : pattern
    });
  }

  $('.slide-out-div a.pattern-box').click(function (e) {
      e.preventDefault();
      var patternUrl = 'url(images/pattern/' + $(this).attr('rel') + '.png)';
      $('body').css({
          backgroundImage: patternUrl,
          backgroundRepeat: "repeat"
      });
      $.cookie("soul_cookie_bgimage",null);
      $.cookie("soul_cookie_pattern", patternUrl)
  });
  
  var defaultPattern = $.cookie("soul_cookie_defaultBg");
  var color = $.cookie("soul_cookie_color");
  var background = $.cookie("soul_cookie_bgimage");
  if (color) {
      $('body').css({
          backgroundColor: '#' + color,
          backgroundImage : defaultPattern
      });
  }
  var pattern = $.cookie("soul_cookie_pattern");
  if (pattern) {
      $('body').css({
          backgroundImage: pattern,
          backgroundRepeat: "repeat"
      });
  } else {
    if (background) {
        $('body').css({
          backgroundImage: background,
          backgroundRepeat: "norepeat",
          backgroundPosition: "top center",
          backgroundAttachment: "fixed"
        });
    }    
  }  

  $('.slide-out-div a.bg-box').each(function (i) {
    var backgroundUrl = 'url(images/' + $(this).attr('rel') + '.jpg)';
    var a = $(this);
      a.css({
          backgroundImage: backgroundUrl
      })
  })
    
  $('.slide-out-div a.bg-box').click(function (e) {
      e.preventDefault();
      var backgroundUrl = 'url(images/' + $(this).attr('rel') + '.jpg)';
      $('body').css({
          backgroundImage: backgroundUrl,
          backgroundRepeat: "norepeat",
          backgroundPosition: "top center",
          backgroundAttachment: "fixed"
      });
    $.cookie("soul_cookie_bgimage",backgroundUrl)
  });

  var background = $.cookie("soul_cookie_bgimage");
  if (background) {
      $('body').css({
        backgroundImage: background,
        backgroundRepeat: "norepeat",
        backgroundPosition: "top center",
        backgroundAttachment: "fixed"
      });
  }
         
});   
 