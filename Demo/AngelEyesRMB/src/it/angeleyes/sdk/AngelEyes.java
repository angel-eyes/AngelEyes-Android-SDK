//
//  Copyright 2012 www.angeleyes.it. All rights reserved
//
//  AngelEyes.java
//
//  Created by koupoo
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are met:
//      * Redistributions of source code must retain the above copyright
//        notice, this list of conditions and the following disclaimer.
//      * Redistributions in binary form must reproduce the above copyright
//        notice, this list of conditions and the following disclaimer in the
//        documentation and/or other materials provided with the distribution.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
//

package it.angeleyes.sdk;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 提供天使眼图片识别服务的对象
 */
public class AngelEyes{
	private String appKey;
	private String appSecret;
	
	/**
	 *用指定的application key和application secret初始化一个AngelEyes对象
	 *@param appKey application key
	 *@param appSecret application secret
	 *@return 返回一个AngelEyes对象
	 */
	public AngelEyes(String appKey, String appSecret){
		super();
		this.appKey = appKey;
		this.appSecret = appSecret;
	}
	
	/**
	 *对指定的图片进行识别
	 *使用默认的设置对图片进行识别。如果该方法的图片识率或耗时不能满足你的要求，请使用能指定识别参数的识别方法:
	 *identifyImage(Bitmap, Rect, float, float, int, int, float)
	 *@param image 待识别的图片
	 *@return 以Map<String, Object>对象的形式返回识别结果信息。目前返回Map<String, Object>对象只包含一个key:tags，tags所对应的的对象是
	 *        一个List<String>对象，这个List<String>对象的第一个元素就是开发者在AngelEyes应用管理平台中为样本图片设置的标签。注意:识别成功并不代表识别准确。
	 *@throws AngelEyesException 识别操作失败时，抛出此类异常
	 */
	public Map<String, Object> identifyImage(Bitmap image) throws AngelEyesException{
		return identifyImage(image, 0.25f, 0.25f, 20, 255, 1);
	}
	
	/**
	 *对指定的图片进行识别
	 *使用指定的识别参数对图片进行识别。通过设置识别参数可以控制图像识别的准确率和识别耗时。
	 *@param image 待识别的图片
	 *@param roi 感兴趣区域，roi和Rect(0, 0, 待识别的图片的宽, 待识别的图片的高)的重叠区域将作为图片的有效的识别区域。
	 *@param r   初选阈值, 有效取值范围为[0.16, 0.25]。r取值越大,图片识别率越高，但识别要用的时间越长。
	 *@param rr  误差平方和阈值,有效取值范围是[0.16, 0.25]。rr取值越大,图片识别率越高，但识别要用的时间越长。
	 *@param maxDrift 抗皱参数,有效取值范围是[4, 36]。如果待识别图片中的待识别对象比较折皱，maxDrif应该设置大点，否则maxDrif设置小点;
	 *@param kpCount 图片特征保留个数,有效取值范围是[50, 256]； kpCount取值越大,图片识别率越高，但识别要用的时间越长；
	 *               如果待识别的图片比较复杂（内容比较丰富）,kpCount应该设置大点；kpCount取200时，一般能达到理想的识别准确率。
	 *@param scale 图片缩放系数，有效取值范围是[0.0, 1.0]
	 *@return 以Map<String, Object>对象的形式返回识别结果信息。目前返回Map<String, Object>对象只包含一个key:tags，tags所对应的的对象是
	 *        一个List<String>对象，这个List<String>对象的第一个元素就是开发者在AngelEyes应用管理平台中为样本图片设置的标签。注意:识别成功并不代表识别准确。
	 *@throws AngelEyesException 识别操作失败时，抛出此类异常
	 */
	public Map<String, Object> identifyImage(Bitmap image, Rect roi, float r, float rr, int maxDrift, int kpCount, float scale) throws AngelEyesException{
		Rect roiImageRect = new Rect(roi);
		roiImageRect.intersect(0, 0, image.getWidth(), image.getHeight());
		
		if(roiImageRect.isEmpty()){
			throw new AngelEyesException("roi is invalid, there are not intersection between roi and image");
		}
		
		Bitmap roiImage = Bitmap.createBitmap(image, roiImageRect.left, roiImageRect.top, roiImageRect.width(), roiImageRect.height());
		
		return identifyImage(roiImage, r, rr, maxDrift, kpCount, scale);
	}
	
	private Map<String, Object> identifyImage(Bitmap image, float r, float rr, int maxDrift, int kpCount, float scale) throws AngelEyesException{
		checkAppKeyAndAppSecret();
		
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 40000);
		
		HttpPost postRequst = new HttpPost("http://api.angeleyes.it/identify.json");
		
		HttpEntity requestEntity = createRequestEntity(image, r, rr, maxDrift, kpCount, scale);
		
		postRequst.setEntity(requestEntity);

		HttpResponse response;
		try {
			response = httpClient.execute(postRequst);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			throw new AngelEyesException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AngelEyesException(e);
		}
		
		HttpEntity responseEntity = response.getEntity();
		
		if(responseEntity == null){
			throw new AngelEyesException("send request failed");
		}
		
		String result = null;
		try {
			result = EntityUtils.toString(responseEntity);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new AngelEyesException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AngelEyesException(e);
		}
		
		List<String> tagList = new ArrayList<String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			JSONObject jsonObj  = new JSONObject(result);

			String errorInfo = jsonObj.getString("error");

			if (errorInfo.length() > 0) {
				throw new AngelEyesException(errorInfo);
			}

			JSONArray tagJsonArray = jsonObj.getJSONArray("tags");

			if (tagJsonArray.length() > 0) {
				tagList.add(tagJsonArray.getString(0));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new AngelEyesException(e);
		}
		
		resultMap.put("tags", tagList);
		
		return resultMap;
	}
	
	private void checkAppKeyAndAppSecret() throws AngelEyesException{
		if(appKey == null || appKey.length() == 0){
			throw new AngelEyesException("appkey can't be empty");
		}
		
		if(appSecret == null || appSecret.length() == 0){
			throw new AngelEyesException("appSecret can't be empty");
		}
	}
	
	private HttpEntity createRequestEntity(Bitmap image, float r, float rr, int maxDrift, int kpCount, float scale) throws AngelEyesException{
		MultipartEntity requestEntity = new MultipartEntity();
		
		try {
			requestEntity.addPart("appkey", new StringBody(appKey));
			requestEntity.addPart("appsecret", new StringBody(appSecret));
			requestEntity.addPart("r", new StringBody(Double.toString(r)));
			requestEntity.addPart("rr", new StringBody(Double.toString(rr)));
			requestEntity.addPart("max_drift", new StringBody(Integer.toString(maxDrift)));
			requestEntity.addPart("kp_count", new StringBody(Integer.toString(kpCount)));
			requestEntity.addPart("scale", new StringBody(Double.toString(scale)));
		} catch (UnsupportedEncodingException e) {
			throw new AngelEyesException(e);
		}
		
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        image.compress(CompressFormat.JPEG, 82, bos);
		byte[] imageData =  bos.toByteArray();
		
		ByteArrayBody imageBody = new ByteArrayBody(imageData, "image");
		
		requestEntity.addPart("pic", imageBody);
		
		return requestEntity;
	}
}
