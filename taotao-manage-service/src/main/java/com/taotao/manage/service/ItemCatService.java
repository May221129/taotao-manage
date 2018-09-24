package com.taotao.manage.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taotao.common.bean.ItemCatData;
import com.taotao.common.bean.ItemCatResult;
import com.taotao.common.bean.RedisKeyConstant;
import com.taotao.manage.pojo.ItemCat;

/**
 * 商品类目的Service
 * 	自己写的queryAllItemCatToTree()和老师提供的queryAllToTree()的区别：
 * 		我写的方法中，三次都那从数据库中查出的含所有ItemCat的集合拿来做遍历，当数据量大的时候，效率就会很低；
 * 		老师提供的方法中，建一个hashmap来存放数据，将parentId相同的类目记录，
 * 		parentId作为key，value为ArrayList，该ArrayList中的元素即为parentId为key的那些类目记录。
 * 		老师提供的方法，将数据存放到map中时比较麻烦，但在建树的时候速度就会快很多。但数据量大时，优势越明显。
 * 
 * Redis缓存加在了老师提供的方法中。
 * 
 */
@Service
public class ItemCatService extends BaseService<ItemCat>{
	
//	@Autowired
//	private ItemCatMapper itemCatMapper;
//	
//	@Override
//	public Mapper<ItemCat> getMapper() {
//		return this.itemCatMapper;
//	}
	
	//下面这个根据主键id做查询，是在没有BaseService时写的，现在有了BaseService，就可以不用了。
//	public List<ItemCat> queryItemListByParentId(Long parentId) {
//		ItemCat record = new ItemCat();
//		record.setParentId(parentId);//查询商品类目
//		return itemCatMapper.select(record);
//	}
	
	//为什么这个要写成类变量：这样只需要计算一次即可。
	private static final Integer REDIS_TIME =  60 * 60 * 24 * 30;//因为商品类目是不经常进行更新的，所以这里设置缓存一个月。
	
