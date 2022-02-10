/************************************************************************
*************************************************************************
@Name :       	jMenu - jQuery Plugin
@Revison :    	1.6
@Date : 		12/2010
@Author:     	ALPIXEL - (www.myjqueryplugins.com - www.alpixel.fr)
@Support:    	FF, IE7, IE8, MAC Firefox, MAC Safari
@License :		Open Source - MIT License : http://www.opensource.org/licenses/mit-license.php
 
**************************************************************************
*************************************************************************/

/** 
@ IsHovered Plugin 
@ Thanks to Chad Smith fr his isHovered Plugin 
@ source : http://mktgdept.com/jquery-ishovered
**/
;(function(b,c){b('*').hover(function(){b(this).data(c,1)},function(){b(this).data(c,0)}).data(c,0);b[c]=function(a){return b(a)[c]()};b.fn[c]=function(a){a=0;b(this).each(function(){a+=b(this).data(c)});return a>0}})(jQuery,'isHovered');


/** jMenu Plugin **/
(function($){

	$.jMenu = {
		/**************/
		/** OPTIONS **/
		/**************/
		defaults: {
			ulWidth : 'auto',
			absoluteTop : 30,
			absoluteLeft : 0,
			effects : {
				effectSpeedOpen : 350,
				effectSpeedClose : 350,
				effectTypeOpen : 'slide',
				effectTypeClose : 'slide',
				effectOpen : 'linear',
				effectClose : 'linear'
			},
			TimeBeforeOpening : 200,
			TimeBeforeClosing : 200,
			animatedText : false,
			paddingLeft: 7
		},
		
		/*****************/
		/** Init Method **/
		/*****************/
		init:function(options){
			/* vars **/
			opts = $.extend({}, $.jMenu.defaults, options);
			
			$("#jMenu a:not(.fNiv)").each(function(){
				var $thisChild = $(this);
				
				/* Add css - arrow right */
				if($.jMenu._IsParent($thisChild))
					$thisChild.addClass('isParent');
					
				/* Add the animation on hover **/
				if(opts.animatedText)
					$.jMenu._animateText($thisChild);
				
				/* Actions on hover */
				$thisChild.bind({
					mouseover:function(){
						$.jMenu._hide($thisChild);
						$.jMenu._showNextChild($thisChild);
					}
				});
			});
			
			/* Actions on parents links */
			$('#jMenu li a.fNiv').bind({
				mouseover:function(){
					var $this = $(this);
					var $child = $this.next();
					ULWidth = $.jMenu._returnUlWidth($this);
					$.jMenu._closeList($("#jMenu ul"));
					if($child.is(':hidden'))
						$.jMenu._showFirstChild($this);
				}
			});
			
			/* Close all when mouse  leaves */
			$('#jMenu').bind({
				mouseleave : function(){
					setTimeout(function(){$.jMenu._closeAll();},opts.TimeBeforeClosing);
				}
			});
		},
		
		
		/****************************
		*****************************
			jMenu Methods Below
		*****************************
		****************************/
		
		/** Show the First Child Lists **/
		_showFirstChild:function(el){
			
			if($.jMenu._IsParent(el))
			{
				var SecondList = el.next();
				
				if(SecondList.is(":hidden"))
				{
					var position = el.position();
					
					SecondList
					.css({
						top : position.top + opts.absoluteTop,
						left : position.left + opts.absoluteLeft,
						width : ULWidth
					})
					.children().css({
						width: ULWidth
					});
					
					$.jMenu._show(SecondList);
				}
			}
			else
				return false;
		},
		
		/** Show all others Child lists except the first list **/
		_showNextChild:function(el){
			if($.jMenu._IsParent(el))
			{
				var ChildList = el.next();
				if(ChildList.is(":hidden"))
				{
					var position = el.position();
					
					ChildList
					.css({
						top : position.top,
						left : position.left + ULWidth,
						width : ULWidth
					})
					.children().css({
						width:ULWidth
					});
					$.jMenu._show(ChildList);
					
				}
			}
			else
				return false;
		},
		
		
		/**************************************/
		/** Short Methods - Generals actions **/
		/**************************************/
		_hide:function(el){
			if($.jMenu._IsParent(el) && !el.next().is(':hidden')) 
				$.jMenu._closeList(el.next());
			else if(($.jMenu._IsParent(el) && el.next().is(':hidden')) || !$.jMenu._IsParent(el)) 
				$.jMenu._closeList(el.parent().parent().find('ul'));
			else
				return false;
		},
		
		_show:function(el) {
			switch(opts.effects.effectTypeOpen)
			{
				case 'slide':
					el.stop(true, true).delay(opts.TimeBeforeOpening).slideDown(opts.effects.effectSpeedOpen, opts.effects.effectOpen);
					break;
				case 'fade':
					el.stop(true, true).delay(opts.TimeBeforeOpening).fadeIn(opts.effects.effectSpeedOpen, opts.effects.effectOpen);
					break;
				default :
					el.stop(true, true).delay(opts.TimeBeforeOpening).show(opts.effects.effectSpeedOpen, opts.effects.effectOpen);
			}
		},
		
		_closeList:function(el) {
			switch(opts.effects.effectTypeClose)
			{
				case 'slide':
					el.slideUp(opts.effects.effectSpeedClose, opts.effects.effectClose);
					break;
				case 'fade':
					el.fadeOut(opts.effects.effectSpeedClose, opts.effects.effectClose);
					break;
				default :
					el.hide(opts.effects.effectSpeedClose, opts.effects.effectClose);
			}
			
		},
		
		_closeAll:function(){
			if(!$('#jMenu').isHovered()) {
				$('#jMenu ul').each(function(){
					$.jMenu._closeList($(this));
				});
			}
		},
		
		_IsParent:function(el) {
			if(el.next().is('ul')) return true;
			else return false;
		},
		
		_returnUlWidth:function(el) {
			switch(opts.ulWidth) {
				case "auto" :
					ULWidth = parseInt(el.parent().outerWidth());
					break;
				default :
					ULWidth = parseInt(opts.ulWidth);
			}
			return ULWidth;
		},
		
		_animateText:function(el) {
			var paddingInit = parseInt(el.css('padding-left'));
			
			el.hover(function(){
				$(this)
				.stop(true,true)
				.animate({
					paddingLeft: paddingInit + opts.paddingLeft
				}, 100);
			}, function(){
				$(this)
				.stop(true,true)
				.animate({
					paddingLeft:paddingInit
				}, 100);
			});
		},
		
		_isReadable:function(){
			if($("a.fNiv").length > 0)	return true;
			else return false;
		},
		
		_error:function(){
			alert('Please, check you have the \'.fNiv\' class on your first level links.');
		}
	};
	
	jQuery.fn.jMenu = function(options){
		if($.jMenu._isReadable())
			$.jMenu.init(options);
		else
			$.jMenu._error();
	};
})(jQuery); 