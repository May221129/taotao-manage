package com.taotao.manage.controller.test;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.ItemCatData;
import com.taotao.common.bean.ItemCatResult;
import com.taotao.manage.service.ItemCatService;

/**
 * 利用spring-text来做测试：
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring/applicationContext*.xml"})
public class MapperTest {
	
	@Autowired
	private ItemCatService itemCatService;
	
	@Test
	public void testQueryAllItemCatToTreeMethod() throws Exception {
		
		long start = System.currentTimeMillis();
		
		ItemCatResult itemCatResult = itemCatService.queryAllItemCatToTree();
		//将结果转为json格式：
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(itemCatResult);
		System.out.println(json);
		
		long end = System.currentTimeMillis();
		System.out.println("程序运行用时：" + (end - start));
	}
	
	@Test
	public void testQueryAllItemCatToTreeMethod2() throws Exception {
		
		long start = System.currentTimeMillis();
		
		ItemCatResult itemCatResult = itemCatService.queryAllToTree();
		//将结果转为json格式：
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(itemCatResult);
		System.out.println(json);
		
		long end = System.currentTimeMillis();
		System.out.println("程序运行用时：" + (end - start));
	}
	
}
