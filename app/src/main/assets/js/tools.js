//闭包
(function($) {
//类级别
jQuery.tools = {
		//提示
		t_showTips : function(str, callback) {
			var tipsHtml = '<div style="position: fixed;left: 0px;right: 0px;top:50%;text-align: center;z-index: 9999;width:90%;margin:0 auto;"><div id="t_showTips"><div class="dialog-body"></div></div></div>';
			if ($("#t_showTips").length == 0) {
				$(document.body).append(tipsHtml);
			}
			$("#t_showTips .dialog-body").html(str);

			var $parent = $("#t_showTips").parent();
			$parent.css({
				"margin-top" : $parent.height() / 2 * -1 + "px",
				"display" : "block"
			});
		
			setTimeout(function() {
				$parent.css("display","none");
				if(typeof(callback)!="undefined")callback();
			}, 2000);
		},
		//滚动监听
		t_scroll : function(isLoad,callback) {
			$(window).scroll(function(){
				//获取浏览器的高度
				var wHeight = $(window).height();
				//获取浏览器滚动条的高度
				var dHeight = $(document).height();
				//获取浏览器滚动条距离顶部的高度
				var scrollTop = $(document).scrollTop();
				//判断什么时候该触发事件
				if(scrollTop>= dHeight-wHeight){
					if(isLoad()){
						if($("#t_scroll_loadbar>.loading").length>0)return;
						if($("#t_scroll_loadbar").length==0){
							$(document.body).append('<div id="t_scroll_loadbar"></div>');
						}
						$("#t_scroll_loadbar").html('<span class="loading"></span>正在加载...');
						setTimeout(function() {
							callback();
							
							setTimeout(function() {
								$("#t_scroll_loadbar").html("");
							}, 500);
						}, 500);
					}
				}
			});
		},
		//遮罩层
		t_mask : function(isShow,zIndex){
			if(isShow){
				var maskHtml = '<div id="t_mask"></div>';
				if ($("#t_mask").length == 0) {
					$(document.body).append(maskHtml);
				}
				if(!zIndex){
					zIndex=900;
				}
				$("#t_mask").css("z-index",zIndex);
				$("#t_mask").show();
			}
			else{
				$("#t_mask").hide();
			}
		}/*,
		//input清空的xxx
		t_input_clear : function(object){
			$(object).after('<span class="t_input_clear" style="z-index:-9;"></span>');
			var height = parseInt($(object).css("padding-top"))+parseInt($(object).css("padding-bottom"));
			var marginLeft = $(object).width()+2;
			$(object).next(".t_input_clear").first().css({"height":$(object).height()+height+3+"px","left":marginLeft-5});
			$(object).css({"padding-right":"10px","margin-left":"-11px"});
			$(object).on("input",function(){
				if($(this).val()!=""){
					$(object).next(".t_input_clear").css("z-index","9");
				}
				else{
					$(object).next(".t_input_clear").css("z-index","-9");
				}
			});
			$(object).next(".t_input_clear").first().on("click",function(){
				$(object).val("");
			});
		}*/
};
//对象级别
$.fn.tools = function(){
};
})(jQuery); 