/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alibaba.mos.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.mos.api.SkuReadService;
import com.alibaba.mos.data.ChannelInventoryDO;
import com.alibaba.mos.data.SkuDO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: 实现
 *
 * @author superchao
 * @version $Id: SkuReadServiceImpl.java, v 0.1 2019年10月28日 10:49 AM superchao Exp $
 */
@Slf4j
@Service
public class SkuReadServiceImpl implements SkuReadService {

    /**
     * 这里假设excel数据量很大无法一次性加载到内存中
     *
     * @param handler
     */
    @Override
    public void loadSkus(SkuHandler handler) {
        InputStream inputStream = this.getClass().getResourceAsStream("/data/skus.xls");
        HSSFWorkbook hb;
        HSSFSheet sheet;
        Row row;
        SkuDO skuDO;
        try {
            hb = new HSSFWorkbook(inputStream);
            int numberOfSheets = hb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                sheet = hb.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }
                int lastRowNum = sheet.getLastRowNum();
                if (lastRowNum == 0) {
                    continue;
                }
                for (int j = 1; j <= lastRowNum; j++) {
                    row = sheet.getRow(j);
                    if (row == null) {
                        continue;
                    }
                    skuDO = new SkuDO();
                    short lastCellNum = row.getLastCellNum();
                    for (int k = 0; k <= lastCellNum; k++) {
                        if (row.getCell(k) == null) {
                            continue;
                        }
                        if (k == 6) {
                            Cell cell = row.getCell(k);
                            skuDO.setInventoryList(JSONObject.parseArray(cell.getStringCellValue(), ChannelInventoryDO.class));
                            continue;
                        }
                        String attrName = getObjFieldMap().get(k);
                        Class<?> attrType = BeanUtils.findPropertyType(attrName, SkuDO.class);
                        Cell cell = row.getCell(k);
                        if (cell != null && !("").equals(cell.toString().trim())) {
                            Object val = getValue(cell, attrType);
                            if (!org.springframework.util.StringUtils.isEmpty(val)) {
                                setter(skuDO, attrName, val, attrType, j, k);
                            }
                        }
                    }
                    handler.handleSku(skuDO);
                }
            }
        } catch (IOException e) {
            log.error("xls file read error", e);
        }
    }


    private static Object getValue(Cell cell, Class<?> attrType) {
        Object val = null;
        if (cell.getCellTypeEnum() == CellType.BOOLEAN) {
            val = cell.getBooleanCellValue();
        } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (attrType == String.class) {
                    val = sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue()));
                } else {
                    val = DateUtil.parseYYYYMMDDDate(sdf.format(DateUtil.getJavaDate(cell.getNumericCellValue())));
                }
            } else {
                if (attrType == String.class) {
                    cell.setCellType(CellType.STRING);
                    val = cell.getStringCellValue();
                } else if (attrType == BigDecimal.class) {
                    val = BigDecimal.valueOf(cell.getNumericCellValue());
                } else if (attrType == long.class) {
                    val = (long) cell.getNumericCellValue();
                } else if (attrType == Double.class) {
                    val = cell.getNumericCellValue();
                } else if (attrType == Float.class) {
                    val = (float) cell.getNumericCellValue();
                } else if (attrType == int.class || attrType == Integer.class) {
                    val = (int) cell.getNumericCellValue();
                } else if (attrType == Short.class) {
                    val = (short) cell.getNumericCellValue();
                } else {
                    val = cell.getNumericCellValue();
                }
            }
        } else if (cell.getCellTypeEnum() == CellType.STRING) {
            val = cell.getStringCellValue();
        }
        return val;
    }


    private static void setter(Object obj, String attrName, Object attrValue, Class<?> attrType, int row, int column) {
        try {
            Method method = obj.getClass().getMethod("set" + Character.toUpperCase(attrName.charAt(0)) + attrName.substring(1), attrType);
            method.invoke(obj, attrValue);
        } catch (Exception e) {
            log.error("set method attr error", e);
            throw new RuntimeException("第" + (row + 1) + "行 " + (column + 1) + "列 属性：" + attrName + " 赋值异常 ");
        }
    }


    private Map<Integer, String> getObjFieldMap() {
        Map<Integer, String> map = new HashMap<>(16);
        map.put(0, "id");
        map.put(1, "name");
        map.put(2, "artNo");
        map.put(3, "spuId");
        map.put(4, "skuType");
        map.put(5, "price");
        map.put(6, "inventoryList");
        return map;
    }
}