package com.hci.prototype.mobilereceipts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/*
 * This class defines how to connect to the server and send the contents of the 
 * database query over the networking using TCP. 
 */
public abstract class AbstractExporter
{

	private Context mContext;

	public AbstractExporter(Context ctxt)
	{
		mContext = ctxt;
	}

	/*
	 * Implements the template pattern by connecting to the given server and calling the derived class' particular
	 * method of formatting
	 */
	public final void connectAndSendData(String dstName, int dstPort)
	{
		// dstPort will be unused for now
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(dstName);
		byte[] data = formatData();
		
		if (data == null)
		{
//			Toast.makeText(mContext, "Error sending data", Toast.LENGTH_SHORT).show();
		}
		else
		{
			ByteArrayEntity entity = new ByteArrayEntity(formatData());
			post.setEntity(entity);
			
			HttpResponse response;
			try
			{
				response = httpClient.execute(post);
				InputStream is = response.getEntity().getContent();
				
				String respString = getStringFromInputStream(is);
				is.close();
				
				Log.d("Upload", respString);
//				Toast.makeText(mContext, respString, Toast.LENGTH_SHORT).show();
			}
			catch (IOException e)
			{
				Log.e("AbstractExporter", "Error communicating with server", e);
//				Toast.makeText(mContext, "Error communicating with server", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private static String getStringFromInputStream(InputStream is)
	{
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(is).useDelimiter("\\A");
		String returnString = null;
		if (scanner.hasNext())
		{
			returnString = scanner.next();
		}
		scanner.close();
		
		return returnString;
	}

	/*
	 * Abstract function usedy by derived classes to retrieve and format data for backup transmission over the network.
	 */
	protected abstract byte[] formatData();
}
