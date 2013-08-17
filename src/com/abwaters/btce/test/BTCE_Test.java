package com.abwaters.btce.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.abwaters.btce.BTCE;
import com.abwaters.btce.BTCE.BTCEException;
import com.abwaters.btce.BTCE.CancelOrder;
import com.abwaters.btce.BTCE.Info;
import com.abwaters.btce.BTCE.OrderList;
import com.abwaters.btce.BTCE.Trade;
import com.abwaters.btce.BTCE.TradeHistory;
import com.abwaters.btce.BTCE.TransactionHistory;

public class BTCE_Test {

	private BTCE btce ;
	
	private static Properties load(File pfile) throws Exception {
		FileInputStream pfs = new FileInputStream(pfile.getAbsoluteFile()) ;
		Properties properties = new Properties() ;
		properties.load(pfs) ;
		return properties ;
	}
	
	@Before
	public void setUp() throws Exception {
		// Note: Keys below do not have trade or withdraw permissions...only info
		String userdir = System.getProperty("user.dir") ;
		Properties p = load(new File(userdir,"config.properties")) ;
		String key = p.getProperty("btce.key") ;
		String secret = p.getProperty("btce.secret") ;
		int request_limit = Integer.parseInt(p.getProperty("btce.request_limit")) ;
		int auth_request_limit = Integer.parseInt(p.getProperty("btce.auth_request_limit")) ;
		btce = new BTCE() ;
		btce.setAuthKeys(key, secret) ;
		btce.setAuthRequestLimit(auth_request_limit) ;
		btce.setRequestLimit(request_limit) ;
	}

	@Test
	public void testTrade() throws BTCEException {
		//Trade trade = btce.trade(BTCE.Pairs.BTC_USD, BTCE.TradeType.SELL, 97.00, .01) ;
		Trade trade = btce.trade(BTCE.Pairs.BTC_USD, BTCE.TradeType.BUY, 96.50, .01) ;
		Assert.assertTrue(trade!=null) ;
		System.out.println(trade) ;
	}
	
	@Test
	public void testCancelOrder() throws BTCEException {
		int order_id = 31770453 ;
		CancelOrder cancel_order = btce.cancelOrder(order_id) ;
		Assert.assertTrue(cancel_order!=null) ;
		System.out.println(cancel_order) ;
	}

	@Test
	public void testInfo() throws BTCEException {
		Info info = btce.getInfo() ;
		Assert.assertTrue(info!=null) ;
		System.out.println(info) ;
	}

	@Test
	public void testTransactionHistory() throws BTCEException {
		TransactionHistory transaction_history = btce.getTransactionHistory() ;
		Assert.assertTrue(transaction_history!=null) ;
		System.out.println(transaction_history.toString()) ;
	}
	
	@Test
	public void testTradeHistory() throws BTCEException {
		TradeHistory trade_history = btce.getTradeHistory() ;
		Assert.assertTrue(trade_history!=null) ;
		System.out.println(trade_history.toString()) ;
	}
	
	@Test
	public void testOrderList() throws BTCEException {
		OrderList order_list = btce.getOrderList() ;
		Assert.assertTrue(order_list!=null) ;
		System.out.println(order_list.toString()) ;
	}
}
