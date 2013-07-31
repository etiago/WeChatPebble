package com.espinhasoftware.wechatpebble.db;

import com.espinhasoftware.wechatpebble.R;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class DatabaseLoadNotifier {

	private NotificationCompat.Builder mBuilder;
    
    private final NotificationManager mNotificationManager;
    
    private Context _context;
    
	public DatabaseLoadNotifier(Context context) {
		mBuilder = new NotificationCompat.Builder(context)
        .setSmallIcon(R.drawable.wechatpebble)
        .setContentTitle("WeChatPebble")
        .setContentText(context.getResources().getText(R.string.notif_started_loading))
        .setAutoCancel(true)
        .setOngoing(true);
        

        this._context = context;
        
        mNotificationManager = (NotificationManager) _context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void changeProgress(int progress, int max) {
        //mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));
        mBuilder.setProgress(max, progress, false);
         
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
	}
	
	public void finish(int progress, int max, int timeout) {
		mBuilder.setContentText(_context.getResources().getText(R.string.notif_finished_loading));
		mBuilder.setContentIntent(PendingIntent.getActivity(_context, 0, new Intent(), 0));
		mBuilder.setOngoing(false);
		
		changeProgress(progress, max);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e) {
				}
				mNotificationManager.cancel(1);
			}
		}).run();
	}
}
