/*
 * This is the package for the BTCE API.  By design, this will contain a single class with the goal of simplifying dependencies and making integration easy.
 * Currently the only dependency is the Google Gson library for JSON serialization {@link http://google-gson.googlecode.com/}.
 * 
 * @author Bryan Waters <bryanw@abwaters.com>
 *
 */
package com.abwaters.btce;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * This is the only class required to connect to the BTC-E bitcoin exchange.  This class allows you to execute API calls on your account to obtain information and create and cancel trades.
 * <p>
 * The BTC-E API is documented at the following location <a href="https://btc-e.com/api/documentation">https://btc-e.com/api/documentation</a>.
 * <p>
 * <pre>
 * BTCE btce = new BTCE() ;
 * btce.setAuthKeys(btce_key,btce_secret) ;
 * Info info = btce.getInfo() ;
 * System.out.println(info.toString()) ; 
 * </pre>
 * 
 */
public class BTCE {
	
	// https://btc-e.com/api/2/btc_usd/ticker
	// https://btc-e.com/api/2/btc_usd/trades
	
	private static final String TICKER_URL = "https://btc-e.com/api/2/" ;
	private static final String API_URL = "https://btc-e.com/tapi" ;
	
	private static long last_request = 0 ;
	private static long request_limit = 1000 ;	// request limit in milliseconds
	private static long nonce = 0, last_nonce = 0 ;
	
	private boolean initialized = false;
	private String secret, key ;
	private Mac mac ;
	private Gson gson ;

	/**
	 * Constructor
	 */
	public BTCE() {
		GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.registerTypeAdapter(TransactionHistoryReturn.class, new TransactionHistoryReturnDeserializer());
		gson_builder.registerTypeAdapter(TradeHistoryReturn.class, new TradeHistoryReturnDeserializer());
		gson_builder.registerTypeAdapter(OrderListReturn.class, new OrderListReturnDeserializer());
		gson = gson_builder.create() ;
		if( nonce == 0 ) nonce = System.currentTimeMillis()/1000 ; 
	}

	/**
	 * Returns the account information. 
	 * 
	 * @return the account info.
	 */
	public Info getInfo() throws BTCEException {
		return gson.fromJson(authrequest("getInfo",null),Info.class) ;
	}
	
	/**
	 * Returns the transaction history for the account.
	 * 
	 * @return the transaction history.
	 */
	public TransactionHistory getTransactionHistory() throws BTCEException {
		return getTransactionHistory(0,0,0,0,null,0,0) ;
	}
	
