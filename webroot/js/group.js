//var serveUrl = "http://10.1.2.24:8080/tb";
var serveUrl = "";
var httpsUrl = "https://tuangou.51mantuo.com/";

var haha={
	/*
	 * init()初始化
	 */
	init:function(){
		
		(function (doc, win) {
		  var docEl = doc.documentElement,
			resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
			recalc = function () {
			  var clientWidth = docEl.clientWidth;
			  if (!clientWidth) return;
			  var size = 100 * (clientWidth / 750);
			  docEl.style.fontSize =(size < 100 ? size : 100) + 'px';
			};
		  if (!doc.addEventListener) return;
		  win.addEventListener(resizeEvt, recalc, false);
		  doc.addEventListener('DOMContentLoaded', recalc, false);
		})(document, window);
		
	},
	/*
	 * getQuery获取url参数
	 * variable 参数键
	 * eg:http://www.runoob.com/index.php?id=1&image=awesome.jpg
	 * 获取id或者image的值
	 */
	getQuery:function(variable){
		var query = window.location.search.substring(1);
		var vars = query.split("&");
		for(var i=0;i<vars.length;i++){
           var pair = vars[i].split("=");
           if(pair[0] == variable){
           		return pair[1];
           }
        }
       return false;
	}
};
