package com.taotao.manage.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.taotao.manage.pojo.ItemCat;
import com.taotao.manage.service.ItemCatService;

@Controller
@RequestMapping("item/cat")
public class ItemCatController extends BaseController {

	@Autowired
	private ItemCatService itemCatService;

	/**
	 * 查询商品类目列表
	 * 
	 * ResponseEntity<T>:这个返回值是根据restful风格而定的。T取决于客户端
	 * 
	 * @param parentId
	 * @return ResponseEntity<List<ItemCat>>
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<ItemCat>> queryItemCatListByParentId(
			@RequestParam(value = "id", defaultValue = "0") Long parendId) {
		ItemCat record = new ItemCat();
		record.setParentId(parendId);
		List<ItemCat> list = this.itemCatService.queryListByWhere(record);
		if (null == list || list.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);// 404
		}
		return ResponseEntity.ok(list);// 200
	}
	
	/**
	 * 更新商品类目
	 */
}
