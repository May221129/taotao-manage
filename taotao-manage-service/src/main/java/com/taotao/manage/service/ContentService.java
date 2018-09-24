package com.taotao.manage.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.bean.EasyUIResult;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.manage.mapper.ContentMapper;
import com.taotao.manage.pojo.Content;

/**
 * （商场首页广告）内容管理
 */
@Service
public class ContentService extends BaseService<Content> {
	
	@Autowired
	private ContentMapper contentMapper;
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private static final Integer REDIS_TIME =  60 * 60 * 24 * 30;//因为首页广告是不经常进行更新的，所以这里设置缓存一个月。
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 为什么这里要新写一个查询方法，而不用BaseService中写好的queryPageListByWhere(T record, Integer pageNum, Integer pageSize)方法？
	 * 因为直接返回PageInfo，不符合前端的解析格式。
	 * 所以要将查询到的数据封装成符合前端解析格式的EasyUIResult类。
	 */
	public EasyUIResult queryContentListByCategoryId(Long categoryId, Integer page, Integer rows){
		
		//1.先试着从redis缓存中拿数据：
		try {
			String cacheData = redisTemplate.opsForValue().get(RedisKeyConstant.API_CONTENT_KEY + categoryId);
			if(StringUtils.isNotEmpty(cacheData)){
				//为了防止热点数据到期，只要在到期时间内查询了该数据，就更新该数据的生存时间：
				this.redisTemplate.expire(RedisKeyConstant.API_CONTENT_KEY + categoryId, REDIS_TIME, TimeUnit.SECONDS);
				//cacheData是字符型，需要反序列化：
				return MAPPER.readValue(cacheData, EasyUIResult.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//2.如果redis缓存中没有，则连接数据库去查询数据：
		PageHelper.startPage(page, rows);
		//查询的结果按照更新时间做倒序排序。实现方式有两种：
		//方法一：通过通用Mapper的Example做查询结果的排序：
		//见：EasyUIResult com.taotao.manage.service.ItemService.queryItemList(Integer page, Integer rows)
		//方法二：使用最原始的方法完成查询——写xml配置文件来完成：
		//见：/taotao-manage-web/src/main/resources/mybatis/mappers/ContentMapper.xml
		List<Content> contents = this.contentMapper.queryContentListByCategoryId(categoryId);
		PageInfo<Content> pageInfo = new PageInfo<>(contents);
		EasyUIResult easyUIResult = new EasyUIResult(pageInfo.getTotal(), pageInfo.getList());
		
		//3.将数据放入redis缓存中：
		//注意：存放的时候，key要加上categoryId，
		//否则service第一次从数据库查到的数据会被放入缓存中，之后不论categoryId为多少的请求，都会从redis中拿数据。
		try {
			this.redisTemplate.opsForValue().set(RedisKeyConstant.API_CONTENT_KEY + categoryId, MAPPER.writeValueAsString(easyUIResult));
			this.redisTemplate.expire(RedisKeyConstant.API_CONTENT_KEY + categoryId, REDIS_TIME, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//将数据返回：
		return easyUIResult;
	}
}
