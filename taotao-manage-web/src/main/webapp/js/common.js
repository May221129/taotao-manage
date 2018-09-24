Date.prototype.format = function(format){ 
    var o =  { 
    "M+" : this.getMonth()+1, //month 
    "d+" : this.getDate(), //day 
    "h+" : this.getHours(), //hour 
    "m+" : this.getMinutes(), //minute 
    "s+" : this.getSeconds(), //second 
    "q+" : Math.floor((this.getMonth()+3)/3), //quarter 
    "S" : this.getMilliseconds() //millisecond 
    };
    if(/(y+)/.test(format)){ 
    	format = format.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length)); 
    }
    for(var k in o)  { 
	    if(new RegExp("("+ k +")").test(format)){ 
	    	format = format.replace(RegExp.$1, RegExp.$1.length==1 ? o[k] : ("00"+ o[k]).substr((""+ o[k]).length)); 
	    } 
    } 
    return format; 
};

var TT = TAOTAO = {//创建一个TAOTAO对象
	// 编辑器参数
	kingEditorParams : {
		filePostName  : "uploadFile", // 图片上传时表单提交时 input名称；需要自定义。
		uploadJson : '/rest/pic/upload', // 图片上传的地址；需要自定义。
		dir : "image" // 类型，图片；这个可以不改动。
	},
	// 格式化时间
	formatDateTime : function(val,row){
		var now = new Date(val);
    	return now.format("yyyy-MM-dd hh:mm:ss");
	},
	// 格式化连接
	formatUrl : function(val,row){
		if(val){
			return "<a href='"+val+"' target='_blank'>查看</a>";			
		}
		return "";
	},
	// 格式化价格
	formatPrice : function(val,row){
		return (val/100).toFixed(2);
	},
	// 格式化商品的状态
	formatItemStatus : function formatStatus(val,row){
        if (val == 1){
            return '正常';
        } else if(val == 2){
        	return '<span style="color:red;">下架</span>';
        } else {
        	return '未知';
        }
    },
    
    init : function(data){
    	this.initPicUpload(data);
    	this.initItemCat(data);
    },
    // 初始化图片上传组件
    initPicUpload : function(data){
    	$(".picFileUpload").each(function(i,e){
    		var _ele = $(e);
    		_ele.siblings("div.pics").remove();//在同级里面找“带pics的div”并将其删除
    		_ele.after('\
    			<div class="pics">\
        			<ul></ul>\
        		</div>');
    		// 回显图片
        	if(data && data.pics){
        		var imgs = data.pics.split(",");
        		for(var i in imgs){
        			if($.trim(imgs[i]).length > 0){
        				_ele.siblings(".pics").find("ul").append("<li><a href='"+imgs[i]+"' target='_blank'><img src='"+imgs[i]+"' width='80' height='50' /></a></li>");
        			}
        		}
        	}
        	$(e).unbind('click').click(function(){//为了防止有重复绑定，所以先解绑，再重新绑定。
        		//找到按钮最近的一个form对象
        		//parentsUntil("form")：查找当前元素的所有父辈元素，知道遇到匹配的form元素为止，但这方方法返回值并不包含form元素，所以需要再.parent("form")。
        		//还可以这么实现：var form = $(this).parentsUntil("div");    因为这里form元素的上一级就是div元素。
        		var form = $(this).parentsUntil("form").parent("form");
        		KindEditor.editor(TT.kingEditorParams).loadPlugin('multiimage',function(){
        			var editor = this;
        			editor.plugin.multiImageDialog({
						clickFn : function(urlList) { // 点击“全部插入”时所用。下面的代码就是根据实际情况来改动的。
							var imgArray = [];
							KindEditor.each(urlList, function(i, data) {
								imgArray.push(data.url);
								form.find(".pics ul").append("<li><a href='"+data.url+"' target='_blank'><img src='"+data.url+"' width='80' height='50' /></a></li>");
							});
							form.find("[name=image]").val(imgArray.join(","));
							editor.hideDialog();
						}
					});
        		});
        	});
    	});
    },
    
    // 初始化选择类目组件
    initItemCat : function(data){
    	//i: index（第几个）
    	//e: element(要循环的对象)
    	$(".selectItemCat").each(function(i,e){//为什么这里要做循环：有多个类目时可用
    		var _ele = $(e);
    		if(data && data.cid){
    			_ele.after("<span style='margin-left:10px;'>" + data.cid + "</span>");
    		}else{
    			_ele.after("<span style='margin-left:10px;'></span>");
    		}
    		_ele.unbind('click').click(function(){
    			//$("<div>")：有<>就是创建一个div，没有<>就是选择一个div
    			$("<div>").css({padding:"5px"}).html("<ul>").window({
    				width:'500',
    			    height:"450",
    			    modal:true,
    			    closed:true,
    			    iconCls:'icon-save',
    			    title:'选择类目',
    			    onOpen : function(){
    			    	var _win = this;//this:当前窗口，即div
    			    	$("ul",_win).tree({//$("ul",_win):在当前的窗口"_win"中去查找ul，如果不写_win，就表示是在当前的整个页面中去查找ul
    			    		url:'/rest/item/cat',
    			    		animate:true,
    			    		method : "GET",//easyUI的tree组件的默认值是post，这里需要手动修改。
    			    		onClick : function(node){
    			    			if($(this).tree("isLeaf",node.target)){
    			    				// 填写到cid中
    			    				_ele.parent().find("[name=cid]").val(node.id);
    			    				_ele.next().text(node.text).attr("cid",node.id);
    			    				$(_win).window('close');
    			    				if(data && data.fun){//如果data存在，并且data中的fun也存在，就执行大括号里的逻辑：
    			    					// 执行传入的方法。
    			    					// 参数：第一个参数一般固定为this，从第二个参数开始，是定义方法中的参数
    			    					//注：data方法是在item-add.jsp中低70行定义的。
    			    					data.fun.call(this,node);
    			    				}
    			    			}
    			    		}
    			    	});
    			    },
    			    onClose : function(){
    			    	$(this).window("destroy");
    			    }
    			}).window('open');
    		});
    	});
    },
    
    createEditor : function(select){
    	return KindEditor.create(select, TT.kingEditorParams);
    },
    
    /**
     * 创建一个窗口，关闭窗口后销毁该窗口对象。<br/>
     * 
     * 默认：<br/>
     * width : 80% <br/>
     * height : 80% <br/>
     * title : (空字符串) <br/>
     * 
     * 参数：<br/>
     * width : <br/>
     * height : <br/>
     * title : <br/>
     * url : 必填参数 <br/>
     * onLoad : function 加载完窗口内容后执行<br/>
     * 
     * 
     */
    createWindow : function(params){
    	$("<div>").css({padding:"5px"}).window({
    		width : params.width?params.width:"80%",
    		height : params.height?params.height:"80%",
    		modal:true,
    		title : params.title?params.title:" ",
    		href : params.url,
		    onClose : function(){
		    	$(this).window("destroy");
		    },
		    onLoad : function(){
		    	if(params.onLoad){
		    		params.onLoad.call(this);
		    	}
		    }
    	}).window("open");
    },
    
    closeCurrentWindow : function(){
    	$(".panel-tool-close").click();
    },
    
    changeItemParam : function(node,formId){
    	$.ajax({
			   type: "GET",
			   url: "/rest/item/param/" + node.id,
			   statusCode:{
				   200 : function(data){
					   $("#"+formId+" .params").show();
						 var paramData = JSON.parse(data.paramData);
						 var html = "<ul>";
						 for(var i in paramData){
							 var pd = paramData[i];
							 html+="<li><table>";
							 html+="<tr><td colspan=\"2\" class=\"group\">"+pd.group+"</td></tr>";
							 
							 for(var j in pd.params){
								 var ps = pd.params[j];
								 html+="<tr><td class=\"param\"><span>"+ps+"</span>: </td><td><input autocomplete=\"off\" type=\"text\"/></td></tr>";
							 }
							 
							 html+="</li></table>";
						 }
						 html+= "</ul>";
						 $("#"+formId+" .params td").eq(1).html(html);
				   },
				   404 : function(){
					   $("#"+formId+" .params").hide();
						 $("#"+formId+" .params td").eq(1).empty();
				   },
				   500 : function(){
					   alert("error");
				   }
			   }
			});
    },
    getSelectionsIds : function (select){
    	var list = $(select);
    	var sels = list.datagrid("getSelections");
    	var ids = [];
    	for(var i in sels){
    		ids.push(sels[i].id);
    	}
    	ids = ids.join(",");
    	return ids;
    },
    
    /**
     * 初始化单图片上传组件 <br/>
     * 选择器为：.onePicUpload <br/>
     * 上传完成后会设置input内容以及在input后面追加<img> 
     */
    initOnePicUpload : function(){
    	$(".onePicUpload").click(function(){
			var _self = $(this);
			KindEditor.editor(TT.kingEditorParams).loadPlugin('image', function() {
				this.plugin.imageDialog({
					showRemote : false,
					clickFn : function(url, title, width, height, border, align) {
						var input = _self.siblings("input");
						input.parent().find("img").remove();
						input.val(url);
						input.after("<a href='"+url+"' target='_blank'><img src='"+url+"' width='80' height='50'/></a>");
						this.hideDialog();
					}
				});
			});
		});
    }
};
