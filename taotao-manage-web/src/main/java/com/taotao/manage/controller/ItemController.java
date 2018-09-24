package com.taotao.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.pojo.Item;
import com.taotao.manage.service.ItemService;

@Controller
@RequestMapping("item")
public class ItemController extends BaseController {

	/**
	 * 日志： 日志一般打在哪儿： 1.方法的入参处； 2.业务执行的状态发生变化时，如新增商品成功或新增商品失败了； 3.异常处。
	 */

	@Autowired
	private ItemService itemService;

	/**
	 * 新增商品
	 */
	@RequestMapping(method = RequestMethod.POST) // POST增，GET查，PUT改，DELETE删
	public ResponseEntity<Void> saveItem(Item item, @RequestParam("desc") String desc,
			@RequestParam("itemParams") String itemParams) {// itemParams：保存商品规格参数
		// 下面为什么可以这么写，而不是用加号进行拼接，是因为debug()这个方法做了处理，将字符串后面的对象，逐一按大括号进行拼接。
		LOGGER.debug("新增商品，item = {}, desc = {}", item, desc);
		if (StringUtils.isEmpty(item.getTitle())) {// TODO 不够详细，如价格也许进行判断。未完成。
			// 参数有误
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();// 400
		}
		// 保存商品：
		// 提问：保存商品规格参数，是否应该在下面这个saveitem()方法中一起实现？答：这取决于保存商品规格参数是否需要新开事务。这里应该是同一个事务。
		Boolean bool = this.itemService.saveItem(item, desc, itemParams);
		if (!bool) {// 取反，即不成功
			// 保存失败
			LOGGER.info("新增商品失败，item = {}", item);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();// 500
		}
		LOGGER.info("新增商品成功，itemId = {}", item.getId());
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();// 201
	}

	/**
	 * 查询商品列表方法。 1. 返回json数据，之前是需要用到@ResponseBody注解的，
	 * 但现在有了ResponseEntity<EasyUIResult>返回值类型，就可以不用写@ResponseBody了。
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<EasyUIResult> queryItemList(@RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "rows", defaultValue = "30") Integer rows) {
		LOGGER.info("查询商品列表，page = {}, rows = {}" + page, rows);
		EasyUIResult easyUIResult = this.itemService.queryItemList(page, rows);
		if (null != easyUIResult) {
			LOGGER.info("查询商品列表成功! easyUIResult = {}" + easyUIResult);
			return ResponseEntity.ok(easyUIResult);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	/**
	 * 更新/编辑商品
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<Void> updateItem(Item item, @RequestParam("desc") String desc,
			@RequestParam("itemParamId") Long itemParamId, @RequestParam("itemParams") String itemParams) {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("编辑商品，item = {}, desc = {}", item, desc);
			}
			if (StringUtils.isEmpty(item.getTitle())) {
				// 参数有误
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();// 400
			}
			// 编辑商品：
			this.itemService.updateItem(item, desc, itemParamId, itemParams);
			
			LOGGER.info("编辑商品成功，itemId = {}", item.getId());
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();// 204
		} catch (Exception e) {
			LOGGER.error("编辑商品失败，item = {}, desc = {}", item, desc);
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();// 500
	}
//	@RequestMapping(method = RequestMethod.PUT)
//	public ResponseEntity<Void> updateItem(Item item, @RequestParam("desc") String desc,
//			@RequestParam("itemParamId") Long itemParamId, @RequestParam("itemParams") String itemParams) {
//		try {
//			if (LOGGER.isDebugEnabled()) {
//				LOGGER.debug("编辑商品，item = {}, desc = {}", item, desc);
//			}
//			if (StringUtils.isEmpty(item.getTitle())) {
//				// 参数有误
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();// 400
//			}
//			// 编辑商品：
//			Boolean bool = this.itemService.updateItem(item, desc, itemParamId, itemParams);
//			if (bool) {
//				// 成功：
//				if (LOGGER.isInfoEnabled()) {
//					LOGGER.info("编辑商品成功，itemId = {}", item.getId());
//				}
//				return ResponseEntity.status(HttpStatus.NO_CONTENT).build();// 204
//			}else{
//				// 失败：
//				if (LOGGER.isInfoEnabled()) {
//					LOGGER.info("编辑商品失败，itemId = {}", item.getId());
//				}
//				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();// 500
//			}
//		} catch (Exception e) {
//			LOGGER.error("编辑商品失败，item = {}, desc = {}", item, desc);
//		}
//		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();// 500
//	}
}
