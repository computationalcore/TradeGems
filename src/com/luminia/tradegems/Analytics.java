package com.luminia.tradegems;

public class Analytics {
	// Events Categories
	public static class Category{
		public final static String AD = "AD";
		public final static String GAME = "GAME";
	}
	
	// Events Actions
	public static class Actions{
		public final static String CLICK = "Click";
		public final static String ADMOB_ACTION = "Admob Action";
		public final static String APP_VERSION_REPORT = "App Version Report";
	}
	
	// Event Labels
	public static class Labels{
		public final static String AD_DISMISSED = "Ad dismissed";
		public final static String LEAVING_APP = "Leaving app";
		public final static String AD_RECEIVED = "Ad received";
		public final static String AD_FAILED = "Ad failed";
		public final static String CONECTION_ERROR = "Server Error";
		public final static String CONNECTION_OK = "Server Ok";
	}
}
