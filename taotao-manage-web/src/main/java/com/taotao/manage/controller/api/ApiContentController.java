package com.taotao.manage.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.controller.BaseController;
import com.taotao.manage.service.ContentService;

/**
 * 对外提供接口服务：（商城首页广告）内容查询
 */
@Controller
@RequestMapping("api/content")
public class ApiContentController extends BaseController {
	
	@Autowired
	private ContentService contentService;
	
	/**
	 * 根据CategoryId来查询内容分页，并且根据更新时间倒序排序
	 * @param categoryId, page, rows:20
	 * @return 要将查询到的数据封装成符合前端解析格式的EasyUIResult类。
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<EasyUIResult> queryPageListByCategoryId(
			@RequestParam("categoryId")Long categoryId,
			@RequestParam(value = "page", defaultValue = "1")Integer page,
			@RequestParam(value = "rows", defaultValue = "20")Integer rows){
		EasyUIResult result = this.contentService.queryContentListByCategoryId(categoryId, page, rows);
		if(null != result){
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
}
