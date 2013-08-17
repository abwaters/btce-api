package com.abwaters.btce.test;

import org.junit.Before;
import org.junit.Test;

import com.abwaters.btce.BTCE;
import com.abwaters.btce.BTCE.BTCEException;
import com.abwaters.btce.BTCE.Info;
import com.abwaters.btce.BTCE.OrderList;
import com.abwaters.btce.BTCE.TradeHistory;
import com.abwaters.btce.BTCE.TransactionHistory;

public class BTCE_Test {

	private BTCE btce ;
	
	@Before
	public void setUp() throws Exception {
		String key = "O07AKCXU-2EFJW1JL-UW03I38C-74JNF7F0-JEXJ83SJ" ;
		String secret = "8c161f8031822a1f9e18cff70a7169ce53ac8ee295943b5a2e0ca2632300f80c" ;
		btce = new BTCE() ;
		btce.setAuthKeys(key, secret) ;
	}

	@Test
	public void testInfo() throws BTCEException {
		Info info = btce.getInfo() ;
		assert(info!=null&&info.success>0) ;
		System.out.println(info) ;
	}

	@Test
	public void testTransactionHistory() throws BTCEException {
		TransactionHistory transaction_history = btce.getTransactionHistory() ;
		assert(transaction_history!=null&&transaction_history.success>0) ;
		System.out.println(transaction_history.toString()) ;
	}
	
	@Test
	public void testTradeHistory() throws BTCEException {
		TradeHistory trade_history = btce.getTradeHistory() ;
		assert(trade_history!=null&&trade_history.success>0) ;
		System.out.println(trade_history.toString()) ;
	}
	
	@Test
	public void testOrderList() throws BTCEException {
		OrderList order_list = btce.getOrderList() ;
		assert(order_list!=null&&order_list.success>0) ;
		System.out.println(order_list.toString()) ;
	}
}
