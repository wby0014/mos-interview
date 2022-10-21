/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.mos.api.ProviderConsumer;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ItemDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author superchao
 * @version $Id: ItemAggregationProviderConsumerImpl.java, v 0.1 2019年11月20日 3:06 PM superchao Exp $
 */
@Service
public class ItemAggregationProviderConsumer implements ProviderConsumer<List<ItemDO>> {
    @Autowired
    SkuReadService skuReadService;

    @Override
    public void execute(ResultHandler<List<ItemDO>> handler) {

    }
}