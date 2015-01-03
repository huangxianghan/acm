package com.xianghan.acm.websocket;

import java.net.URI;
import java.util.Map;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import android.util.Log;

/**
 * 
 * @author 黄香翰
 *
 */
public class AcmWebsocketClient extends WebSocketClient {

	final static boolean DEBUG = true;
	final static String TAG = AcmWebsocketClient.class.getName();
	
	AcmMessageCallback mWsCallback = sDefCallback;
	
	public interface AcmMessageCallback{
		public void onOpen(short httpStatus,String httpStatusMessage);
		public void onMessage(String msg);
		public void onError(Exception ex);
		public void onClose(int code,String reason);
	}
	
	public AcmWebsocketClient(URI serverURI) {
		super(serverURI);
	}

	public AcmWebsocketClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}
	
	public AcmWebsocketClient(URI serverUri, Draft draft, Map<String, String> headers, int connecttimeout){
		super(serverUri, draft,headers,connecttimeout);
	}

	@Override
	public void onClose(int code, String msg, boolean bln) {
		mWsCallback.onClose(code, msg);
	}

	@Override
	public void onError(Exception ex) {
		mWsCallback.onError(ex);
	}

	@Override
	public void onMessage(String msg) {
		mWsCallback.onMessage(msg);
	}

	@Override
	public void onOpen(ServerHandshake sh) {
		mWsCallback.onOpen(sh.getHttpStatus(), sh.getHttpStatusMessage());
	}
	
	public void setMessageCallback(AcmMessageCallback messageCallback){
		if(messageCallback == null)
			mWsCallback = sDefCallback;
		else
			mWsCallback = messageCallback;
	}
	
	final static AcmMessageCallback sDefCallback = new AcmMessageCallback() {
		
		@Override
		public void onOpen(short httpStatus, String httpStatusMessage) {
			if(DEBUG) Log.d(TAG,"httpStatus: "+httpStatus 
					+ " message: "+httpStatusMessage);
		}
		
		@Override
		public void onMessage(String msg) {
			if(DEBUG) Log.d(TAG,msg);
		}
		
		@Override
		public void onError(Exception ex) {
			if(DEBUG) Log.d(TAG,ex.getMessage());
		}
		
		@Override
		public void onClose(int code, String reason) {
			if(DEBUG) Log.d(TAG,"webscoket close code:"
					+code + " reason:" + reason );
		}
	};

}
