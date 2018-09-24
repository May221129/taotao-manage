package com.taotao.manage.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商品描述类
 */
@Table(name = "tb_item_desc")
public class ItemDesc extends BasePojo{
    
    @Id//对应tb_item中的id
    private Long itemId;
    
    private String itemDesc;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemDesc() {
        return itemDesc;
    }

    public void setItemDesc(String itemDesc) {
        this.itemDesc = itemDesc;
    }
    
    

}