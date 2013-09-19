package com.hci.prototype.mobilereceipts;

import java.text.NumberFormat;

public final class Util {

	public static String formatPrice(double price){
		NumberFormat formatter = NumberFormat.getCurrencyInstance();
		return formatter.format(price);
	}
}
