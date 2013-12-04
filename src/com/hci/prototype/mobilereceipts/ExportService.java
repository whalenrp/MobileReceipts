package com.hci.prototype.mobilereceipts;

import android.app.IntentService;
import android.content.Intent;

/*
 * Transmits the current financial state of the application to the server
 * for backup.
 */
public class ExportService extends IntentService {
	
	public ExportService(){
		super("ExportService");
	}

	public ExportService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		
		AbstractExporter exporter = new JsonReceiptExporter(getApplicationContext());
		exporter.connectAndSendData("http://ec2-54-204-238-136.compute-1.amazonaws.com/sync", 8080);
		
//		AbstractExporter mExporter = new ConcreteReceiptExporter(getApplicationContext());
//		mExporter.connectAndSendData("10.0.2.2", 8080);
		
	}

}
