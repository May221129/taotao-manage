package com.taotao.manage.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.service.ItemDescService;

@Controller
@RequestMapping("api/item/desc")
public class ApiItemDescController {
	
	@Autowired
	private ItemDescService itemDescService;
	
	@RequestMapping(value = "{itemId}", method = RequestMethod.GET)
	public ResponseEntity<ItemDesc> queryItemDescByItemId(@PathVariable("itemId")Long itemId){
		ItemDesc itemDesc = this.itemDescService.queryById(itemId);
		if(null != itemDesc){
			return ResponseEntity.ok(itemDesc); 
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
}
