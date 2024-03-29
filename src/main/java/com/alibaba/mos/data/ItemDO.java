/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.data;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author superchao
 * @version $Id: ItemDO.java, v 0.1 2019年10月28日 11:02 AM superchao Exp $
 */
@Builder
@Data
public class ItemDO implements Serializable {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 货号
     */
    private String artNo;

    /**
     * itemid
     */
    private String spuId;

    /**
     * 库存数量, 保留小数点后2位
     */
    private BigDecimal inventory;

    /**
     * 最大价格, 保留小数点后2位
     */
    private BigDecimal maxPrice;

    /**
     * 最小价格, 保留小数点后2位
     */
    private BigDecimal minPrice;

    /**
     * 该item下的sku id列表
     */
    private List<String> skuIds;
}