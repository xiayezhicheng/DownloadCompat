package com.wanghao.downloadcompat;

import java.text.DecimalFormat;

import android.app.DownloadManager;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewHolder {

	public ProgressBar            downloadProgress;
	public TextView               downloadTitle;
	public TextView               downloadSize;
	public TextView               downloadPrecent;
	public Button                 downloadStart;
	public Button                 downloadCancel;
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");
	public static final int MB_2_BYTE	=1024*1024;
	public static final int KB_2_BYTE	=1024;
	
    public ViewHolder(View parentView) {
		if (parentView != null) {
			downloadTitle = (TextView) parentView.findViewById(R.id.paper_name);
			downloadPrecent = (TextView) parentView.findViewById(R.id.download_precent);
			downloadSize = (TextView) parentView.findViewById(R.id.download_size);
			downloadProgress = (ProgressBar) parentView.findViewById(R.id.download_progress);
			downloadStart = (Button) parentView.findViewById(R.id.download_start);
			downloadCancel = (Button) parentView.findViewById(R.id.download_cancel);
		}
	}
    
    public void pend(){
    	downloadProgress.setVisibility(View.VISIBLE);
		downloadProgress.setMax(0);
		downloadProgress.setProgress(0);
		downloadStart.setVisibility(View.GONE);
		downloadSize.setVisibility(View.VISIBLE);
		downloadPrecent.setVisibility(View.VISIBLE);
		downloadCancel.setVisibility(View.VISIBLE);
    }
    
    public void start(){
    	downloadProgress.setIndeterminate(true);
    	downloadPrecent.setText("0%");
    	downloadSize.setText("0M/0M");
    }

    public void resume(Message msg){
    	downloadProgress.setIndeterminate(false);
    	downloadProgress.setMax(msg.arg2);
    	downloadProgress.setProgress(msg.arg1);
    	downloadPrecent.setText(getNotiPercent(msg.arg1, msg.arg2));
    	downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
    }
    
    public void cancel(int status){
    	downloadProgress.setVisibility(View.GONE);
		downloadProgress.setMax(0);
		downloadProgress.setProgress(0);
		downloadStart.setVisibility(View.VISIBLE);
		downloadSize.setVisibility(View.GONE);
		downloadPrecent.setVisibility(View.GONE);
		downloadCancel.setVisibility(View.GONE);

        if (status == DownloadManager.STATUS_FAILED) {
        	downloadStart.setText("下载失败，点击重新下载");
        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
        	downloadStart.setText("下载成功,点击重新下载");
        } else {
        	downloadStart.setText("点击下载");
        }
    }
	
	public static CharSequence getAppSize(long size){
		if(size<=0){
			return "0M";
		}
        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
     }
	
	public static String getNotiPercent(long progress,long max){
		int rate = 0;
		if(progress<=0||max<=0){
			rate=0;
		}else if(progress>max){
			rate=100;
		}else{
			rate = (int)((double)progress/max*100);
		}
		return new StringBuilder(16).append(rate).append("%").toString();
   }
}
