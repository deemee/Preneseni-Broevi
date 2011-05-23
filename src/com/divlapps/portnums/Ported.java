package com.divlapps.portnums;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.database.Cursor;

public class Ported extends Activity {
	Button okBut, callMe, smsMe;
    AutoCompleteTextView number;
    String fixedNumber, retNum, aek, textNum, callTry, smsTry, chNum, newText, checkedNum, displayText, tmoText, oneText, vipText;
    Double enterNum;
    Boolean a, b, c, d;
    InputMethodManager imm;
    URL url;
    int firstTime = 0;
    TextView returnValue;
    private String patternStr = ".*urn\">(.*)</div.*";
    private ProgressDialog pd;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstTime = MyApplication.popupInfo(getApplicationContext());
        setContentView(R.layout.glavno);
        
        // if first time, display this dialog box, carry valuable info :)
        if (firstTime == 1) {
      		AlertDialog.Builder builder = new AlertDialog.Builder(this);
      		builder.setMessage(R.string.sFirstTime)
      		.setCancelable(true)
      		.setNegativeButton(R.string.Close, new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int id) {
    					dialog.cancel();
    				}
    			});
      		AlertDialog test = (AlertDialog) builder.create();
      		test.show();
        } 
        
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        returnValue = (TextView) findViewById(R.id.returnedValue);
        
        number = (AutoCompleteTextView) findViewById(R.id.EditText01);
        
        Cursor cursor = null;
        // the reason why it does not support 1.5 and my laziness to work around it :)
        int sdkVersion = Build.VERSION.SDK_INT;
        if (sdkVersion < 5) {
        	ContactAccessorSdk34 adapter = new ContactAccessorSdk34(this, cursor);
            number.setAdapter(adapter);
        } else {
            ContactAccessorSdk5 adapter = new ContactAccessorSdk5(this, cursor);
            number.setAdapter(adapter);
        }
        
     // call button, call the phone number which the user checked
        callMe = (Button) findViewById(R.id.Button02);
		callMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				callTry = number.getText().toString();
				if (callTry.equals("")) {
					Toast.makeText(getApplicationContext(), R.string.sNumb4Call, Toast.LENGTH_LONG).show();
				} else {
					try {
						startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callTry)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		// sms button, send sms to the phone number which the user checked
		smsMe = (Button) findViewById(R.id.Button01);
        smsMe.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				smsTry = number.getText().toString();
				if (smsTry.equals("")) {
					Toast.makeText(getApplicationContext(), R.string.sNumb4Sms, Toast.LENGTH_LONG).show();
				} else {
					try {
						startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + smsTry)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
        // clear the number in the edittext box when the user touches it
        number.setOnTouchListener(new View.OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				returnValue.setText("");
				number.onTouchEvent(event);
				return true;
			}
		});
        
        // the button which will trigger the check of the phone number
        okBut = (Button)findViewById(R.id.Button03);
        okBut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// get the phone number and check if it is ported
				ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    			if (connec != null && (connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) ||(connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED)){
    				
    				retNum = number.getText().toString();
					imm.hideSoftInputFromWindow(number.getWindowToken(),0);
					
					try {
						checkedNum = numberCheck(retNum);
		            } catch (IOException e) {
						Toast.makeText(getApplicationContext(), R.string.sProblem, Toast.LENGTH_LONG).show();
						e.printStackTrace();
					}
					
		            if(checkedNum.equals("")) {
	            		Toast.makeText(getApplicationContext(), R.string.sEmptyNumber, Toast.LENGTH_LONG).show();
	            	} else {
		            
						pd = new ProgressDialog(Ported.this);
	    		        pd.setCancelable(true);
	    		        pd.setMessage(getString(R.string.sWait));
	    		        pd.show();
	    		        
	    				Thread background = new Thread (new Runnable() {
	    		            public void run() {
	    		            displayText = getPortInfo(checkedNum);
	    		            progressHandler.sendMessage(progressHandler.obtainMessage());
	    		            }
	    		         });
	    		        
	    		        background.start();
	    				
	    		        // update the call button with the new value for phone number
	    				callMe.setOnClickListener(new OnClickListener() {
	    					@Override
	    					public void onClick(View v) {
	    							try {
	    								startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + checkedNum)));
	    							} catch (Exception e) {
	    								e.printStackTrace();
	    							}
	    					}
	    				});
	            	}
					
				}else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||  connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED ) {             
    			    //Not connected.    
    			    Toast.makeText(getApplicationContext(), R.string.sNoInet, Toast.LENGTH_LONG).show();
    			} 
			}
		});
    }
    
    public String numberCheck(String firstTime) throws IOException {
    	// remove spaces, dashes and other stuff the user might input, other than digits
    	firstTime = getDigitsOnly(firstTime);
    	
    	a = firstTime.startsWith("0");
    	b = !firstTime.startsWith("0");
    	c = firstTime.startsWith("389");
    	d = !firstTime.startsWith("389");
    	if (a) {
    		// check if the length is 9 digits
    		if (firstTime.length() != 9) {
    			firstTime = "";
    			return firstTime;
    		}
    		// call the link and retrieve the message
    		// add stats counter -- planned for future versions
    		return firstTime;
    	} else if(c) {
    		// remove the 389 prefix and add 0, then check + stats
    		fixedNumber = "0" + firstTime.substring(3);
    		if (fixedNumber.length() != 9) {
    			firstTime = "";
    			return firstTime;
    		}
    		firstTime = fixedNumber;
    		return firstTime;
    	}
    	
    	else if ((b) || (d)) {
    		firstTime = "";
    		return firstTime;
    	}
    	return firstTime;
    }
    
    public static String getDigitsOnly(String str) {
        
        if (str == null) {
            return null;
        }

        StringBuffer strBuffer = new StringBuffer();
        char chars;
        
        for (int i = 0; i < str.length() ; i++) {
            chars = str.charAt(i);
            
            if (Character.isDigit(chars)) {
                strBuffer.append(chars);
            }
        }
        return strBuffer.toString();
    }
    
    public String getPortInfo(final String finalNum) {
    	aek = "http://www.aek.mk/GetCustomerData-veb1.php?id=" + finalNum;
		textNum = DownloadText(aek);
		// check the returned value, and replace it for the screen
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(textNum);
		Boolean matchFound = matcher.find(); 
		if (matchFound) {
			textNum = matcher.group(1);
		}
		chNum = " " + getString(R.string.chNum) + " ";
		newText = getString(R.string.newText);
		
		if (textNum.equals(chNum)) {
			textNum = newText;
			//custom for mobile operators -- too lazy to implement for all operators, there are too many combinations
			vipText = getString(R.string.VipNum);
			tmoText = getString(R.string.TmoNum);
			oneText = getString(R.string.OneNum);
			
			// Vip
			if(finalNum.startsWith("077") || finalNum.startsWith("078")){
				textNum = textNum + vipText;
			}
			// T-Mobile
			if(finalNum.startsWith("070") || finalNum.startsWith("071") || finalNum.startsWith("072")){
				textNum = textNum + tmoText;
			}
			// One/Cosmofon
			if(finalNum.startsWith("075") || finalNum.startsWith("076")){
				textNum = textNum + oneText;
			}
		}
		
		return textNum;
    }
    
    // download the text/source from the URL
    private String DownloadText(String URL)
    {
        int BUFFER_SIZE = 3000;
        InputStream in = null;
        try {
            in = OpenHttpConnection(URL);
        } catch (IOException e1) {
        	Toast.makeText(getApplicationContext(), R.string.sServProblem, Toast.LENGTH_LONG).show();
            e1.printStackTrace();
            return "";
        }
        
        InputStreamReader isr = new InputStreamReader(in);
        int charRead;
          String str = "";
          char[] inputBuffer = new char[BUFFER_SIZE];
        try {
            while ((charRead = isr.read(inputBuffer))>0)
            {
                // convert the chars to a String
                String readString = String.copyValueOf(inputBuffer, 0, charRead);         
                str += readString;
                inputBuffer = new char[BUFFER_SIZE];
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return str;
    }
    
    private InputStream OpenHttpConnection(String urlString) 
    throws IOException
    {
        InputStream in = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection))    
            throw new IOException(getString(R.string.sNoHTTP));
        
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect(); 

            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }
        catch (Exception ex)
        {
            throw new IOException(getString(R.string.sConnProblem));
        }
        return in;     
    }
	
    // handler for the background updating
    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
        	pd.dismiss();
        	returnValue.setText(displayText);
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	super.onCreateOptionsMenu(menu);
    	
    	menu.add(0, 111, 0, R.string.sAboutButton).setIcon(R.drawable.about);
    			
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item)
    {
         if (item.hasSubMenu() == false)
         {
        	 switch (item.getItemId()){
        	 case 111: // About          
                Dialog dialog = new Dialog(Ported.this);
 				dialog.setContentView(R.layout.about);
 				dialog.setCancelable(true);
 				
 				dialog.setTitle(R.string.sAboutButton);
         		TextView aboutBut = (TextView) dialog.findViewById(R.id.about);
					
         		aboutBut.setText(R.string.sAboutText);
         		
				dialog.show();				
 					
                return true;

 	  					
 	        	 }
 	         }
 	         
 	    return true;
    }
} 