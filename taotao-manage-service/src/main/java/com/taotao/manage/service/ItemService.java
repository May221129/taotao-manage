package com.taotao.manage.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.bean.EasyUIResult;
import com.taotao.manage.mapper.ItemMapper;
import com.taotao.manage.pojo.Item;
import com.taotao.manage.pojo.ItemDesc;
import com.taotao.manage.pojo.ItemParamItem;

/**
 * 商品的Service
 */
@Service
public class ItemService extends BaseService<Item> {

	@Autowired
	private ItemMapper itemMapper;

	// 因为建表的时候，把描述从item中分离出来了，所以描述也得有个Service
	@Autowired
	private ItemDescService itemDescService;

	@Autowired
	private ItemParamItemService itemParamItemService;

	// redis的消息发布，B端的商品详情进行更新了，就会发布消息，C端订阅了该通道，收到消息后将redis中该商品Id的缓存数据删除。
	@Autowired
	private StringRedisTemplate redisTemplate;

	protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * 为什么这里要将保存item和保存desc写到同一个方法里，而不是在ItemController中直接分别调用？
	 * 因为需要将这两个方法，写在同一个事务里。根据事务的传播性，
	 * 执行到super.save(item);和this.itemDescService.save(itemDesc);时，
	 * 默认是不会再新开一个事物，而是直接用saveItem()方法的事务， 从而实现一旦出错，item和的desc都会回滚。
	 */
	public Boolean saveItem(Item item, String desc, String itemParams) {

		// 比对Iteam表，看哪些字段是item对象没有的，如下面的初始值。
		// 初始值
		item.setStatus(1);// 1:商品状态为正常
		item.setId(null);// 出于安全考虑，强制设置id为null，通过数据库自增长得到id值。
		Integer count1 = super.save(item);// 返回值是插入条数，如果是失败则有可能是0，所以在return时需要做判断，而不能依赖事务的回滚。
		
		//测试上下两个方法是否在同一个事务中，一旦出错，是否会一起回滚。还可以通过看日志的方式来验证。
		// System.out.println(1 / 0);
		
		// desc参数的处理：保存商品表述数据
		ItemDesc itemDesc = new ItemDesc();
		itemDesc.setItemId(item.getId());// 这里是能拿到商品id的，因为上面那段代码已经被执行了，数据保存到数据库了，自然商品id也自动生成了。
		itemDesc.setItemDesc(desc);
		Integer count2 = this.itemDescService.save(itemDesc);

		// 保存规格参数数据：
		ItemParamItem itemParamItem = new ItemParamItem();
		// itemParamItem.setId(null);//这是自己new的对象，并未为其id赋值，所以这里的id一定为null，就不需要再setId为null了。
		itemParamItem.setItemId(item.getId());
		itemParamItem.setParamData(itemParams);
		Integer count3 = this.itemParamItemService.save(itemParamItem);

		return count1.intValue() == 1 && count2.intValue() == 1 && count3.intValue() == 1;
	}// 泛型类型是Item，就得保证ItemMapper是存在的。

	public EasyUIResult queryItemList(Integer page, Integer rows) {
		// 设置分页参数：
		PageHelper.startPage(page, rows);
		// 按创建时间排序：
		Example example = new Example(Item.class);
		example.setOrderByClause("Created DESC");
		List<Item> items = this.itemMapper.selectByExample(example);

		PageInfo<Item> pageInfo = new PageInfo<>(items);
		return new EasyUIResult(pageInfo.getTotal(), pageInfo.getList());
	}

