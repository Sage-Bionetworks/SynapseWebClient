/*-------------------TWITTER FEED---------------*/

jQuery.noConflict()(function($){
	$(document).ready(function () {
		// start jqtweet!
		JQTWEET.loadTweets();
	});
});
	
/***************************************************
			Nivo Slider
***************************************************/
jQuery.noConflict()(function($){
$(document).ready(function() {
            $('#slider').nivoSlider({
                pauseTime:5000,
                pauseOnHover:false
            });        
    });
});
/***************************************************
			SuperFish Menu
***************************************************/	
// initialise plugins
	jQuery.noConflict()(function(){
		jQuery('ul.sf-menu').superfish();
	});

/***************************************************
			PRETTY PHOTO
***************************************************/
jQuery.noConflict()(function($){
$(document).ready(function() {  

$("a[rel^='prettyPhoto']").prettyPhoto({opacity:0.80,default_width:500,default_height:344,theme:'light_rounded',hideflash:false,modal:false});

});
});
/***************************************************
			LIST SLIDER
***************************************************/
jQuery.noConflict()(function($){
		$(document).ready(function() {

			$.featureList(
				$("#tabs li a"),
				$("#output li"), {
					start_item	:	1
				}
			);
		});
});

/***************************************************
			ACCORDION SLIDER
***************************************************/
jQuery.noConflict()(function($){
				$('.kwicks').kwicks({
					max : 900,
					spacing : 0
				});
			});
			
			
/***************************************************
			MOSAIC
***************************************************/
jQuery.noConflict()(function($){
				
				$('.circle').mosaic({
					opacity		:	0.8			//Opacity for overlay (0-1)
				});
				
				$('.fade').mosaic();
				
				$('.bar').mosaic({
					animation	:	'slide'		//fade or slide
				});
				
				$('.bar2').mosaic({
					animation	:	'slide'		//fade or slide
				});
				
				$('.bar3').mosaic({
					animation	:	'slide',	//fade or slide
					anchor_y	:	'top'		//Vertical anchor position
				});
				
				$('.cover').mosaic({
					animation	:	'slide',	//fade or slide
					hover_x		:	'400px'		//Horizontal position on hover
				});
				
				$('.cover2').mosaic({
					animation	:	'slide',	//fade or slide
					anchor_y	:	'top',		//Vertical anchor position
					hover_y		:	'100px'		//Vertical position on hover
				});
				
				$('.cover3').mosaic({
					animation	:	'slide',	//fade or slide
					hover_x		:	'400px',	//Horizontal position on hover
					hover_y		:	'300px'		//Vertical position on hover
				});
		    
		    });			
/***************************************************
			IMAGE HOVER
***************************************************/
jQuery.noConflict()(function($){
$(document).ready(function() {  
            $('.img-preview').each(function() {
                $(this).hover(
                    function() {
                        $(this).stop().animate({ opacity: 0.5 }, 400);
                    },
                   function() {
                       $(this).stop().animate({ opacity: 1.0 }, 400);
                   })
                });
});
});
jQuery.noConflict()(function($){
			$('#slides').slides({
				preload: true,
				generateNextPrev: false
			});
			$('#slides2').slides({
				preload: true,
				generateNextPrev: false,
				generatePagination: true
			});
		});
/***************************************************
			SlideOut
***************************************************/
		 


jQuery.noConflict()(function($){
    
    $('#example-1').tipsy();
    
    $('#north').tipsy({gravity: 'n'});
    $('#south').tipsy({gravity: 's'});
    $('#east').tipsy({gravity: 'e'});
    $('#west').tipsy({gravity: 'w'});
    
    $('#auto-gravity').tipsy({gravity: $.fn.tipsy.autoNS});
    
    $('.social').tipsy({fade: true});
	$('.service-tipsy').tipsy({fade: true, gravity: 's'});
    
    $('#example-custom-attribute').tipsy({title: 'id'});
    $('#example-callback').tipsy({title: function() { return this.getAttribute('original-title').toUpperCase(); } });
    $('#example-fallback').tipsy({fallback: "Where's my tooltip yo'?" });
    
    $('#example-html').tipsy({html: true });
    
  });		 
	
	
jQuery.noConflict()(function($){
$(document).ready(function() {
	$('ul#filter a').click(function() {
		$(this).css('outline','none');
		$('ul#filter .current').removeClass('current');
		$(this).parent().addClass('current');
		
		var filterVal = $(this).text().toLowerCase().replace(' ','-');
				
		if(filterVal == 'all') {
			$('ul#portfolio li.hidden').fadeIn('slow').removeClass('hidden');
		} else {
			
			$('ul#portfolio li').each(function() {
				if(!$(this).hasClass(filterVal)) {
					$(this).fadeOut('normal').addClass('hidden');
				} else {
					$(this).fadeIn('slow').removeClass('hidden');
				}
			});
		}
		
		return false;
	});
});
});

jQuery.noConflict()(function($){
$(document).ready(function() {
	$('ul#filter-sidebar a').click(function() {
		$(this).css('outline','none');
		$('ul#filter-sidebar .current').removeClass('current');
		$(this).parent().addClass('current');
		
		var filterVal = $(this).text().toLowerCase().replace(' ','-');
				
		if(filterVal == 'all') {
			$('ul#portfolio li.hidden').fadeIn('slow').removeClass('hidden');
		} else {
			
			$('ul#portfolio li').each(function() {
				if(!$(this).hasClass(filterVal)) {
					$(this).fadeOut('normal').addClass('hidden');
				} else {
					$(this).fadeIn('slow').removeClass('hidden');
				}
			});
		}
		
		return false;
	});
});
});	


jQuery.noConflict()(function($){
	$('#sti-menu').iconmenu({
		animMouseenter	: {
			'mText' : {speed : 400, easing : 'easeOutExpo', delay : 140, dir : -1},
			'sText' : {speed : 400, easing : 'easeOutExpo', delay : 280, dir : -1},
			'icon'  : {speed : 400, easing : 'easeOutExpo', delay : 0, dir : -1}
		},
		animMouseleave	: {
			'mText' : {speed : 400, easing : 'easeInExpo', delay : 140, dir : -1},
			'sText' : {speed : 400, easing : 'easeInExpo', delay : 0, dir : -1},
			'icon'  : {speed : 400, easing : 'easeInExpo', delay : 280, dir : -1}
		}
	});
});

jQuery.noConflict()(function($){
$('#ca-container').contentcarousel();
});
jQuery.noConflict()(function($) {
	$('#va-accordion').vaccordion();
});

/***************************************************
			TABS
***************************************************/
// Activating tabs
jQuery.noConflict()(function($){
	$(document).ready(function() {
		implement_tabs();
	});
});

/***************************************************
			TABLES
***************************************************/
jQuery.noConflict()(function($){
	$("table").tablesorter({debug: true});
});
