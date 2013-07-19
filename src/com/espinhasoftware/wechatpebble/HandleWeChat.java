package com.espinhasoftware.wechatpebble;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class HandleWeChat extends AccessibilityService {
    @Override
    public void onServiceConnected()
    {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.notificationTimeout = 100;
        info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
        info.packageNames = new String[]{"com.tencent.mm"};;
        
        setServiceInfo(info);

    }
	
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event)
    {
//        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//	      if (event.getParcelableData() instanceof Notification) {
	          Notification notification = (Notification) event.getParcelableData();
	
//	          Log.d("ticker: " , notification.tickerText+"");
//	          Log.d("icon: " , notification.icon+"");
//	          Log.d("largeIcon: " , notification.largeIcon+"");
//	          Log.d("Type", event.getParcelableData()+"");
	          String originalMsg = notification.tickerText.toString();
	          
			  HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			  format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			  format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
			  format.setVCharType(HanyuPinyinVCharType.WITH_V);
				
			  try {
				  originalMsg = PinyinHelper.toHanyuPinyinString(originalMsg, format , "");
			  } catch (BadHanyuPinyinOutputFormatCombination e) {
				  // TODO Auto-generated catch block
				  Log.e("Pinyin", "Failed to convert pinyin");
			  }
	  		
	          sendAlertToPebble(originalMsg);
//	      }
	
//	      Log.d("notification: " , event.getText() + "");
//        }
    }

    public void sendAlertToPebble(String alert) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map<String, String> data = new HashMap<String, String>();
        data.put("title", "WeChat");
        data.put("body", alert);
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "MyAndroidApp");
        i.putExtra("notificationData", notificationData);

        //Log.d("Alert", "About to send a modal alert to Pebble: " + notificationData);
        sendBroadcast(i);
    }
    
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub

	}

}