	/**
	 * 根据商品id更新做更新
	 * 注意：
	 * 	1.该方法不能用int count来记录每个SQL语句是否成功执行更新。
	 * 		因为有些商品是没有param模板的，也就意味着没有param参数，那么更新param参数时返回的count就为0，
	 * 		最后如果用“count1.intValue() == 1 && count2.intValue() == 1 && count3.intValue() == 1;”
	 * 		来判断是否都更新成功了，结果就为false。那么最后controller层返回的就是500.
	 * 		所以下面那个被注释掉的方法时错误的写法。
	 * 	2.即使按照第1点进行修改了，还会存在一个问题：原先就没有itemDesc,现在更新，补上了商品描述，
	 * 		但是因为tb_item_desc表中就没有该item_id对应的记录，所以此时是更新不了的。
	 * 		解决办法：
	 * 		（1）新增商品时，不管是否有商品描述信息，都在tb_item_desc表中新增字段。(不推荐，这样该表中会有很多无效记录)
	 * 		（2）更新商品时，先查询是否存在该商品对应的商品描述：（推荐使用）
	 * 			如果存在，则直接更新；
	 * 			如果不存在，则判断前端是否传了desc字段过来，传了则直接插入，没传则不做任何操作。
	 * 	3.如果以后要做itemParams的更新优化，也参照第2点。
	 * @param item
	 * @param desc
	 * @param itemParams
	 * @return void。
	 */
	public void updateItem(Item item, String desc, Long itemParamId, String itemParams) {
		item.setStatus(null);
		super.updateSelective(item);
		
		// 更新商品描述：
		if(null != itemDescService.queryById(item.getId())){
			ItemDesc itemDesc = new ItemDesc();
			itemDesc.setItemId(item.getId());
			itemDesc.setItemDesc(desc);
			itemDesc.setUpdated(new Date());
			itemDescService.updateSelective(itemDesc);
		}else{
			if(StringUtils.isNotEmpty(desc)){
				ItemDesc itemDesc = new ItemDesc();
				itemDesc.setItemId(item.getId());
				itemDesc.setItemDesc(desc);
				itemDesc.setCreated(new Date());
				itemDesc.setUpdated(itemDesc.getCreated());
				itemDescService.save(itemDesc);
			}
		}
		
		// 更新商品规格参数：
		// 因为BaseService中重写的更新方法都是根据主键进行更新，所以这里需要拿到ItemParamId主键作为更新条件。
		// 还有一种方法：在ItemParamItemService中自定义更新方法，ItemController和ItemService中再进行调用。
		ItemParamItem itemParamItem = new ItemParamItem();
		itemParamItem.setId(itemParamId);
		itemParamItem.setParamData(itemParams);
		this.itemParamItemService.updateSelective(itemParamItem);
		
		//商品更新成功后，发布消息：
		//实现方式一：redis发布消息，通知那些订阅了该通道的客户端：
//		redisTemplate.convertAndSend("item-detail", item.getId().toString());// 通道名、要发布的消息
		//实现方式二：使用Rabbitm的通配符模式发布消息：
		this.sendMessage("item.update", item.getId());
		
		LOGGER.info("redisTemplate.convertAndSend{'item-detail'," + item.getId().toString());
	}
//	public Boolean updateItem(Item item, String desc, Long itemParamId, String itemParams) {
//		item.setStatus(null);
//		Integer count1 = super.updateSelective(item);
//		
//		// 更新商品描述：
//		ItemDesc itemDesc = new ItemDesc();
//		itemDesc.setItemId(item.getId());
//		itemDesc.setItemDesc(desc);
//		Integer count2 = itemDescService.updateSelective(itemDesc);
//		
//		// 更新商品规格参数：
//		// 因为BaseService中重写的更新方法都是根据主键进行更新，所以这里需要拿到ItemParamId主键作为更新条件。
//		// 还有一种方法：在ItemParamItemService中自定义更新方法，ItemController和ItemService中再进行调用。
//		ItemParamItem itemParamItem = new ItemParamItem();
//		itemParamItem.setId(itemParamId);
//		itemParamItem.setParamData(itemParams);
//		Integer count3 = this.itemParamItemService.updateSelective(itemParamItem);
//		
//		if(count1.intValue() == 1 && count2.intValue() == 1 && count3.intValue() == 1){
//			//redis发布消息，通知那些订阅了该通道的客户端：
//			redisTemplate.convertAndSend("item-detail", item.getId().toString());// 通道名、要发布的消息
//			LOGGER.info("redisTemplate.convertAndSend{'item-detail'," + item.getId().toString());
//		}
//
//		return count1.intValue() == 1 && count2.intValue() == 1 && count3.intValue() == 1;
//	}
	
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/**
	 * 发布消息。实现方式二：使用Rabbitm的通配符模式发布消息：
	 * @param routingKey：指明queue接收哪类的消息，同时传给消息消费者告知数据做了CRUD哪类操作。
	 * @param id：被操作数据的id.
	 */
	public void sendMessage(String routingKey, Long itemId){
		try {
			Map<String, Object> message = new HashMap<>();
			message.put("id", itemId);
			message.put("routingKey", routingKey);
			message.put("date", System.currentTimeMillis());
			rabbitTemplate.convertAndSend(routingKey, MAPPER.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
