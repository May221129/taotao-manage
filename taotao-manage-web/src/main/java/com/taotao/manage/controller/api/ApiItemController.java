package com.taotao.manage.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.manage.pojo.Item;
import com.taotao.manage.service.ItemService;

@RequestMapping("api/item")
@Controller
public class ApiItemController {
	
	@Autowired
	private ItemService itemService;
	
	@RequestMapping(value = "{itemId}", method = RequestMethod.GET)
	public ResponseEntity<Item> queryItemById(@PathVariable("itemId")Long itemId){
		Item item = this.itemService.queryById(itemId);
		if(null == item){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(item);
	}
}
