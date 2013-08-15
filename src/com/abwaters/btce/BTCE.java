package com.abwaters.btce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author bryanw
 *
 */
public class BTCE {
	
	private static final String API_URL = "https://btc-e.com/tapi" ;
	
	private boolean initialized = false;
	private long nonce ;
	private String secret, key ;
	private Mac mac ;
	private Gson gson ;

	public BTCE() {
	}

	public BTCE(Properties p) {
		this();
		initializeProperties(p);
	}

	public Info getInfo() throws Exception {
		return gson.fromJson(request("getInfo",null),Info.class) ;
	}
	
	public String getTransactionHistory() throws Exception {
		return getTransactionHistory(0,0,0,0,null,0,0) ;
	}
	
	public String getTransactionHistory(int from,int count,int from_id,int end_id,String order,long since,long end) throws Exception {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		return request("TransHistory",args) ;
	}

	public String getTradeHistory() throws Exception {
		return getTradeHistory(0,0,0,0,null,0,0,null) ;
	}

	public String getTradeHistory(int from,int count,int from_id,int end_id,String order,long since,long end,String pair) throws Exception {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		if( pair != null && pair.length() > 0 ) args.put("pair", pair) ;
		return request("TradeHistory",args) ;
	}
	
	public String getOrderList() throws Exception {
		return getOrderList(0,0,0,0,null,0,0,null,0) ;
	}
	
	public String getOrderList(int from,int count,int from_id,int end_id,String order,long since,long end,String pair,int active) throws Exception {
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
		return request("OrderList",args) ;
	}
	
	public String executeTrade(String pair,String type,double rate,double amount) throws Exception {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("pair", pair) ;
		args.put("type", type) ;
		args.put("rate", Double.toString(rate)) ;
		args.put("amount", Double.toString(amount)) ;
		return request("Trade",args) ;
	}
	
	public String cancelTrade(int order_id) throws Exception {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("order_id", Integer.toString(order_id)) ;
		return request("CancelTrade",args) ;
	}
	
	public void initializeProperties(Properties p) {
		gson = new Gson() ;
		key = p.getProperty("btce.key") ;
		secret = p.getProperty("btce.secret") ;
		nonce = (System.currentTimeMillis() / 1000) ;
		
		SecretKeySpec keyspec = null ;
		try {
			keyspec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA512") ;
		} catch (UnsupportedEncodingException uee) {
			System.err.println("Unsupported encoding exception: "+ uee.toString()) ;
		}

		try {
			mac = Mac.getInstance("HmacSHA512") ;
		} catch (NoSuchAlgorithmException nsae) {
			System.err.println("No such algorithm exception: "
					+ nsae.toString());
		}

		try {
			mac.init(keyspec) ;
		} catch (InvalidKeyException ike) {
			System.err.println("Invalid key exception: " + ike.toString());
		}
		
		initialized = true ;
	}
	
	private final String request(String method, Map<String,String> args) throws Exception {
		if( !initialized ) return null ;
		
		if (args == null) args = new HashMap<String,String>() ;

		args.put("method", method) ;
		args.put("nonce",""+ ++nonce) ;
		
		String postData = "" ;
		for (Iterator<String> iter = args.keySet().iterator(); iter.hasNext();) {
			String arg = iter.next() ;
			if (postData.length() > 0) postData += "&" ;
			postData += arg + "=" + URLEncoder.encode(args.get(arg)) ;
		}
		
		URL url = new URL(API_URL);
		URLConnection conn = url.openConnection() ;
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
		StringBuffer response = new StringBuffer() ;
		String line = null ;
		while ((line = in.readLine()) != null)
			response.append(line) ;
		in.close() ;
		return response.toString() ;
	}
	
	public String toHex(byte[] b) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1,b));
	}
	
	public class Funds {
		public double usd, btc, ltc, nmc, rur, eur, nvc, trc, ppc, ftc, cnc ;

		@Override
		public String toString() {
			return "Funds [usd=" + usd + ", btc=" + btc + ", ltc=" + ltc + ", nmc="
					+ nmc + ", rur=" + rur + ", eur=" + eur + ", nvc=" + nvc
					+ ", trc=" + trc + ", ppc=" + ppc + ", ftc=" + ftc + ", cnc="
					+ cnc + "]";
		}
	}
	
	public class Info extends Results {
		@Override
		public String toString() {
			return "Info [info=" + info + "]";
		}

		@SerializedName("return")
		public InfoReturn info ;
	}	

	public class InfoReturn {
		public Funds funds ;
		public Rights rights ;
		public int transaction_count ;
		public int open_orders ;
		public long server_time ;
		@Override
		public String toString() {
			return "InfoReturn [funds=" + funds + ", rights=" + rights
					+ ", transaction_count=" + transaction_count + ", open_orders="
					+ open_orders + ", server_time=" + server_time + "]";
		}
	}

	public class Results {
		public int success ;
		public String error ;
		
		@Override
		public String toString() {
			return "Results [success=" + success + ", error=" + error + "]";
		}
	}

	public class Rights {
		public int info, trade, withdraw ;

		@Override
		public String toString() {
			return "Rights [info=" + info + ", trade=" + trade + ", withdraw="
					+ withdraw + "]";
		}
	}

	public class TransactionHistory extends Results {
		@Override
		public String toString() {
			return "Info [info=" + info + "]";
		}

		@SerializedName("return")
		public TransactionHistoryReturn info ;
	}

	public class TransactionHistoryReturn extends Results {
		@Override
		public String toString() {
			return "Info [info=" + info + "]";
		}

		@SerializedName("return")
		public TransactionHistoryReturn info ;
	}
}
