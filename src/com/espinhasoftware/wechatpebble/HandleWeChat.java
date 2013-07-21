/**
 * This file is part of WeChat Pebble (http://github.com/wechatpebble/) 
 * and distributed under GNU GENERAL PUBLIC LICENSE (GPL).
 * 
 * pinyin4j is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * WeChat Pebble is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with WeChat Pebble. If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * @author Tiago Espinha (tiago@espinha.pt)
 *
 */
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
		  Notification notification = (Notification) event.getParcelableData();
		
		  String originalMsg = notification.tickerText.toString();
		  
		  HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		  format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		  format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
		  format.setVCharType(HanyuPinyinVCharType.WITH_V);
			
		  try {
			  // I know this is deprecated but there's no viable alternative...
			  originalMsg = PinyinHelper.toHanyuPinyinString(originalMsg, format , "");
		  } catch (BadHanyuPinyinOutputFormatCombination e) {
			  Log.e("Pinyin", "Failed to convert pinyin");
		  }
		
		  sendAlertToPebble(originalMsg);
    }

    /**
     * Sends alerts to the Pebble watch, as per the Pebble app's intents
     * @param alert Alert which to send to the watch.
     */
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

        sendBroadcast(i);
    }
    
	@Override
	public void onInterrupt() {
		
	}

}
