package com.abwaters.btce.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.abwaters.btce.BTCE;
import com.abwaters.btce.BTCE.BTCEException;
import com.abwaters.btce.BTCE.CancelOrder;
import com.abwaters.btce.BTCE.Info;
import com.abwaters.btce.BTCE.OrderList;
import com.abwaters.btce.BTCE.OrderListOrder;
import com.abwaters.btce.BTCE.Ticker;
import com.abwaters.btce.BTCE.Trade;
import com.abwaters.btce.BTCE.TradeHistory;
import com.abwaters.btce.BTCE.TradeHistoryOrder;
import com.abwaters.btce.BTCE.TradesDetail;
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

	private static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	@Test
	public void testActiveOrders() throws BTCEException {
		OrderList order_list = btce.getActiveOrders() ;
		if( order_list != null && order_list.info != null )
		for(OrderListOrder order:order_list.info.orders) {
			System.out.println(order) ;
		} else System.out.println("no orders") ;
	}
	
	@Test
	public void testTrade() throws BTCEException {
		// get hit at 1.014
		String trade_type = BTCE.TradeType.SELL ;
		double price = 98.26 ;
		String pair = BTCE.Pairs.BTC_USD ;
		Info info = btce.getInfo() ;
		double amount = 0, funds = 0 ;
		if( trade_type.compareToIgnoreCase(BTCE.TradeType.BUY)==0 ) {
			funds = info.info.funds.usd ;
			amount = round(funds / (price*1.002),4) ;
		}else if( trade_type.compareToIgnoreCase(BTCE.TradeType.SELL)==0 ) {
			amount = info.info.funds.btc ;
		}
		System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount) ;
		Trade trade = btce.trade(pair,trade_type,price,amount) ;
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
		for(TradeHistoryOrder trade:trade_history.info.trades) {
			System.out.println(trade) ;
		}
		System.out.println(trade_history.toString()) ;
	}
	
	@Test
	public void testOrderList() throws BTCEException {
		OrderList order_list = btce.getOrderList() ;
		Assert.assertTrue(order_list!=null) ;
		for(OrderListOrder order:order_list.info.orders) {
			System.out.println(order) ;
		}
	}
	
	@Test
	public void testTicker() throws BTCEException {
		Ticker t = btce.getTicker(BTCE.Pairs.BTC_USD) ;
		System.out.println(t) ;
	}
	
	@Test
	public void testTrades() throws BTCEException {
		TradesDetail[] trades = btce.getTrades(BTCE.Pairs.BTC_USD) ;
		Assert.assertTrue(trades!=null) ;
		System.out.println(Arrays.toString(trades)) ;
	}
	
	private void tradesSummary(TradesDetail[] trades,String type) {
		double min=Double.MAX_VALUE, max=0, avg=0, total=0, wall=0 ;
		double minPrice=Double.MAX_VALUE, maxPrice=0, avgPrice=0, totalPrice = 0, wallPrice = 0 ;
		int cnt = 0 ;
		for(TradesDetail trade:trades) {
			if( trade.trade_type.equalsIgnoreCase(type)) {
				cnt++ ;
				total += trade.amount ;
				totalPrice += trade.price ;
				if( minPrice > trade.price ) {
					min = trade.amount ;
					minPrice = trade.price ;
				}
				if( maxPrice < trade.price ) {
					max = trade.amount ;
					maxPrice = trade.price ;
				}
				if( wall < trade.amount ) {
					wall = trade.amount ;
					wallPrice = trade.price ;
				}
			}
		}
		avg = total/cnt ;
		avgPrice = totalPrice/cnt ;
		String tran_type = type.equalsIgnoreCase("ask")?"buy":"sell" ;
		System.out.println("=========== "+type+" ("+tran_type+") summary ==================") ;
		System.out.println("Min="+min+" Price="+minPrice) ;
		System.out.println("Max="+max+" Price="+maxPrice) ;
		System.out.println("Avg="+avg+" Price="+avgPrice) ;
		System.out.println("Wal="+wall+" Price="+wallPrice) ;
		System.out.println("Ttl="+total+" Price="+totalPrice) ;
		System.out.println() ;
	}
	
	@Test
	public void testMarketDepth() throws BTCEException {
		TradesDetail[] trades = btce.getTrades(BTCE.Pairs.BTC_USD) ;
		Assert.assertTrue(trades!=null) ;
		tradesSummary(trades,"ask") ;
		tradesSummary(trades,"bid") ;
		System.out.println("===================================") ;
		for(TradesDetail trade:trades) {
			System.out.println(trade) ;
		}
	}
}
