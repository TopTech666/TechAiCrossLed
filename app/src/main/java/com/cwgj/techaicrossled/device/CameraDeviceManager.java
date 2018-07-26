package com.cwgj.techaicrossled.device;

import com.vz.PlateResult;
import com.vz.tcpsdk;

/**
 * 相机设备操作类
 */
public class CameraDeviceManager {
	/*
	* 存在的问题：
	*     1. 判断当前设备是否存在异常： open相机看是否成功。open相机前必须close相机，否则车牌识别会多次回调，但是close相机过程中可能会有车进出场，会出现问题!!!
	*     2. 判断当前相机是否是连接： 连接状态在网络断开可以反馈错误， 但是如果相机其余问题，例如无法识别是否也能反馈在断开的状态！！！
	*     3. 抬杠落杠目前参考的是C端的  io1抬杠 io0落杠
	*     4. 语音通话按钮是以触发识别到无牌车开启，定时N分钟后失效
	* */
	private static final String TAG = "VideoDeviceManager";
	
	private CameraDeviceInfo mCameraDeviceInfo = null;

	private  boolean     serialOneFlag = false;
	private  boolean     serialTwoFlag = false;
	private  boolean     openFlag = false;//相机是否已开启

	//白名单
	private  tcpsdk.onWlistReceiver  mOnWlistReceiver =null;

	private static CameraDeviceManager sCameraDeviceManager;

	private CameraDeviceManager(){
	}


	public static CameraDeviceManager getInstance(){
		if(sCameraDeviceManager == null){
			synchronized (CameraDeviceManager.class){
				if(sCameraDeviceManager == null){
					sCameraDeviceManager = new CameraDeviceManager();
				}
			}
		}
		return sCameraDeviceManager;
	}

	//车牌识别回调
	private tcpsdk.OnDataReceiver mOnDataReceiver = new tcpsdk.OnDataReceiver() {
		@Override
		public void onDataReceive(int handle, PlateResult plateResult, int uNumPlates, int eResultType,
                                  byte[] pImgFull, int nFullSize, byte[] pImgPlateClip, int nClipSize) {


		}
	};

	//gpio 地感触发回调
	private tcpsdk.onGpioReceiver mOnGpioReceiver = new tcpsdk.onGpioReceiver() {
		@Override
		public void onGpioReceiver(int handle, int nGPIOId, int nVal) {
		}
	};

	/**
	 * 判断是否是特殊车牌
	 * @param carNum
	 * @return
	 */
	private boolean isSpecialCarNum(String carNum){

		if(carNum.contains("WJ") || carNum.contains("警")|| carNum.contains("军") || carNum.contains("领")||carNum.startsWith("BB"))
			return true;
		//类型不准确，臻识不建议使用
//			if(plateResult.nType == 5 || plateResult.nType == 6 || plateResult.nType == 8 || plateResult.nType == 9 || plateResult.nType == 10
//					|| plateResult.nType == 11 || plateResult.nType == 14 || plateResult.nType == 15 || plateResult.nType == 16 || plateResult.nType == 17 )
//				return true;
		return false;
	}


	//初始化相机
	public void setUp(){
		tcpsdk.getInstance().setup();
	}

	//释放相机资源
	public void cleanUp(){
		try{
			tcpsdk.getInstance().cleanup();

		}catch (Exception e){
		    e.printStackTrace();
		}
	}

	//添加device
	public void setDevice(CameraDeviceInfo device){
		this.mCameraDeviceInfo = device;
	}

	public CameraDeviceInfo getDevice(){
		return mCameraDeviceInfo;
	}

	/**
	 * 打开设备
	 * @return
	 */
	public boolean openDevice() {
	   if(open(mCameraDeviceInfo.deviceName,
			   mCameraDeviceInfo.ip,
			   mCameraDeviceInfo.port,
			   mCameraDeviceInfo.userName, mCameraDeviceInfo.userPwd)) {
		   if(mOnDataReceiver != null)//设置车牌识别回调
			   tcpsdk.getInstance().setPlateInfoCallBack(mCameraDeviceInfo.handle,mOnDataReceiver,1);
		   if(mOnGpioReceiver !=null)
			   tcpsdk.getInstance().SetRecieveGpioCallback(mCameraDeviceInfo.handle, mOnGpioReceiver);
		   return true;
	   }
	   return false;
	}

	//重启设备
	public synchronized boolean resetDevice(){
		//必须先关闭device否则车牌会多次识别回调
		closeDevice();
		return openDevice();
	}

	/**
	 * 打开设备
	 * @return
	 */
	private boolean open(String deviceName, String ip, int port, String username, String userpassword) {


		//打开设备
		int res = tcpsdk.getInstance().open(ip.getBytes(),ip.length(), port, username.getBytes(),
 			   username.length(), userpassword.getBytes(), userpassword.length());
		if(res > 0) {
			mCameraDeviceInfo.handle = res;
			mCameraDeviceInfo.deviceName = deviceName;
			mCameraDeviceInfo.ip = ip;
			mCameraDeviceInfo.port = port;
			mCameraDeviceInfo.userName = username;
			mCameraDeviceInfo.userPwd = userpassword;
			openFlag = true;

		 	return true;
		}
		 return false;
	}



