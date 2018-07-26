package com.cwgj.led_lib.ledproduct;


import com.cwgj.led_lib.LedDrive;
import com.cwgj.led_lib.utils.ByteUtil;
import com.cwgj.led_lib.utils.DateUtils;

import java.io.UnsupportedEncodingException;

/**
 * +----------------------------------------------------------------------
 * |  说明     ：欧冠协议驱动实现类
 * +----------------------------------------------------------------------
 * | 创建者   :  ldw
 * +----------------------------------------------------------------------
 * | 时　　间 ：2018/5/17 09:47
 * +----------------------------------------------------------------------
 * | 版权所有: 北京市车位管家科技有限公司
 * +----------------------------------------------------------------------
 **/
public class OuGuanLed implements LedDrive {


    //即时消息
    @Override
    public byte[] packTextCommad(int line, String body, int timeLen) {
        return packBasicTextCommad(0, line, getTextControlTime(timeLen).concat(body));
    }

    //永久消息
    @Override
    public byte[] packForeverTextCommad(int line, String body) {
        return packBasicTextCommad(1, line, body);
    }


    //当前时间（时间需要软件同步）
    @Override
    public byte[] packLocalDate(int line, int timeLen) {
        if(timeLen == 0){
            //时间永久显示   注意：欧冠显示屏这里有个bug，设置永久显示时间，开始只会显示[d000],需要再发个指令显示下即时时间，所以这里发了2条指令,也可以通过重启led
            return ByteUtil.addBytes(packBasicTextCommad(
                    1, line, getLocalTimeControlTime(timeLen)),
                    packBasicTextCommad(0, line, getLocalTimeControlTime(timeLen)));
        }else {
            return packBasicTextCommad(0, line, getLocalTimeControlTime(timeLen));
        }

    }

    //欧冠led时间没有硬件时钟，使用软件中断定时的，需要24小时更新一次时间，纠正误差
    @Override
    public byte[] updateLocalDate(long currentTime) {
        try {
            return getPackageBytes(Type, CMD_UPDATE_TIME, DateUtils.getyyyyMMddHHmmss(currentTime) );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private byte[] packBasicTextCommad(int type, int line, String body) {
        int cmd = 0;
        if(line == 1){
            cmd = (type==0?CMD_TEXT_1_Line:CMD_TEXT_LONG_1_Line);
        }else if(line == 2){
            cmd = (type==0?CMD_TEXT_2_Line:CMD_TEXT_LONG_2_Line);
        }else if(line == 3){
            cmd = (type==0?CMD_TEXT_3_Line:CMD_TEXT_LONG_3_Line);
        }else if(line == 4){
            cmd = (type==0?CMD_TEXT_4_Line:CMD_TEXT_LONG_4_Line);
        }
        return getTextPackageBytes(cmd, body);
    }



    //帧头
    public static  final byte[] headBytes = {(byte) 0xA5, (byte) 0xA5};
    //485显示模式
    public static final int Type = 0x03;

    public static final int CMD_UPDATE_TIME = 0x53;

//            1、显示即时自定义信息（第1行）       数据命令：    05
//            2、显示即时自定义信息（第2行）       数据命令：    06
//            3、显示即时自定义信息（第3行）       数据命令：    17
//            4、显示即时自定义信息（第4行）       数据命令：    18

//            5、显示永久自定义信息（第1行）       数据命令：    41
//            6、显示永久自定义信息（第2行）       数据命令：    42
//            7、显示永久自定义信息（第3行）       数据命令：    43
//            8、显示永久自定义信息（第4行）       数据命令：    44

    public static final int CMD_TEXT_1_Line = 0x05;

    public static final int CMD_TEXT_2_Line = 0x06;

    public static final int CMD_TEXT_3_Line = 0x17;

    public static final int CMD_TEXT_4_Line = 0x18;

    public static final int CMD_TEXT_LONG_1_Line = 0x41;

    public static final int CMD_TEXT_LONG_2_Line = 0x42;

    public static final int CMD_TEXT_LONG_3_Line = 0x43;

    public static final int CMD_TEXT_LONG_4_Line = 0x44;
    //帧尾
    public static  final byte[] endBytes = {(byte) 0xBE, (byte) 0xEF};
//
//    格式[txxx]：控制显示文本显示时间长度，xxx为000到255 单位为秒  000为永久显示，
//    格式[dxxx]：打开控制板日期时间显示， xxx为000到255 单位为秒  000为永久显示，
//    格式[sxxx]：设置空闲显示文本，xxx为000到255 单位为秒  xxx无意义，

    private String getTextControlTime(int timeLen){
        return getBasicTextControlTime("t", timeLen);
    }

    private String getLocalTimeControlTime(int timeLen){
        return getBasicTextControlTime("d", timeLen);
    }

    //文本显示时长
    private String getBasicTextControlTime(String tag, int time){
        String timeLenStr;
        if(time >99){
            timeLenStr = String.valueOf(time);
        }else if(time >9){
            timeLenStr = "0"+time;
        }else {
            timeLenStr = "00" + time;
        }
        return String.format("[%s%s]", tag,timeLenStr);
    }

    //空闲显示文本
    public static String getFreeText(){
        return "[s000]";
    }


    /**
     * 显示文字
     * @param cmd   文字数据指令
     * @param body  文字内容
     * @return
     * @throws UnsupportedEncodingException
     */
    private  byte[] getTextPackageBytes(int cmd, String body) {
        try {
            return  getPackageBytes(Type, cmd, body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param mode  0x03 485显示文字
     * @param cmd   数据指令
     * @param body  显示的文字
     * @return
     * @throws UnsupportedEncodingException
     */
    private   byte[] getPackageBytes(int mode, int cmd, String body) throws UnsupportedEncodingException {
        byte[] bodyBytes = body.getBytes("GBK");//获得body的字节数组
        int bodyLen = bodyBytes.length;
        //2个帧头字节 + 1个通信类型字节 + 1个数据长度字节  + 1个字节数据命令 + 1个字节数据长度 + n个data字节 + 1个字节checksum + 2个字节帧尾
        int totalLen = bodyLen + 9;
        byte[] packageBytes = new byte[totalLen];
        packageBytes[0] = headBytes[0];
        packageBytes[1] = headBytes[1];
        packageBytes[2] = (byte) mode;
        packageBytes[3] = (byte) (bodyLen + 3); //n个字节数据 + 1个指令 + 1个数据长度 + 1个checksum
        packageBytes[4] = (byte) cmd;
        packageBytes[5] = (byte) bodyLen;
        int index = 5;
        for(int i=0; i< bodyLen; i++){
            index ++;
            packageBytes[index] = bodyBytes[i];
        }
        //DATA 字节求和取反获取checksum
        packageBytes[totalLen -3] = (byte) ByteUtil.getCheckSum(ByteUtil.getByteArr(packageBytes, 4,  totalLen - 3)); //checksum
        packageBytes[totalLen -2] = endBytes[0];
        packageBytes[totalLen -1] = endBytes[1];

        return packageBytes;
    }


}
