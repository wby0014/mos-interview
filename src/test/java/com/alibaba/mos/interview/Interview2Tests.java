package com.alibaba.mos.interview;

import com.alibaba.fastjson.JSON;
import com.alibaba.mos.api.ProviderConsumer;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ItemDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
 */
@SpringBootTest
@Slf4j
class Interview2Tests {

    @Autowired
    SkuReadService skuReadService;

    @Autowired
    ProviderConsumer<List<ItemDO>> providerConsumer;

    /**
     * 试题1：
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 在com.alibaba.mos.service.SkuReadServiceImpl中实现com.alibaba.mos.api.SkuReadService#loadSkus(com.alibaba.mos.api.SkuReadService.SkuHandler)
     * 从/resources/data/data.xls读取数据并逐条打印数据
     */
    @Test
    void readDataFromExcelWithHandlerTest() {
        AtomicInteger count = new AtomicInteger();
        skuReadService.loadSkus(skuDO -> {
            log.info("读取SKU信息={}", JSON.toJSONString(skuDO));
            count.incrementAndGet();
            return skuDO;
        });
        Assert.isTrue(count.get() == 10, "未能读取商品列表");
    }

    /**
     * 试题2：
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 计算以下统计值:
     * 1、获取价格在最中间的任意一个skuId，假设所有sku的价格都是精确到1元且一定小于1万元
     * 2、每个渠道库存量为前五的skuId列表 例如( miao:[1,2,3,4,5],tmall:[3,4,5,6,7],intime:[7,8,4,3,1]
     * 3、所有sku的总价值
     */
    void statisticsDataTest() {

    }

    /**
     * 试题3:
     * 注意： 假设sku数据很多, 无法将sku列表完全加载到内存中
     * 基于试题1, 在com.alibaba.mos.service.ItemAggregationProviderConsumer中实现一个生产者消费者, 将sku列表聚合为商品, 并通过回调函数返回,
     * 聚合规则为：
     * 对于sku type为原始商品(ORIGIN)的, 按货号(artNo)聚合成ITEM
     * 对于sku type为数字化商品(DIGITAL)的, 按spuId聚合成ITEM
     * 聚合结果需要包含: item的最大价格、最小价格、sku列表及总库存
     */
    @Test
    void aggregationSkusWithConsumerProviderTest() {
        AtomicInteger count = new AtomicInteger();
        providerConsumer.execute(list -> {
            list.forEach(item -> {
                log.info("聚合后ITEM信息={}", JSON.toJSONString(item));
                count.incrementAndGet();
            });
            return list;
        });
        Assert.isTrue(count.get() == 7, "未能聚合商品列表");
    }
}
