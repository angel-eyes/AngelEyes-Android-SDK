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
 * �ṩ��ʹ��ͼƬʶ�����Ķ���
 */
public class AngelEyes{
	private String appKey;
	private String appSecret;
	
	/**
	 *��ָ����application key��application secret��ʼ��һ��AngelEyes����
	 *@param appKey application key
	 *@param appSecret application secret
	 *@return ����һ��AngelEyes����
	 */
	public AngelEyes(String appKey, String appSecret){
		super();
		this.appKey = appKey;
		this.appSecret = appSecret;
	}
	
	/**
	 *��ָ����ͼƬ����ʶ��
	 *ʹ��Ĭ�ϵ����ö�ͼƬ����ʶ������÷�����ͼƬʶ�ʻ��ʱ�����������Ҫ����ʹ����ָ��ʶ�������ʶ�𷽷�:
	 *identifyImage(Bitmap, Rect, float, float, int, int, float)
	 *@param image ��ʶ���ͼƬ
	 *@return ��Map<String, Object>�������ʽ����ʶ������Ϣ��Ŀǰ����Map<String, Object>����ֻ����һ��key:tags��tags����Ӧ�ĵĶ�����
	 *        һ��List<String>�������List<String>����ĵ�һ��Ԫ�ؾ��ǿ�������AngelEyesӦ�ù���ƽ̨��Ϊ����ͼƬ���õı�ǩ��ע��:ʶ��ɹ���������ʶ��׼ȷ��
	 *@throws AngelEyesException ʶ�����ʧ��ʱ���׳������쳣
	 */
	public Map<String, Object> identifyImage(Bitmap image) throws AngelEyesException{
		return identifyImage(image, 0.25f, 0.25f, 20, 255, 1);
	}
	
	/**
	 *��ָ����ͼƬ����ʶ��
	 *ʹ��ָ����ʶ�������ͼƬ����ʶ��ͨ������ʶ��������Կ���ͼ��ʶ���׼ȷ�ʺ�ʶ���ʱ��
	 *@param image ��ʶ���ͼƬ
	 *@param roi ����Ȥ����roi��Rect(0, 0, ��ʶ���ͼƬ�Ŀ�, ��ʶ���ͼƬ�ĸ�)���ص�������ΪͼƬ����Ч��ʶ������
	 *@param r   ��ѡ��ֵ, ��Чȡֵ��ΧΪ[0.16, 0.25]��rȡֵԽ��,ͼƬʶ����Խ�ߣ���ʶ��Ҫ�õ�ʱ��Խ����
	 *@param rr  ���ƽ������ֵ,��Чȡֵ��Χ��[0.16, 0.25]��rrȡֵԽ��,ͼƬʶ����Խ�ߣ���ʶ��Ҫ�õ�ʱ��Խ����
	 *@param maxDrift �������,��Чȡֵ��Χ��[4, 36]�������ʶ��ͼƬ�еĴ�ʶ�����Ƚ����壬maxDrifӦ�����ô�㣬����maxDrif����С��;
	 *@param kpCount ͼƬ������������,��Чȡֵ��Χ��[50, 256]�� kpCountȡֵԽ��,ͼƬʶ����Խ�ߣ���ʶ��Ҫ�õ�ʱ��Խ����
	 *               �����ʶ���ͼƬ�Ƚϸ��ӣ����ݱȽϷḻ��,kpCountӦ�����ô�㣻kpCountȡ200ʱ��һ���ܴﵽ�����ʶ��׼ȷ�ʡ�
	 *@param scale ͼƬ����ϵ������Чȡֵ��Χ��[0.0, 1.0]
	 *@return ��Map<String, Object>�������ʽ����ʶ������Ϣ��Ŀǰ����Map<String, Object>����ֻ����һ��key:tags��tags����Ӧ�ĵĶ�����
	 *        һ��List<String>�������List<String>����ĵ�һ��Ԫ�ؾ��ǿ�������AngelEyesӦ�ù���ƽ̨��Ϊ����ͼƬ���õı�ǩ��ע��:ʶ��ɹ���������ʶ��׼ȷ��
	 *@throws AngelEyesException ʶ�����ʧ��ʱ���׳������쳣
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
