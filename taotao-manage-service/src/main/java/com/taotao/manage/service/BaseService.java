package com.taotao.manage.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.abel533.entity.Example;
import com.github.abel533.mapper.Mapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.manage.pojo.BasePojo;

/**
 * 通用Service： 1、queryById 2、queryAll 3、queryOne 4、queryListByWhere
 * 5、queryPageListByWhere 6、save 7、update 8、deleteById 9、deleteByIds
 * 10、deleteByWhere
 */
public abstract class BaseService<T extends BasePojo> {

	/**
	 * 这自动注入的是一个接口，不能直接使用。但是，spring4以上的版本，支持泛型注入！！！所以下面的写法是可以的。
	 * 什么是泛型注入：根据泛型的类型，到spring容器中去查找这种类型的mapper实现类。
	 * 用了泛型注入后，子类中的mapper就可以不用再进行注入了。
	 */
	@Autowired
	private Mapper<T> mapper;

	/**
	 * 具体的Mapper类型，由实现者自行实现：
	 */
	// public abstract Mapper<T> getMapper();

	/**
	 * 根据id查询数据
	 */
	public T queryById(Long id) {
		return this.mapper.selectByPrimaryKey(id);
	}

	/**
	 * 查询所有数据
	 */
	public List<T> queryAll() {
		return this.mapper.select(null);// 没有参数的查询，就是查询全部。
	}

	/**
	 * 根据条件查询一条数据，如果该条件所查询的数据为多条就会抛出异常。
	 */
	public T queryOne(T record) {
		return this.mapper.selectOne(record);
	}

	/**
	 * 根据条件查询多条数据 提问：这里的条件是类似"where like name = 'A'"这样的条件吗？
	 */
	public List<T> queryListByWhere(T record) {
		return this.mapper.select(record);
	}

	/**
	 * 根据条件分页查询数据。
	 */
	public PageInfo<T> queryPageListByWhere(T record, Integer pageNum, Integer pageSize) {
		// 设置分页参数：
		PageHelper.startPage(pageNum, pageSize);
		List<T> list = this.mapper.select(record);
		return new PageInfo<T>(list);
	}

	/**
	 * 新增数据。 返回的是插入条数。失败可能返回的是0. 【这里不确定是因为看不到源码。猜测来源：Boolean
	 * com.taotao.manage.service.ItemService.saveItem(Item item, String desc,
	 * String itemParams)】
	 * 一般情况下，新增的返回值都是主键，如果有异常则会直接抛异常，一旦抛出异常就会在service送进行事务的回滚。
	 * 如果不知道某个方法的返回值是什么，最好是通过打断点来看源码，打断点可以打在调用方，然后往回看执行了的代码，找到真正要看的岱庙。
	 */
	public Integer save(T t) {
		/**
		 * 记录的创建时间和更新时间都有相应字段，设置到t中去，有两点要注意： 1.BaseService<T extends
		 * BasePojo>;如果只写BaseService<T>而不给泛型上限，是无法为t的属性赋值的。
		 * 2.创建时间和更新时间的入参要注意，最好严谨一点。
		 */
		t.setCreated(new Date());
		t.setUpdated(t.getCreated());
		return this.mapper.insert(t);
	}

	/**
	 * 新增数据：插入一条数据,只插入不为null的字段,不会影响有默认值的字段
	 */
	public Integer saveSelective(T t) {
		t.setCreated(new Date());
		t.setUpdated(t.getCreated());
		return this.mapper.insertSelective(t);
	}

	/**
	 * 更新记录。
	 */
	public Integer update(T t) {
		t.setUpdated(new Date());
		return this.mapper.updateByPrimaryKey(t);
	}

	/**
	 * 更新记录：只更新不为null的字段。 注意点：为“创建时间”字段赋值时，最好严谨的设置为null，这样就永远不会将创建时间更新掉。
	 */
	public Integer updateSelective(T t) {
		t.setUpdated(new Date());
		t.setCreated(null);
		return this.mapper.updateByPrimaryKeySelective(t);
	}

	/**
	 * 根据主键id删除记录(物理删除)。 如果是逻辑删除，就用上面的更新即可。
	 */
	public Integer deleteById(Long id) {
		return this.mapper.deleteByPrimaryKey(id);
	}

	/**
	 * 根据条件做批量删除:delete 表名 where property in ( , ,);
	 * 
	 * @param ids:id的集合
	 * @param clazz
	 * @param property:具体的id字段名
	 * @return
	 */
	public Integer deleteByIds(List<Object> ids, Class<T> clazz, String property) {
		Example example = new Example(clazz);
		// 设置条件
		example.createCriteria().andIn(property, ids);
		return this.mapper.deleteByExample(example);
	}

	/**
	 * 根据条件删除数据。
	 */
	public Integer deleteByWhere(T record) {
		return this.mapper.delete(record);
	}
}
