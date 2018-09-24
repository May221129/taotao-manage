package com.taotao.manage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.pojo.ItemParam;
import com.taotao.manage.service.ItemParamService;

/**
 * 商品规格参数的模板Controller
 */
@Controller
@RequestMapping("item/param")
public class ItemParamController extends BaseController {

	@Autowired
	private ItemParamService itemParamService;

	/**
	 * 根据商品类目id来查询商品规格参数模板
	 * 
	 * @param itemCatId
	 * @return ResponseEntity<ItemParam>
	 */
	@RequestMapping(value = "{itemCatId}", method = RequestMethod.GET)
	public ResponseEntity<ItemParam> queryItemParam(@PathVariable("itemCatId") Long itemCatId) {
		ItemParam record = new ItemParam();
		record.setItemCatId(itemCatId);
		ItemParam itemParam = this.itemParamService.queryOne(record);
		if (itemParam != null) {
			return ResponseEntity.status(HttpStatus.OK).body(itemParam);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	/**
	 * 新增商品规格参数模板-实现方式一
	 */
	@RequestMapping(value = "{itemCatId}", method = RequestMethod.POST)
	public ResponseEntity<Void> saveItemParam(@PathVariable("itemCatId") Long itemCatId,
			@RequestParam("paramData") String paramData) {
		ItemParam itemParam = new ItemParam();
		itemParam.setId(null);
		itemParam.setItemCatId(itemCatId);
		itemParam.setParamData(paramData);
		this.itemParamService.save(itemParam);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();// 204
	}
	/**
	 * 新增商品规格参数模板-实现方式二
	 */
	// @RequestMapping(value = "{itemCatId}", method =
	// RequestMethod.POST)//(method = RequestMethod.POST)
	// public ResponseEntity<Void> saveItemParam(ItemParam itemparam){
	// try {
	// this.itemParamService.save(itemparam);
	// return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	// }

	/**
	 * 查询商品规格参数模板列表。
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ResponseEntity<EasyUIResult> queryItemParamList(
			@RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "rows", defaultValue = "30") Integer rows) {
		EasyUIResult easyUIResult = this.itemParamService.queryItemParamList(page, rows);
		return ResponseEntity.status(HttpStatus.OK).body(easyUIResult);
	}
}
