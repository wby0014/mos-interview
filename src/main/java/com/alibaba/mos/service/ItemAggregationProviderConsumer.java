/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.mos.api.ProviderConsumer;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.ItemDO;
import com.alibaba.mos.data.SkuDO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author superchao
 * @version $Id: ItemAggregationProviderConsumerImpl.java, v 0.1 2019年11月20日 3:06 PM superchao Exp $
 */
@Service
public class ItemAggregationProviderConsumer implements ProviderConsumer<List<ItemDO>> {

    @Autowired
    SkuReadService skuReadService;

    private static final String ORIGIN_TYPE = "ORIGIN";
    private static final String DIGITAL_TYPE = "DIGITAL";

    @Override
    public void execute(ResultHandler<List<ItemDO>> handler) {
        List<SkuDO> originList = new ArrayList<>();
        List<SkuDO> digitalList = new ArrayList<>();
        skuReadService.loadSkus(skuDO -> {
            if (ORIGIN_TYPE.equals(skuDO.getSkuType())) {
                originList.add(skuDO);
            } else if (DIGITAL_TYPE.equals(skuDO.getSkuType())) {
                digitalList.add(skuDO);
            }
            return skuDO;
        });
        List<ItemDO> itemList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(originList)) {
            itemList.addAll(aggregationItem(originList, ORIGIN_TYPE));
        }
        if (CollectionUtils.isNotEmpty(digitalList)) {
            itemList.addAll(aggregationItem(digitalList, DIGITAL_TYPE));
        }
        handler.handleResult(itemList);
    }

    private List<ItemDO> aggregationItem(List<SkuDO> skuDOS, String skuType) {
        Map<String, List<SkuDO>> groupMap = new HashMap<>();
        if (ORIGIN_TYPE.equals(skuType)) {
            // 按artNo聚合成ITEM
            groupMap = skuDOS.stream().collect(Collectors.groupingBy(SkuDO::getArtNo));
        } else if (DIGITAL_TYPE.equals(skuType)) {
            // 按spuId聚合成ITEM
            groupMap = skuDOS.stream().collect(Collectors.groupingBy(SkuDO::getSpuId));
        }
        List<ItemDO> itemList = new ArrayList<>();
        groupMap.forEach((key, value) -> {
            BigDecimal max = value.stream().map(SkuDO::getPrice).max(Comparator.comparing(x -> x)).orElse(null);
            BigDecimal min = value.stream().map(SkuDO::getPrice).min(Comparator.comparing(x -> x)).orElse(null);
            List<BigDecimal> totalList = new ArrayList<>();
            value.stream().map(SkuDO::getInventoryList).forEach((s) -> {
                BigDecimal reduce = s.stream().map(ChannelInventoryDO::getInventory).reduce(BigDecimal.ZERO, BigDecimal::add);
                totalList.add(reduce);
            });
            BigDecimal totalInventory = totalList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            // 聚合结果需要包含: item的最大价格、最小价格、sku列表及总库存
            itemList.add(ItemDO.builder().artNo(key).maxPrice(max).minPrice(min)
                    .skuIds(value.stream().map(SkuDO::getId).collect(Collectors.toList()))
                    .inventory(totalInventory).build());
        });
        return itemList;
    }
}