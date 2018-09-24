package com.taotao.manage.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.taotao.manage.pojo.ContentCategory;

/**
 * （商城首页广告）内容分类管理
 */
@Service
public class ContentCategoryService extends BaseService<ContentCategory> {

	/**
	 * 新增内容分类节点，这里之所以要写这么一个方法，是为了将“新增节点”和“修改其父节点的parentId”这个事务合并为同一个事务。
	 */
	public void saveContentCategory(ContentCategory contentCategory) {
		contentCategory.setId(null);
		contentCategory.setIsParent(false);// 新增的节点，默认是没有子节点的
		contentCategory.setSortOrder(1);
		contentCategory.setStatus(1);
		super.save(contentCategory);

		// 新增节点完成之后，需要判断该节点的"父节点的isParent"是否为true，因为还没新增该子节点时，其父节点还不是父节点且它的isParent是false的！
		// 如果不是，则需要修改为true;如果是，则不需要修改了
		ContentCategory parentContentCategory = super.queryById(contentCategory.getParentId());
		if (!parentContentCategory.getIsParent()) {
			parentContentCategory.setIsParent(true);
			super.updateSelective(parentContentCategory);
		}
	}

	/**
	 * 删除 删除一个节点时，要判断这个节点是否有子节点，如果有，则连同子节点一起删除。这时候就要用到递归
	 */
	public void deleteContentCategoryById(Long parentId, Long id) {

		// 所有要删除的节点的id都放在ids中
		List<Object> ids = new ArrayList<>();
		ids.add(id);

		// 递归查找当前节点的所有子节点、子子节点……
		this.findSonNodeByParentId(id, ids);
		
		// 批量删除ids中的所有节点：
		super.deleteByIds(ids, ContentCategory.class, "id");

		// 把入参传进来的节点及其下面所有子节点都删除后，还需要判断该节点的父节点是否还有其他子节点，
		// 如果有，则不需要改变其isParent值，如果没有，则需将其父节点的isParent值改为false：
		ContentCategory contentCategory = new ContentCategory();
		contentCategory.setParentId(parentId);
		List<ContentCategory> nodeListByPatentId = super.queryListByWhere(contentCategory);
		if (null == nodeListByPatentId || nodeListByPatentId.isEmpty()) {
			ContentCategory parentContentCategory = new ContentCategory();
			parentContentCategory.setId(parentId);
			parentContentCategory.setIsParent(false);
			super.updateSelective(parentContentCategory);
		}
	}

	/**
	 * 递归：根据parentId来查找其所有子节点，及其子子节点。
	 */
	public void findSonNodeByParentId(Long patentId, List<Object> ids) {
		// 查出表中所有符合“parentId=id”的记录的id字段：
		// select id from tb_content_category where parent_id = patentId;
		// 但是Mapper提供的查询方法中没有封装只查出id字段的查询，所以只能查出parentId=patentId的记录的所有字段，再遍历拿到他们的id
		ContentCategory record = new ContentCategory();
		record.setParentId(patentId);
		List<ContentCategory> list = super.queryListByWhere(record);
		// 遍历集合，看子节点是否还有下一级节点：
		if (null != list && list.size() != 0) {
			for (ContentCategory element : list) {
				ids.add(element.getId());
				// 判断element是否为父节点，开始递归：
				if (element.getIsParent()) {
					findSonNodeByParentId(element.getId(), ids);
				}
			}
		}
	}
}
