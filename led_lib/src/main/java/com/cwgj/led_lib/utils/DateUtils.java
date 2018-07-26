package com.cwgj.led_lib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：
 * +----------------------------------------------------------------------
 * | 创建者   :  ldw
 * +----------------------------------------------------------------------
 * | 时　　间 ：2018/7/25 16:08
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/
public class DateUtils {

    public static String getyyyyMMddHHmmss(long day) {
        // 定义日期格式
        SimpleDateFormat matter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(day);
        return matter.format(date);
    }

}
