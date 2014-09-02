package com.wanghao.downloadcompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

public class DownloadAdapter extends BaseAdapter{
	
	public static final String	DOWNLOAD_FOLDER_NAME = "DownloadDemo";
	public static final String	DOWNLOAD_FILE_NAME = "HSK.zip";
    public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
    
    private DownloadManager	downloadManager;
    private DownloadManagerPro downloadManagerPro;
    private DownloadChangeObserver downloadObserver;
	
	private String[] urls = {
		"http://img.yingyonghui.com/apk/16457/com.rovio.angrybirdsspace.ads.1332528395706.apk",
		"http://img.yingyonghui.com/apk/15951/com.galapagossoft.trialx2_winter.1328012793227.apk",
		"http://cdn1.down.apk.gfan.com/asdf/Pfiles/2012/3/26/181157_0502c0c3-f9d1-460b-ba1d-a3bad959b1fa.apk",
		"http://static.nduoa.com/apk/258/258681/com.gameloft.android.GAND.GloftAsp6.asphalt6.apk",
		"http://192.168.1.156:8079/hsk/A0A35B5E-13D7-40B9-90E4-BE63A9331EEF.zip",
		"http://192.168.1.156:8079/hsk/A0A35B5E-13D7-40B9-90E4-BE63A9331EEF.zip"};
	
	private String[] titles = {
		"模拟考试一",
		"模拟考试二",
		"模拟考试三",
		"模拟考试四",
		"模拟考试五",
		"模拟考试六"};
	
	private Context context;
	
	public DownloadAdapter(Context context) {
		super();
		this.context = context;
		
		downloadManager = (DownloadManager)context.getSystemService(context.DOWNLOAD_SERVICE);
		downloadManagerPro = new DownloadManagerPro(downloadManager);
	}

	@Override
	public int getCount() {
		return urls.length;
	}

	@Override
	public Object getItem(int position) {
		return urls[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.downlist_item,null);
		}
		ViewHolder viewHolder = new ViewHolder(convertView);
		viewHolder.downloadTitle.setText(titles[position]);
//		final String url = urls[position];
		final MyDownloadHandler handler = new MyDownloadHandler(viewHolder);
		
		viewHolder.downloadStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long downloadId = PreferencesUtils.getLong(context, urls[position], -1);
				if(isNetworkOnline()){
					download(urls[position], handler, downloadId);
				}else{
					Toast.makeText(context, "网络无连接，无法下载！", Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.downloadCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				long downloadId = PreferencesUtils.getLong(context, urls[position], -1);
				downloadManager.remove(downloadId);
			}
		});
		
		return convertView;
	}
	
	private void download(String url, MyDownloadHandler handler,long downloadId) {
		File folder = new File(DOWNLOAD_FOLDER_NAME);
		if(!folder.exists()||!folder.isDirectory()){
			folder.mkdirs();
		}
		
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);//设置文件下载路径
		request.setTitle("试卷压缩包下载");
		request.setDescription("项目快点弄完哈");
//		aviailable above api 11 
//		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		downloadId = downloadManager.enqueue(request);
		
		downloadObserver = new DownloadChangeObserver(handler,downloadId);
		context.getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
		
		/*save download id to preferences*/
		PreferencesUtils.putLong(context, url, downloadId);
		updateView(handler,downloadId);
	}
	

	class DownloadChangeObserver extends ContentObserver{
		
		private Handler handler;
		private long downloadId;

		public DownloadChangeObserver(Handler handler, long downloadId) {
			super(handler);
			this.handler = handler;
			this.downloadId = downloadId;
		}


		@Override
		public void onChange(boolean selfChange) {
			updateView(handler,downloadId);
			
		}

	}
	private void updateView(Handler handler, long downloadId) {
		int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
    	handler.sendMessage(handler.obtainMessage(0,bytesAndStatus[0],bytesAndStatus[1],bytesAndStatus[2]));
	}

	public class MyDownloadHandler extends Handler{

		private ViewHolder viewHolder;
		
		public MyDownloadHandler(ViewHolder viewHolder) {
			this.viewHolder = viewHolder;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			switch(msg.what){
			case 0:
				int status = (Integer)msg.obj;
				if(isDownloading(status)){
					viewHolder.pend();
                    if (msg.arg2 < 0) {
                    	viewHolder.start();
                    } else {
                    	viewHolder.resume(msg);
                    }
				}else{
					viewHolder.cancel(status);
					if (status == DownloadManager.STATUS_FAILED) {

					} else if (status == DownloadManager.STATUS_SUCCESSFUL) {

					} else {

					}
                }
				notifyDataSetChanged();
				break;
			}
		}
	}
	
	public static boolean isDownloading(int downloadManagerStatus){
		return downloadManagerStatus==DownloadManager.STATUS_RUNNING||
				downloadManagerStatus==DownloadManager.STATUS_PAUSED||
				downloadManagerStatus==DownloadManager.STATUS_PENDING;
	}
	
	
    protected boolean isNetworkOnline() {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo nf =  cm.getActiveNetworkInfo();
		if(nf!=null&&nf.isConnectedOrConnecting()){
			return true;
		}
		return false;
	}

}
