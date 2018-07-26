package com.cwgj.led_lib;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：LED 驱动接口
 * +----------------------------------------------------------------------
 * | 创建者   :  ldw
 * +----------------------------------------------------------------------
 * | 时　　间 ：2018/5/17 09:42
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/
public interface LedDrive {

    /**
     * 驱动显示屏显示文字(即时显示)
     * @param line  第几行
     * @param body  显示内容
     * @param timeLen 显示的时间长度
     * @return
     */
    byte[] packTextCommad(int line, String body, int timeLen);

    /**
     * 永久显示文字
     * @param line  第几行
     * @param body  显示内容
     * @return
     */
    byte[] packForeverTextCommad(int line, String body);

    /**
     *显示当前时间
     * @param line  第几行
     * @param timeLen  显示的时长 s
     * @return
     */
    byte[] packLocalDate(int line, int timeLen);

    //更新当前时间
    byte[] updateLocalDate(long currentTime);

}


