package com.abwaters.btce.test;

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
	
	@Before
	public void setUp() throws Exception {
		// Note: Keys below do not have trade or withdraw permissions...only info
		String key = "O07AKCXU-2EFJW1JL-UW03I38C-74JNF7F0-JEXJ83SJ" ;
		String secret = "8c161f8031822a1f9e18cff70a7169ce53ac8ee295943b5a2e0ca2632300f80c" ;
		btce = new BTCE() ;
		btce.setAuthKeys(key, secret) ;
	}

	@Test
	public void testTrade() throws BTCEException {
		//Trade trade = btce.trade(BTCE.Pairs.BTC_USD, BTCE.TradeType.SELL, 97.00, .01) ;
		Trade trade = btce.trade(BTCE.Pairs.BTC_USD, BTCE.TradeType.BUY, 96.00, .01) ;
		Assert.assertTrue(trade!=null) ;
		System.out.println(trade) ;
	}
	
	@Test
	public void testCancelOrder() throws BTCEException {
		int order_id = 31763837 ;
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
