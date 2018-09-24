package com.taotao.manage.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.mapper.ItemParamMapper;
import com.taotao.manage.pojo.ItemParam;

/**
 * 商品规格参数的模板Service
 */
@Service
public class ItemParamService extends BaseService<ItemParam> {
	
	@Autowired
	private ItemParamMapper itemParamMapper;
	
	/**
	 * 查询商品规格参数模板列表。
	 */
	public EasyUIResult queryItemParamList(Integer page, Integer rows) {
		
		PageHelper.startPage(page, rows);
		
		Example example = new Example(ItemParam.class);
		example.setOrderByClause("Created DESC");//按创建时间降序排序，ASC为升序。
		List<ItemParam> itemParams = this.itemParamMapper.selectByExample(example);
		
		PageInfo<ItemParam> pageInfo = new PageInfo<>(itemParams);
		return new EasyUIResult(pageInfo.getTotal(), pageInfo.getList());
	}
}
