package com.wanghao.downloadcompat;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class MainActivity extends Activity {

	ListView downlist;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		downlist = (ListView)findViewById(R.id.downlist);
		DownloadAdapter adapter = new DownloadAdapter(MainActivity.this);
		downlist.setAdapter(adapter);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
