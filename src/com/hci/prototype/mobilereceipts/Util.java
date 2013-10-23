package com.hci.prototype.mobilereceipts;

import java.text.NumberFormat;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public final class Util {

	public static String formatPrice(final double price){
		final NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(price);
	}

	public static boolean isIntentAvailable(final Context context, final String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		final List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
