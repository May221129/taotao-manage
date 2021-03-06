package com.taotao.manage.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.ItemCatResult;
import com.taotao.manage.service.ItemCatService;

@RequestMapping("api/item/cat")
@Controller
public class ApiItemCatController {
	
	@Autowired
	private ItemCatService itemCatService;
	
	//因为不知道ObjectMapper是否是线程安全的，所以用下面这种new的方式来做是最妥当的：
	private final static ObjectMapper objectMapper = new ObjectMapper();
	
	/**
	 * 对外提供接口服务，查询所有的类目数据。
	 * 最终返回jsonp类型，但是这里还得自己判断前端是否传了callback过来。
	 */
	/**
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<String> queryItemCatList(
			@RequestParam(value = "callback", required = false)String callback){
		try {
			ItemCatResult itemCatResult = this.itemCatService.queryAllToTree();
			//返回的数据是jsonp类型，所以在数据返回前，需要做如下处理：
			String json = objectMapper.writeValueAsString(itemCatResult);
			if(StringUtils.isEmpty(callback)){
				return ResponseEntity.ok(json);
			}
			return ResponseEntity.ok(callback + "(" + json + ");");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	 */
	
	/**
	 * 最终返回jsonp类型，将判断前端是否传了callback过来的代码抽取到
	 * /taotao-common/src/main/java/com/taotao/common/spring/exetend/converter/json/CallbackMappingJackson2HttpMessageConverter.java中。
	 * 在数据进行返回前，自动进行消息转化。
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<ItemCatResult> queryItemCatList(){
		try {
			ItemCatResult itemCatResult = this.itemCatService.queryAllToTree();
			return ResponseEntity.ok(itemCatResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	}
	
}
