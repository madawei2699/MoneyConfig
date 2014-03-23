/**
	@description 沪深300 modular
	@author xingben1
	@version 0.1
*/
var modular = (function($){
    var config = {marks:['gray','red','green']};
	var modular = {
		/**
			@description 十大重仓
		*/
		blue_clips:{
			delay:6000,
			cache:{},
			/**
				@description 更新页面试图
				@param {Object} data 数据对象;
				@param {Array} arr 行情数组;
			*/
			update:function(data,arr){
				var _render = function(stock,data_stock){
							var temp_obj = modular.blue_clips.cache[stock],
								amt = data_stock[4] == 0 ? data_stock[4] : (data_stock[4] - data_stock[3]) / data_stock[3] * 100,
								cls = config.marks[modular.helper.get_status(amt)];
							if(!temp_obj){
								//缓存查找的dom对象;
								temp_obj = {};
								temp_obj["obj"] = $("#blue-chips-" + stock);
								temp_obj["price"] = temp_obj["obj"].find(".price");
								temp_obj["amt"] = temp_obj["obj"].find(".amt");
							};
							temp_obj["price"].text(modular.helper.num_to_str(data_stock[4],2));
							temp_obj["amt"].addClass(cls).text(modular.helper.num_to_str(amt,2,"%"));
				};
				$.each(arr,function(key,stock){
					var data_stock = data[stock];
					_render(stock,data_stock);
				});
			},
			/**
				@description 获取十大重仓信息
			*/
			get_clips_info:function(){
				//从页面中获取fund_app.blue_chips;
				var clips_arr = fund_app.blue_chips;
				if(clips_arr && clips_arr.length > 0){
					(function(){
						StockHq.get_hq({stocks:clips_arr},function(data){
							modular.blue_clips.update(data.str,clips_arr);
						});
						setTimeout(arguments.callee,modular.blue_clips.delay);
					})();
				}
			}
		},
		helper:{
            /**
                @description 比较数值
                @param {Number} amt 需要比较的数值
                @param {Number} base 比较的基数
            */
            get_status:function( amt,base ){
                var base = parseFloat( base || 0 );
                amt = parseFloat( amt );
                if(isNaN(amt) || amt == base){
                    return 0;
                }else if(amt > base){
                    return 1;
                }else{
                    return 2;
                };
            },
            /**
                @description 格式化数值对象
                @param {Number} val 需要格式化的数值
                @param {Number} decimal 需要截取的小数位数
                @param {String} unit 数值单位
            */
            num_to_str:function (val, decimal, unit ){
                var amt = parseFloat( val );
                return ( isNaN( amt ) )  ? "--" : ( ( decimal === undefined ? amt : amt.toFixed( decimal ) ) + (unit || "") );
            }
        }
	};
	return modular;
})(jQuery)