	@Autowired
	private StringRedisTemplate redisTemplate;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 老师给的方法：将全部的类目数据从数据库查询出来后，按照ItemCatData类的格式进行封装，最后封装成Json格式的数据返回。
	 */
	public ItemCatResult queryAllToTree() {
		ItemCatResult result = new ItemCatResult();
		
		try {//==》原则：缓存逻辑不能影响原有的业务逻辑执行。
			String cacheData = redisTemplate.opsForValue().get(RedisKeyConstant.API_ITEM_CAT_GET_KEY);
			if(StringUtils.isNotEmpty(cacheData)){//如果不为空，就命中缓存
				
				//为了防止热点数据到期，只要在到期时间内查询了该数据，就更新该数据的生存时间：
				this.redisTemplate.expire(RedisKeyConstant.API_ITEM_CAT_GET_KEY, REDIS_TIME, TimeUnit.SECONDS);
				
				//cacheData是字符型，需要反序列化：
				return MAPPER.readValue(cacheData, ItemCatResult.class);
			} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		// 全部查出，并且在内存中生成树形结构
		List<ItemCat> itemCats = super.queryAll();
		
		// 转为map存储，key为父节点ID，value为数据集合
		Map<Long, List<ItemCat>> itemCatsMap = new HashMap<Long, List<ItemCat>>();
		for (ItemCat itemCat : itemCats) {
			if(!itemCatsMap.containsKey(itemCat.getParentId())){
				itemCatsMap.put(itemCat.getParentId(), new ArrayList<ItemCat>());
			}
			itemCatsMap.get(itemCat.getParentId()).add(itemCat);
		}
		
		// 封装一级对象
		List<ItemCat> itemCats1 = itemCatsMap.get(0L);
		for (ItemCat itemCat1 : itemCats1) {
			ItemCatData itemCatData1 = new ItemCatData();
			itemCatData1.setUrl("/products/" + itemCat1.getId() + ".html");
			itemCatData1.setName("<a href='"+itemCatData1.getUrl()+"'>"+itemCat1.getName()+"</a>");
			result.getItemCats().add(itemCatData1);
			//如果当前itemCat1不是父类目，即其下面没有字类目了，那就跳出当前循环：
			if(!itemCat1.getIsParent()){
				continue;
			}
			
			// 封装二级对象
			List<ItemCat> itemCats2 = itemCatsMap.get(itemCat1.getId());
			List<ItemCatData> itemCatDatas2 = new ArrayList<ItemCatData>();
			itemCatData1.setItems(itemCatDatas2);
			for (ItemCat itemCat2 : itemCats2) {
				ItemCatData itemCatData2 = new ItemCatData();
				itemCatData2.setName(itemCat2.getName());
				itemCatData2.setUrl("/products/" + itemCat2.getId() + ".html");
				itemCatDatas2.add(itemCatData2);
				if(itemCat2.getIsParent()){
					// 封装三级对象
					List<ItemCat> itemCats3 = itemCatsMap.get(itemCat2.getId());
					List<String> itemCatDatas3 = new ArrayList<String>();
					itemCatData2.setItems(itemCatDatas3);
					for (ItemCat itemCat3 : itemCats3) {
						itemCatDatas3.add("/products/" + itemCat3.getId() + ".html|"+itemCat3.getName());
					}
				}
			}
			//首页的类目那里，最多能够显示14个，多了显示不下，所以这里做了限制。但其实这是不对的。
			if(result.getItemCats().size() >= 14){
				break;
			}
		}
		//将数据库的查询结果放到缓存中：这里的生存时间取决于数据的更新频率
		try {
			this.redisTemplate.opsForValue().set(RedisKeyConstant.API_ITEM_CAT_GET_KEY, MAPPER.writeValueAsString(result));
			this.redisTemplate.expire(RedisKeyConstant.API_ITEM_CAT_GET_KEY, REDIS_TIME, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 自己写的方法：将全部的类目数据从数据库查询出来后，按照ItemCatData类的格式进行封装，最后封装成Json格式的数据返回。
	 */
	public ItemCatResult queryAllItemCatToTree(){
		//1.得到所有的类目：
		List<ItemCat> itemCatList = super.queryAll();
		
		//2.将类目数据封装成ItemCatData，并将所有的ItemCatData封装到ItemCatResult中：
		ItemCatResult itemCatResult = new ItemCatResult();
		List<ItemCatData> firstItemCatDatas = new ArrayList<>();//存放所有的一级类目
		//2.1 得到所有的一级类目：
		for(ItemCat firstitemCat : itemCatList){
			if(firstitemCat.getParentId() == 0){
				
				ItemCatData firstItemCatData = new ItemCatData();
				firstItemCatData.setUrl("/products/" + firstitemCat.getId() + ".html");//"u":"/products/1.html"
				firstItemCatData.setName("<a href='/products/" + firstitemCat.getId() + ".html'>" + firstitemCat.getName() + "</a>");//"n":"<a href='/products/1.html'>图书、音像、电子书刊</a>"
				
				List<ItemCatData> secondItemCatDatas = new ArrayList<>();//存放二级类目
				//2.2 得到以当前一级类目的id为parentId的二级目录：
				for(ItemCat secondItemCat : itemCatList){
					if(secondItemCat.getParentId().equals(firstitemCat.getId())){
						
						ItemCatData secondItemCatData = new ItemCatData();
						secondItemCatData.setUrl("/products/" + secondItemCat.getId() + ".html");//"u":"/products/2.html"
						secondItemCatData.setName(secondItemCat.getName());//"n":"电子书刊"
						
						List<String> thirdItemCatDatas = new ArrayList<>();//存放三级类目
						//2.3 得到以当前二级类目的id为parentId的三级类目：
						for(ItemCat thirdItemCat : itemCatList){
							if(thirdItemCat.getParentId().equals(secondItemCat.getId())){
								thirdItemCatDatas.add(new String("/products/" + thirdItemCat.getId() + ".html|" + thirdItemCat.getName()));//"/products/3.html|电子书"
							}
						}
						//把2.3得到的三级类目的list，设置到二级类目的item中：
						secondItemCatData.setItems(thirdItemCatDatas);
						//把满足 secondItemCat.getParentId() == firstitemCat.getId() 条件的当前类目，放到存放二级类目的list中：
						secondItemCatDatas.add(secondItemCatData);
					}
				}
				//把2.2得到的二级类目的list，设置到一级类目的item中：
				firstItemCatData.setItems(secondItemCatDatas);
				//把满足 firstitemCat.getParentId() == 0 条件的当前类目，放到存放一级类目的list中：
				firstItemCatDatas.add(firstItemCatData);
			}
		}
		itemCatResult.setItemCats(firstItemCatDatas);
		return itemCatResult;
	}
	
}
	