	/**
	 * Returns the transaction history for the account. 
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 *
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @return the transaction history.
	 */
	public TransactionHistory getTransactionHistory(int from,int count,int from_id,int end_id,String order,long since,long end) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		return gson.fromJson(authrequest("TransHistory",args),TransactionHistory.class) ;
	}

	/**
	 * Returns the trade history for the account.
	 * @return the trade history.
	 */
	public TradeHistory getTradeHistory() throws BTCEException {
		return getTradeHistory(0,0,0,0,null,0,0,null) ;
	}

	/**
	 * Returns the trade history for the account.
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 *
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @param pair the pair to include in the trade history. 
	 * @return the trade history.
	 */
	public TradeHistory getTradeHistory(int from,int count,int from_id,int end_id,String order,long since,long end,String pair) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		if( pair != null && pair.length() > 0 ) args.put("pair", pair) ;
		return gson.fromJson(authrequest("TradeHistory",args),TradeHistory.class) ;
	}
	
	/**
	 * Returns the order list for the account.
	 * 
	 * @return the order list.
	 */
	public OrderList getOrderList() throws BTCEException {
		return getOrderList(0,0,0,0,null,0,0,null,0) ;
	}
	
	/**
	 * Returns the order list for the account.
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 * 
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @param pair the pair to include in the order list.
	 * @param active include only active orders in the order list. 
	 */
	public OrderList getOrderList(int from,int count,int from_id,int end_id,String order,long since,long end,String pair,int active) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		if( pair != null && pair.length() > 0 ) args.put("pair", pair) ;
		if( active > 0 ) args.put("active", Long.toString(active)) ;
		return gson.fromJson(authrequest("OrderList",args),OrderList.class) ;
	}
	
	/**
	 * Execute a trade for the specified currency pair.
	 * 
	 * @param pair the currency pair to trade.  This is specified as <curr1>_<curr2>, example: "BTC_USD"
	 * @param type the type of transaction.  Can be "buy" or "sell".
	 * @param rate the rate to pay for the transaction in <curr2>.
	 * @param amount the quantity of <curr1> to buy.
	 * @return the trade results.
	 */
	public Trade executeTrade(String pair,String type,double rate,double amount) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("pair", pair) ;
		args.put("type", type) ;
		args.put("rate", Double.toString(rate)) ;
		args.put("amount", Double.toString(amount)) ;
		return gson.fromJson(authrequest("Trade",args),Trade.class) ;
	}
	
	/**
	 * Cancel the specified order.
	 * 
	 * @param order_id the id of the order to cancel.
	 */
	public CancelOrder cancelOrder(int order_id) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("order_id", Integer.toString(order_id)) ;
		return gson.fromJson(authrequest("CancelTrade",args),CancelOrder.class) ;
	}
	
	/**
	 * Sets the account API keys to use for calling methods that require access to a BTC-E account.
	 * 
	 * @param key the key obtained from Profile->API Keys in your BTC-E account.
	 * @param secret the secret obtained from Profile->API Keys in your BTC-E account.
	 */
	public void setAuthKeys(String key,String secret) throws BTCEException {
		this.key = key ;
		this.secret = secret ;
		SecretKeySpec keyspec = null ;
		try {
			keyspec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA512") ;
		} catch (UnsupportedEncodingException uee) {
			throw new BTCEException("HMAC-SHA512 doesn't seem to be installed",uee) ;
		}

		try {
			mac = Mac.getInstance("HmacSHA512") ;
		} catch (NoSuchAlgorithmException nsae) {
			throw new BTCEException("HMAC-SHA512 doesn't seem to be installed",nsae) ;
		}

		try {
			mac.init(keyspec) ;
		} catch (InvalidKeyException ike) {
			throw new BTCEException("Invalid key for signing request",ike) ;
		}
		initialized = true ;
	}

	private final void prepCall() {
		while(nonce==last_nonce) nonce++ ;
		long elapsed = System.currentTimeMillis()-last_request ;
		if( elapsed < request_limit ) {
			try {
				Thread.currentThread().sleep(request_limit-elapsed) ;
			} catch (InterruptedException e) {
				
			}
		}
		last_request = System.currentTimeMillis() ;
	}
	
	private final String authrequest(String method, Map<String,String> args) throws BTCEException {
		if( !initialized ) throw new BTCEException("BTCE not initialized.") ;
		
		// prep the call
		prepCall() ;

		// add method and nonce to args
		if (args == null) args = new HashMap<String,String>() ;
		args.put("method", method) ;
		args.put("nonce",Long.toString(nonce)) ;
		last_nonce = nonce ;
		
		// create url form encoded post data
		String postData = "" ;
		for (Iterator<String> iter = args.keySet().iterator(); iter.hasNext();) {
			String arg = iter.next() ;
			if (postData.length() > 0) postData += "&" ;
			postData += arg + "=" + URLEncoder.encode(args.get(arg)) ;
		}
		
		// create connection
		URLConnection conn = null ;
		StringBuffer response = new StringBuffer() ;
		try {
			URL url = new URL(API_URL);
			conn = url.openConnection() ;
			conn.setUseCaches(false) ;
			conn.setDoOutput(true) ;
			conn.setRequestProperty("Key",key) ;
			conn.setRequestProperty("Sign",toHex(mac.doFinal(postData.getBytes("UTF-8")))) ;
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded") ;
		
			// write post data
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(postData) ;
			out.close() ;
	
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null ;
			while ((line = in.readLine()) != null)
				response.append(line) ;
			in.close() ;
		} catch (MalformedURLException e) {
			throw new BTCEException("Internal error.",e) ;
		} catch (IOException e) {
			throw new BTCEException("Error connecting to BTC-E.",e) ;
		}
		return response.toString() ;
	}
	
	private String toHex(byte[] b) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1,b));
	}
	
	/**
	 * Displays the amounts of various currencies associated with an account or an order.
	 */
	public class Funds {
		public double usd, btc, ltc, nmc, rur, eur, nvc, trc, ppc, ftc, cnc ;

		@Override
		public String toString() {
			return "[usd=" + usd + ", btc=" + btc + ", ltc=" + ltc + ", nmc="
					+ nmc + ", rur=" + rur + ", eur=" + eur + ", nvc=" + nvc
					+ ", trc=" + trc + ", ppc=" + ppc + ", ftc=" + ftc + ", cnc="
					+ cnc + "]";
		}
	}
	
	/**
	 * Info class.
	 */
	public class Info extends Results {
		@Override
		public String toString() {
			return "Info [success=" + success + ", error=" + error + ", return=" + info + "]";
		}

		@SerializedName("return")
		public InfoReturn info ;
	}	

	/**
	 * InfoReturn class.
	 */
	public class InfoReturn {
		public Funds funds ;
		public Rights rights ;
		public int transaction_count ;
		public int open_orders ;
		public long server_time ;
		@Override
		public String toString() {
			return "[funds=" + funds + ", rights=" + rights
					+ ", transaction_count=" + transaction_count + ", open_orders="
					+ open_orders + ", server_time=" + server_time + "]";
		}
	}

	/**
	 * Results class.
	 */
	public class Results {
		public int success ;
		public String error = "" ;
		
		@Override
		public String toString() {
			return "[success=" + success + ", error=" + error + "]";
		}
	}

	/**
	 * Rights class.
	 */
	public class Rights {
		public int info, trade, withdraw ;

		@Override
		public String toString() {
			return "[info=" + info + ", trade=" + trade + ", withdraw="
					+ withdraw + "]";
		}
	}

	/**
	 * TransactionHistory class.
	 */
	public class TransactionHistory extends Results {
		@SerializedName("return")
		public TransactionHistoryReturn info ;
		
		@Override
		public String toString() {
			return "TransactionHistory [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}

	/**
	 * TransactionHistoryReturn class.
	 */
	public class TransactionHistoryReturn {
		public TransactionHistoryOrder[] orders ;

		@Override
		public String toString() {
			return "[orders="+ Arrays.toString(orders) + "]";
		}
	}
	
	/**
	 * TransactionHistoryOrder class.
	 */
	public class TransactionHistoryOrder {
		public int order_id ;
		public TransactionHistoryOrderDetails order_details ;
		
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", order_details="+ order_details + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class TransactionHistoryOrderDetails {
		public int type ;
		public double amount ;
		public String currency, desc ;
		public int status, timestamp ;
		
		@Override
		public String toString() {
			return "[type=" + type + ", amount="
					+ amount + ", currency=" + currency + ", desc=" + desc
					+ ", status=" + status + ", timestamp=" + timestamp + "]";
		}
	}

	/**
	 * @param args
	 */
	private class TransactionHistoryReturnDeserializer implements JsonDeserializer<TransactionHistoryReturn> {
		  public TransactionHistoryReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  TransactionHistoryReturn thr = new TransactionHistoryReturn() ;
			  List<TransactionHistoryOrder> orders = new ArrayList<TransactionHistoryOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  TransactionHistoryOrder order = new TransactionHistoryOrder() ;
					  order.order_id = Integer.parseInt(jsonOrder.getKey()) ;
					  order.order_details = context.deserialize(jsonOrder.getValue(),TransactionHistoryOrderDetails.class) ;
					  orders.add(order) ;
				  }
			  }
			  thr.orders = orders.toArray(new TransactionHistoryOrder[0]) ;
			  return thr ;
		  }
	}
	
	/**
	 * @param args
	 */
	public class TradeHistory extends Results {
		@Override
		public String toString() {
			return "TradeHistory [success=" + success + ", error=" + error + ", return=" + info + "]";
		}

		@SerializedName("return")
		public TradeHistoryReturn info ;
	}

	/**
	 * @param args
	 */
	public class TradeHistoryReturn {
		public TradeHistoryOrder[] orders ;

		@Override
		public String toString() {
			return "[orders="+ Arrays.toString(orders) + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class TradeHistoryOrder {
		public int order_id ;
		public TradeHistoryOrderDetails order_details ;
		
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", order_details="+ order_details + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class TradeHistoryOrderDetails {
		public String pair, type ;
		public double amount, rate ;
		public int order_id ;
		public int is_your_order ;
		public int timestamp ;
		@Override
		public String toString() {
			return "[pair=" + pair + ", type=" + type
					+ ", amount=" + amount + ", rate=" + rate + ", order_id="
					+ order_id + ", is_your_order=" + is_your_order
					+ ", timestamp=" + timestamp + "]";
		}
	}

	private class TradeHistoryReturnDeserializer implements JsonDeserializer<TradeHistoryReturn> {
		  public TradeHistoryReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  TradeHistoryReturn thr = new TradeHistoryReturn() ;
			  List<TradeHistoryOrder> orders = new ArrayList<TradeHistoryOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  TradeHistoryOrder order = new TradeHistoryOrder() ;
					  order.order_id = Integer.parseInt(jsonOrder.getKey()) ;
					  order.order_details = context.deserialize(jsonOrder.getValue(),TradeHistoryOrderDetails.class) ;
					  orders.add(order) ;
				  }
			  }
			  thr.orders = orders.toArray(new TradeHistoryOrder[0]) ;
			  return thr ;
		  }
	}
	
	/**
	 * @param args
	 */
	public class OrderList extends Results {
		@SerializedName("return")
		public OrderListReturn info ;
		
		@Override
		public String toString() {
			 return "OrderList [success=" + success + ", error=" + error + ", return=" + info + "]";			
		}
	}
	
	/**
	 * @param args
	 */
	public class OrderListReturn {
		public OrderListOrder[] orders ;

		@Override
		public String toString() {
			return "[orders="+ Arrays.toString(orders) + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class OrderListOrder {
		public int order_id ;
		public OrderListOrderDetails order_details ;
		
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", order_details="	+ order_details + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class OrderListOrderDetails {
		public String pair, type ;
		public double amount, rate ;
		public int status, timestamp ;
		
		@Override
		public String toString() {
			return "[pair=" + pair + ", type=" + type
					+ ", amount=" + amount + ", rate=" + rate + ", status="
					+ status + ", timestamp=" + timestamp + "]";
		}
	}
	
	/**
	 * @param args
	 */
	private class OrderListReturnDeserializer implements JsonDeserializer<OrderListReturn> {
		  public OrderListReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  OrderListReturn olr = new OrderListReturn() ;
			  List<OrderListOrder> orders = new ArrayList<OrderListOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  OrderListOrder order = new OrderListOrder() ;
					  order.order_id = Integer.parseInt(jsonOrder.getKey()) ;
					  order.order_details = context.deserialize(jsonOrder.getValue(),OrderListOrderDetails.class) ;
					  orders.add(order) ;
				  }
			  }
			  olr.orders = orders.toArray(new OrderListOrder[0]) ;
			  return olr ;
		  }
	}
	
	/**
	 * @param args
	 */
	public class Trade extends Results {
		@SerializedName("return")
		public TradeReturn info ;

		@Override
		public String toString() {
			return "Trade [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class TradeReturn {
		public double received ;
		public double remains ;
		public int order_id ;
		public Funds funds ;
		
		@Override
		public String toString() {
			return "[received=" + received + ", remains=" + remains	+ ", order_id=" + order_id + ", funds=" + funds + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class CancelOrder extends Results {
		@SerializedName("return")
		public CancelOrderReturn info ;

		@Override
		public String toString() {
			return "CancelOrder [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class CancelOrderReturn {
		public int order_id ;
		public Funds funds ;
		
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", funds="
					+ funds + "]";
		}
	}
	
	/**
	 * @param args
	 */
	public class BTCEException extends Exception {
		
		private static final long serialVersionUID = 1L;
		
		public BTCEException(String msg) {
			super(msg) ;
		}
		
		public BTCEException(String msg, Throwable e) {
			super(msg,e) ;
		}
	}
}
