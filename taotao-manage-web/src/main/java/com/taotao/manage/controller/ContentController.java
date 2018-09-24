package com.taotao.manage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.pojo.Content;
import com.taotao.manage.service.ContentService;

/**
 * （商城首页广告）内容管理
 * 注：还将该ContentController拷贝到了api包下，改名为APIContentController，对外提供接口服务，其url也变了。
 */
@Controller
@RequestMapping("content")
public class ContentController extends BaseController {
	
	@Autowired
	private ContentService contentService;
	
	/**
	 * 新增内容
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> saveContent(Content content){
		//检查一下看那些字段需要手动初始化：
		content.setId(null);
		this.contentService.save(content);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
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
	
	/**
	 * 编辑内容
	 */
	@RequestMapping("/edit")
	public ResponseEntity<Void> updateContent(Content content){
		this.contentService.updateSelective(content);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	/**
	 * 批量删除：根据ids做批量删除
	 */
	@RequestMapping("/delete")
	public ResponseEntity<Void> deleteContentById(@RequestParam("ids")List<Object> ids){
		this.contentService.deleteByIds(ids, Content.class, "id");
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
