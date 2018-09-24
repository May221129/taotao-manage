package com.taotao.manage.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.taotao.manage.pojo.ContentCategory;
import com.taotao.manage.service.ContentCategoryService;

/**
 * （商城首页广告）内容分类管理
 */
@Controller
@RequestMapping("content/category")
public class ContentCategoryController extends BaseController {
	
	@Autowired
	private ContentCategoryService contentCategoryService;
	
	/**
	 * 根据父节点id查询内容分类列表
	 * 1.@RequestParam(value = "id", defaultValue = "0")Long patentId:
	 * 	点击一个节点的时候，会根据这个节点id去查询子节点，所以这里的入参是该节点的id。第一次查询的时候是没有节点id的，所以要给默认值0.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<ContentCategory>> queryContentCategoryListByParentId(
			@RequestParam(value = "id", defaultValue = "0")Long patentId){
		
		ContentCategory record = new ContentCategory();
		record.setParentId(patentId);
		List<ContentCategory> contentCategoryList  = this.contentCategoryService.queryListByWhere(record);
		if(null == contentCategoryList || contentCategoryList.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(contentCategoryList);
	}
	
	/**
	 * 新增内容分类节点
	 */
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<ContentCategory> saveContentCategory(ContentCategory contentCategory){//需要把新增的节点的id返回给前端
		this.contentCategoryService.saveContentCategory(contentCategory);
		return ResponseEntity.status(HttpStatus.CREATED).body(contentCategory);
	}
	
	/**
	 * 根据id为内容分类节点重命名
	 * 提问：接收参数的时候，是用对象来接收好？还是用@RequestParam一个参数一个参数的接收好？
	 * 回答：在这里用第二种比较好，因为如果前端不止传来id和name这两个参数，这时用了对象来接收就会多修改了字段，而我们这个方法本身的目的是重命名。
	 */
//	@RequestMapping(method = RequestMethod.PUT)
//	public ResponseEntity<Void> rename(ContentCategory contentCategory){
//		this.contentCategoryService.updateSelective(contentCategory);
//		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//	}
	@RequestMapping(method = RequestMethod.PUT)
	public ResponseEntity<Void> rename(@RequestParam("id")Long id, @RequestParam("name")String name){
		//怎么知道@RequestParam参数命名的？这个要看对应的前端代码：/taotao-manage-web/src/main/webapp/WEB-INF/views/content-category.jsp 的第40行
		ContentCategory contentCategory = new ContentCategory();
		contentCategory.setId(id);
		contentCategory.setName(name);
		this.contentCategoryService.updateSelective(contentCategory);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * 删除内容分类节点
	 * 提问：这里用什么来接手参数好？
	 * 答：主要看controller和service中需要哪些参数
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteContentCategoryById(
			@RequestParam(value = "parentId")Long parentId,
			@RequestParam(value = "id")Long id){
		this.contentCategoryService.deleteContentCategoryById(parentId, id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
