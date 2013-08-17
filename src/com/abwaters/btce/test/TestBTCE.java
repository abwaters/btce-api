package com.abwaters.btce.test;

import java.util.Properties;

import com.abwaters.btce.BTCE;
import com.abwaters.btce.BTCE.Info;
import com.abwaters.btce.BTCE.OrderList;
import com.abwaters.btce.BTCE.TradeHistory;
import com.abwaters.btce.BTCE.TransactionHistory;

public class TestBTCE {
	public static void main(String[] args) throws Exception {
		String key = "O07AKCXU-2EFJW1JL-UW03I38C-74JNF7F0-JEXJ83SJ" ;
		String secret = "8c161f8031822a1f9e18cff70a7169ce53ac8ee295943b5a2e0ca2632300f80c" ;
		BTCE btce = new BTCE() ;
		btce.setAuthKeys(key,secret) ;
		Info info = btce.getInfo() ;
		System.out.println(info.toString()) ;
		
		TransactionHistory transaction_history = btce.getTransactionHistory() ;
		System.out.println(transaction_history.toString()) ;
		
		TradeHistory trade_history = btce.getTradeHistory() ;
		System.out.println(trade_history.toString()) ;
		
		OrderList order_list = btce.getOrderList() ;
		System.out.println(order_list.toString()) ;
		
		
	}
}
