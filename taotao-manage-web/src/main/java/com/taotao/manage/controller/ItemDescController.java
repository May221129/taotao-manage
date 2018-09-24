package com.taotao.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.service.ItemDescService;

/**
 * 商品描述的controller
 */
@Controller
@RequestMapping("item/desc")
public class ItemDescController extends BaseController {

	@Autowired
	private ItemDescService itemDescService;

	/**
	 * 根据itemId查询商品的描述数据。
	 */
	@RequestMapping(value = "{itemId}", method = RequestMethod.GET)
	public ResponseEntity<ItemDesc> queryByItemId(@PathVariable("itemId") Long itemId) {
		ItemDesc itemDesc = this.itemDescService.queryById(itemId);
		if (itemDesc == null) {
			ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);// 404
		}
		// 写成ResponseEntity.ok(itemDesc)，只是写法不同，效果一样？
		return ResponseEntity.status(HttpStatus.OK).body(itemDesc);// 200。
	}
}
