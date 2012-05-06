//
//  Copyright (c) 2012年 www.angeleyes.it. All rights reserved.
//
//  AngelEyesRMBActivity.java
//
//  Created by koupoo
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.
// 

package it.angeleyes.demo.rmb;

import it.angeleyes.demo.rmb.R;
import it.angeleyes.sdk.AngelEyes;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AngelEyesRMBActivity extends Activity {
	private TextView resultTextView;
	private ImageView rmbImageView;
	private AngelEyes angelEyes;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     
        resultTextView = (TextView)findViewById(R.id.resultTextView);
        rmbImageView = (ImageView)findViewById(R.id.rmbImageView);
        
        Button photoTakeBtn = (Button)findViewById(R.id.photoTakeBtn);
        photoTakeBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			    startActivityForResult(intent, 0);
			}
		});
        
    	new AlertDialog.Builder(this)
		.setIcon(R.drawable.icon)
		.setTitle("拍照识别提示")
		.setMessage("给人民币拍张照，让我认认它是多少钱。")
		.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		})
		.show();
        
        //本appkey和appsecet的最终解释权归www.angeleyes.it所有
        //本appkey和appsecet将在2012/07/06 18:00:00失效。
        angelEyes = new AngelEyes("4f5f2e2868fb7", "ff8d1ad73d71ad63ff3e8847b5cb12e4");
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            rmbImageView.setImageBitmap(photo);
            new ImageIdentifyAsyncTask(photo).execute();
        } else if (resultCode == RESULT_CANCELED) {
            
        } else {   	
            Toast.makeText(this, "Image capture failed", Toast.LENGTH_LONG).show();
        }
    }
    
    class ImageIdentifyAsyncTask extends AsyncTask<String, Long, Boolean>{
    	private Dialog progressDialog;
    	private String errorInfo;
    	private Map<String, Object> resultMap;
    	private Bitmap image;
    	
    	public ImageIdentifyAsyncTask(Bitmap image){
    		super();
    		this.image = image;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		
    		resultTextView.setText(null);
    		progressDialog = new Dialog(AngelEyesRMBActivity.this);
    		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		progressDialog.setContentView(R.layout.progress_dialog);
    		progressDialog.setCancelable(false);
    		Button cancelBtn = (Button)progressDialog.findViewById(R.id.cancelBtn);
    		cancelBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
    				progressDialog.dismiss();
    				ImageIdentifyAsyncTask.this.cancel(true);
				}
			});
    		
            RotateAnimation rotation = new RotateAnimation(
          	      0f,
          	      360f,
          	      Animation.RELATIVE_TO_SELF,
          	      0.5f,
          	      Animation.RELATIVE_TO_SELF,
          	      0.5f);
			rotation.setDuration(1300);
			rotation.setInterpolator(new LinearInterpolator());
			rotation.setRepeatMode(Animation.RESTART);
			rotation.setRepeatCount(Animation.INFINITE);
          	progressDialog.findViewById(R.id.activityIndicator).startAnimation(rotation);
    		
    		progressDialog.show();
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... params) {
    		try{
    			resultMap = angelEyes.identifyImage(image);
    		}
    		catch(Exception e){
    			errorInfo = e.getMessage();
    			e.printStackTrace();
    			return false;
    		}
    		
    		return true;
    	}
    	
    	@Override
    	protected void onCancelled() {
    		super.onCancelled();
    	}
    	
    	@Override
    	protected void onPostExecute(Boolean successes){
    		super.onPostExecute(successes);
    		progressDialog.dismiss();
    		
    		if(successes.booleanValue()){
    			showIdentifyResult(resultMap);
    		}
    		else{
    			Toast.makeText(AngelEyesRMBActivity.this, "识别操作失败", Toast.LENGTH_LONG).show();
    			resultTextView.setText(errorInfo);
    			return;
    		}
    	}
    	
    	private void showIdentifyResult(Map<String, Object> result){
    		@SuppressWarnings("unchecked")
			List<String> tagList = (List<String>)result.get("tags");
    	    if (tagList.isEmpty()) {
    	    	resultTextView.setText("无法识别");
    	    }
    	    else{
    	    	resultTextView.setText(tagList.get(0));    
    	    }
    	}
    }
}

