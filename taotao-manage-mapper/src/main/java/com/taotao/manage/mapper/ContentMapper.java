package com.taotao.manage.mapper;

import java.util.List;

import com.github.abel533.mapper.Mapper;
import com.taotao.manage.pojo.Content;

public interface ContentMapper extends Mapper<Content> {
	
	/**
	 * 根据categoryId查询内容列表，并按更新时间倒序排序。
	 */
	public List<Content> queryContentListByCategoryId(Long categoryId);
	
}