	/**
	 * 关闭设备
	 * @return
	 */
	public void closeDevice() {
		if(mCameraDeviceInfo == null)
			return;
		try{
			if((serialOneFlag || serialTwoFlag) && mCameraDeviceInfo.handle >0) {
				//透明通道停止发送数据
				tcpsdk.getInstance().serialStop(mCameraDeviceInfo.handle);
				serialOneFlag = false;
				serialTwoFlag = false;
			}
			if(mCameraDeviceInfo.handle > 0) {
				//关闭设备
				tcpsdk.getInstance().close(mCameraDeviceInfo.handle);
				mCameraDeviceInfo.handle = 0;
			}
			openFlag = false;
		}catch (Exception e){
		    e.printStackTrace();
		}

	}

	//获取设备信息
	public CameraDeviceInfo getVideoDeviceInfo() {
		return mCameraDeviceInfo;
	}


	//led发送串口数据 默认通道0
	public boolean serialSend(byte[] pData) {
		return serialSend(0,pData);
	}

	/**
	 * 透明通道发送数据
	 * @param serialNum 通道 0 1
	 * @param pData
	 * @return
	 */
	public boolean serialSend(int serialNum,byte[] pData) {
		if(!openFlag)
			return false;
		if(serialNum < 0 || serialNum > 1) {
			return false;
		}
		// 串口1
		if( serialNum == 0 ) {
			if(!serialOneFlag) {
				if(tcpsdk.getInstance().serialStart(mCameraDeviceInfo.handle, serialNum) != 0) //开启透明通道
					return false;
				serialOneFlag = true;
			}
		} else {
			if(!serialTwoFlag) {
				if(tcpsdk.getInstance().serialStart(mCameraDeviceInfo.handle, serialNum) != 0)
					return false;
				serialTwoFlag = true;
			}
		}
		if(tcpsdk.getInstance().serialSend(mCameraDeviceInfo.handle, serialNum, pData, pData.length) != 0)
			return false;
		else
			return true;
	}

	/**
	 * 获取截图
	 * @param imgBuffer
	 * @param imgBufferMaxLength
	 * @return
	 */
	public int getSnapImageData(byte[] imgBuffer, int imgBufferMaxLength) {
		if(!openFlag)
			return -1;
		return tcpsdk.getInstance().getSnapImageData(mCameraDeviceInfo.handle, imgBuffer, imgBufferMaxLength);
	}
	 
	//播放语音
	public int playVoice( byte[] voice, int interval, int volume, int male) {
		if(!openFlag)
			return -1;
		return tcpsdk.getInstance().playVoice(mCameraDeviceInfo.handle, voice, interval, volume, male);
	}

	//获取设备打开状态
	public boolean getopenFlag() {
		return openFlag;
	}

	//手动触发车牌识别
	public int forceTrigger() {
		if(!openFlag)
			return  -1;
		return tcpsdk.getInstance().forceTrigger(mCameraDeviceInfo.handle);
	}

	//是否连接中
	public synchronized boolean isConnected(){
		return tcpsdk.getInstance().isConnected(mCameraDeviceInfo.handle);
	}


	//抬杠
	public int openGate(){
		//out1 开
		return setIoOutputAuto((short) 1, 600);
	}

	//落杠
	public int downGate(){
		//out0 落
		return setIoOutputAuto((short) 0, 600);
	}


	/**
	 * 开闸
	 * @param uChnId  通道号
	 * @param nDuration
	 * @return
	 */
	public int  setIoOutputAuto( short uChnId, int nDuration) {
		if(!openFlag)
			return  -1;
		return tcpsdk.getInstance().setIoOutputAuto(mCameraDeviceInfo.handle,uChnId,nDuration);
	}


//
//	 public  void setWlistInfoCallBack(tcpsdk.onWlistReceiver recevier) {
//		 if(!openFlag)
//		  return ;
//		  tcpsdk.getInstance().setWlistInfoCallBack(mVideoDeviceInfo.handle, recevier);
//	 }
//
//	 public  int  importWlistVehicle(WlistVehicle wllist) {
//		   if(!openFlag)
//				return -1;
//			return tcpsdk.getInstance().importWlistVehicle(mVideoDeviceInfo.handle, wllist);
//	   }
//
//	   public  int  deleteWlistVehicle(byte[] plateCode) {
//		   if(!openFlag)
//				return -1;
//			return tcpsdk.getInstance().deleteWlistVehicle(mVideoDeviceInfo.handle, plateCode);
//	   }
//
//	   public  int  queryWlistVehicle(byte[] plateCode) {
//		   if(!openFlag)
//				return -1;
//			return tcpsdk.getInstance().queryWlistVehicle(mVideoDeviceInfo.handle, plateCode);
//	   }
//
//



}
