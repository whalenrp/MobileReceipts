package com.hci.prototype.mobilereceipts.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.test.mock.MockContext;
import android.test.mock.MockPackageManager;

import com.hci.prototype.mobilereceipts.Util;

public class UtilTest extends TestCase {
	
	// Stubs for Context and PackageManager objects
	private class MyMockContext extends MockContext{
		@Override
		public PackageManager getPackageManager(){
			return new MyMockPackageManager();
		}
	}
	
	private class MyMockPackageManager extends MockPackageManager{
		@Override
		public List<ResolveInfo> queryIntentActivities(Intent i, int flags){
			List<ResolveInfo> mList = new ArrayList<ResolveInfo>();
			mList.add(null);
			return mList;
		}
	}
	
	// Required for Android testing framework
	public UtilTest(){
		super();
	}
	
	@Override
	public void setUp(){}
	
	@Override
	public void tearDown(){}
	
	
	/*
	 * Test the FormatPrice function
	 */
	public void testFormatPrice(){
		double cost[] = new double[]{2.0,5.0,20.00,2.5};
		String output[] = new String[]{"$2.00", "$5.00", "$20.00", "$2.50"};
		
		for (int i=0; i < cost.length; ++i){
			assertEquals( Util.formatPrice(cost[i]), output[i] );
		}
	}
	
	/*
	 * Test the isIntentAvailable function
	 */
	public void testIsIntentAvailable(){
		MockContext ctxt = new MyMockContext();
		assertTrue(Util.isIntentAvailable(ctxt, null));
	}
}
