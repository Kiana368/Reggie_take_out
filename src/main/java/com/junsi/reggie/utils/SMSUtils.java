package com.junsi.reggie.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.exceptions.ServerException;
import com.google.gson.Gson;
import java.util.*;
import com.aliyuncs.dysmsapi.model.v20170525.*;

/**
 * 短信发送工具类
 */
public class SMSUtils {

	/**
	 * 发送短信
	 * @param signName 签名
	 * @param templateCode 模板
	 * @param phoneNumbers 手机号
	 * @param param 参数
	 */
	public static void sendMessage(String signName, String templateCode,String phoneNumbers,String param){
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI5t6iJU9iXn2komGVjQx9", "5w0LeaT7qY0l0rNbKGWxprvfRLjnbl");
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSysRegionId("cn-hangzhou");
		request.setPhoneNumbers(phoneNumbers);
		request.setSignName(signName);
		request.setTemplateCode(templateCode);
		request.setTemplateParam("{\"code\":\""+param+"\"}");
		try {
			SendSmsResponse response = client.getAcsResponse(request);
			System.out.println("短信发送成功");
		}catch (ClientException e) {
			e.printStackTrace();
		}

		// 新版
//		DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", "LTAI5t6iJU9iXn2komGVjQx9", "5w0LeaT7qY0l0rNbKGWxprvfRLjnbl");
//		/** use STS Token
//		 DefaultProfile profile = DefaultProfile.getProfile(
//		 "<your-region-id>",           // The region ID
//		 "<your-access-key-id>",       // The AccessKey ID of the RAM account
//		 "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
//		 "<your-sts-token>");          // STS Token
//		 **/
//		IAcsClient client = new DefaultAcsClient(profile);
//
//		SendSmsRequest request = new SendSmsRequest();
//		request.setPhoneNumbers(phoneNumbers);//接收短信的手机号码
//		request.setSignName(signName);//短信签名名称
//		request.setTemplateCode(templateCode);//短信模板CODE
//		request.setTemplateParam(param);//短信模板变量对应的实际值
//
//		try {
//			SendSmsResponse response = client.getAcsResponse(request);
//			System.out.println(new Gson().toJson(response));
//		} catch (ServerException e) {
//			e.printStackTrace();
//		} catch (ClientException e) {
//			System.out.println("ErrCode:" + e.getErrCode());
//			System.out.println("ErrMsg:" + e.getErrMsg());
//			System.out.println("RequestId:" + e.getRequestId());
//		}
	}

}
