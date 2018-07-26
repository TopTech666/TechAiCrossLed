package com.cwgj.techaicrossled.device;

/**
 * 设备
 */
public class CameraDeviceInfo {

	   public int handle = 0; //设备句柄
	   public String deviceName = "设备1";
	   public String ip = "192.168.7.98";
	   public int port = 8131;

	   //相机的帐号和密码目前逻辑是写死admin
	   public String userName = "admin";
	   public String userPwd = "admin";
	   //抬杠IO 默认io1
	   public short chnId = 0;

	public CameraDeviceInfo( String ip) {
		this.ip = ip;
	}

	  public CameraDeviceInfo() {
	  }
	   
	   
	   public boolean equals(Object object)
	   {
		   if(object == this)
			   return true;
		   
		   if(object != null && getClass() == object.getClass()  )
		   {
			   CameraDeviceInfo other = (CameraDeviceInfo)object;
			   
			   if( handle == other.handle &&
					   ip == other.ip &&
					   port == other.port &&
					   userName == other.userName &&
					   userPwd == other.userPwd) {
				   return true;
			   }
		   }
		   return false;
	   }
	   
	   public int hashCode() {
         return handle;
	   }
}