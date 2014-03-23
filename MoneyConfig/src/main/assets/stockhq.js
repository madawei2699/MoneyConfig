var StockHq = (function($){
    var config = {
            loop:false,
            delay:3000
    };
	var types = {
			url:'http://hq.sinajs.cn/',
			stock:{
					func:function(stocks, callback){
						var stocks_strs = {},
							stocks_basic_strs = {};
						$.each(stocks,function(i,v){
							//alert(window["hq_str_" + v]);
							if(typeof window["hq_str_" + v] !== "undefined"){
								var stock_code_arr,postfix,code;
								if(v.indexOf("_") > 0){
									//stock_code_arr = v.split("_");
									postfix = v;
									code = postfix.replace("$",'.');
								}else{
									postfix = v;
									code = v;
								};
								if(v.lastIndexOf("_i") < 0 ){
									//存储股票行情信息;
									stocks_strs[postfix] = window["hq_str_" + v].split(",");
									//将股票代码添加到数组的最前端;
									stocks_strs[postfix].unshift(code);
									//相应股票基础信息数据;
								}else{
									stocks_basic_strs[postfix] = window["hq_str_" + v].split(",");
								};
							};
						})
						//调用回调函数
						if($.isFunction(callback)){
							callback({
								str:stocks_strs,
								basic_str:stocks_basic_strs
							});
						};
					}
			},
            hk:{
                    func:function(stocks, callback){
                         var stocks_strs = {},
							stocks_basic_strs = {};
						$.each(stocks,function(i,v){
							//alert(window["hq_str_" + v]);
							if(typeof window["hq_str_" + v] !== "undefined"){
								if(v.lastIndexOf("_i") < 0 ){
									//存储股票行情信息;
									stocks_strs[v] = window["hq_str_" + v].split(",");
									//将股票代码添加到数组的最前端;
									stocks_strs[v].unshift(v.substring(2));
									//相应股票基础信息数据;
								}else{
									stocks_basic_strs[v] = window["hq_str_" + v].split(",");
								};
							};
						})
						//调用回调函数
						if($.isFunction(callback)){
							callback({
								str:stocks_strs,
								basic_str:stocks_basic_strs
							});
						};
					}
            },
			us:{
					func:function(stocks, callback){

					}
			}
	}
	var _StockHq = {
			max_len:30,
			split:function(arr){
				var len = arr.length,
					max_len = _StockHq.max_len,
					group = Math.ceil(len / max_len),
					temp_arr = [];
					for(var i=0;i<group;i++){
						var temp_clone = arr.concat([]);
						temp_arr[i] = temp_clone.splice( i * max_len, max_len);
					};
					return temp_arr;
			},
			_timedProcessArray:function(items, process, callback){
				var todo = items.concat(); //create a clone of the original
				setTimeout(function(){
					var start = +new Date();
					do {
						process(todo.shift());
					} while (todo.length > 0 && (+new Date() - start < 50));
					if (todo.length > 0){
						setTimeout(arguments.callee, 25);
					} else {
						callback(items);
					}
				}, 25);
			},
			_loadScript: function(url, opt, callback){
				var script = document.createElement("script")
				script.type = "text/javascript";
				if (script.readyState){ //IE
					script.onreadystatechange = function(){
						if (script.readyState == "loaded" || script.readyState == "complete"){
							script.onreadystatechange = null;
							if($.isFunction(callback)){
								callback();
							};
							script.parentNode.removeChild(script);
						}
					};
				} else { //Others
					script.onload = function(){
						if($.isFunction(callback)){
							callback();
						};
						script.parentNode.removeChild(script);
					};
				};
				script.charset = opt.charset || 'gb2312';
				script.src = url;
				document.getElementsByTagName("head")[0].appendChild(script);
			},
			_fetchData:function(url, opt, func, callback){
				var stamp = (new Date()).getTime();
                if(opt.extra_stocks && opt.extra_stocks.length > 0){
                    opt.stocks = opt.stocks.concat(opt.extra_stocks);
                };
				var stocks = opt.stocks,
                    len = stocks.length;
				//判断stocks数组的长度;
				if(len > _StockHq.max_len){
					var stocks = _StockHq.split(stocks);
					/*var stock_info = {
						str:{},
						basic_str:{}
					};*/
					//加载股票行情信息;
					var _process = function(arr){
							var stock_code = arr.join(",");
							_StockHq._loadScript(url + "?_=" + stamp + "/"+ "&" + "list=" + stock_code, opt,function(){
								func(arr,callback);
								/*function(data){
									$.extend(stock_info.str,data.str);
									$.extend(stock_info.basic_str,data.basic_str);
								});*/
							})
					};
					_StockHq._timedProcessArray(stocks, _process, function(arr){
						//callback(stock_info);
					})
				}else{
					var stock_code = stocks.join(",");
					_StockHq._loadScript(url + "?_=" + stamp + "/"+ "&" + "list=" + stock_code, opt, function(){
						func(stocks, callback);
					});
				}
			}
	};
	return {
		get_hq:function(opt,callback){
			var loop = typeof opt.loop !== "undefined" ? opt.loop : config.loop,
				delay = opt.delay || config.delay;
			(function(){
				_StockHq._fetchData(types.url, opt, types.stock.func,callback);
				if(loop == true && delay !== 'undefined'){
					setTimeout(arguments.callee,delay);
				}
			})();
		},
		get_us_hq:function(opt, callback){
			var loop = typeof opt.loop !== "undefined" ? opt.loop : config.loop,
				delay = opt.delay || config.delay;
			opt.stocks = $.map(opt.stocks,function(val){
				return "gb_" + val.replace(".","$");
			});
			(function(){
				_StockHq._fetchData(types.url, opt, types.stock.func,callback);
				if(loop == true && delay !== 'undefined'){
					setTimeout(arguments.callee,delay);
				}
			})();
		},
        get_hk_hq:function(opt, callback){
			var loop = typeof opt.loop !== "undefined" ? opt.loop : config.loop,
				delay = opt.delay || config.delay;
			opt.stocks = $.map(opt.stocks,function(val){
				return "hk" + val;
			});
			(function(){
				_StockHq._fetchData(types.url, opt, types.hk.func,callback);
				if(loop == true && delay !== 'undefined'){
					setTimeout(arguments.callee,delay);
				}
			})();
		},
        get_fund_hq:function(opt, callback){
            var loop = typeof opt.loop !== "undefined" ? opt.loop : config.loop,
				delay = opt.delay || config.delay;
			opt.stocks = $.map(opt.stocks,function(val){
                /*if(opt.type == "rate"){
			    	return "f_" + val;
                }else{
                   var fst_char = (val+"").charAt(0);
                   if(fst_char == "5"){
                      return "sh" + val;
                   }else if(fst_char == "1"){
                      return "sz" + val;
                   }else{
                      return val;
                   }
                }*/
                return  opt.type == "rate" ? "f_" + val : val;
			});
			(function(){
				_StockHq._fetchData(types.url, opt, types.stock.func,callback);
				if(loop == true && delay !== 'undefined'){
					setTimeout(arguments.callee,delay);
				}
			})();
        }
	}
})(jQuery);