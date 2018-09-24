package com.taotao.manage.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taotao.manage.controller.BaseController;
import com.taotao.manage.pojo.ItemParamItem;
import com.taotao.manage.service.ItemParamItemService;

@RequestMapping("aip/item/param/item")
@Controller
public class ApiItemParamItemController extends BaseController {

	@Autowired
	private ItemParamItemService itemParamItemService;

	/**
	 * 根据商品id查询商品的规格参数，是从item_param _item表中。
	 */
	@RequestMapping(value = "{itemId}", method = RequestMethod.GET)
	public ResponseEntity<ItemParamItem> queryItemParamItemByItemId(@PathVariable("itemId") Long itemId) {
		ItemParamItem record = new ItemParamItem();
		record.setItemId(itemId);
		ItemParamItem itemParamItem = this.itemParamItemService.queryOne(record);
		if (itemParamItem != null) {
			return ResponseEntity.ok(itemParamItem);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
}
