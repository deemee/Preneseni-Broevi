package com.divlapps.portnums;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class ContactAccessorSdk5 extends CursorAdapter implements Filterable {
	public ContactAccessorSdk5(Context context, Cursor c) {
        super(context, c);
        mContent = context.getContentResolver();
        c = mContent.query(Phone.CONTENT_URI, PEOPLE_PROJECTION, null, null, null);
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(R.layout.list_menu, parent, false);
        view.setText(cursor.getString(5));
        return view;
    }
    
    
    // display this in the dropdown menu (5 = name, 3 = phone nr)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    	String nameNumber = cursor.getString(5) + " <" + cursor.getString(3) + "> "; 
    	((TextView) view).setText(nameNumber);
        
    }
    
    // insert the following value in the edittext (number)
    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(3);
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
    	if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
    	
        StringBuilder buffer = null;
        String[] args = null;
        if (constraint != null) {
            buffer = new StringBuilder();
            buffer.append("UPPER(");
            buffer.append(Phone.DISPLAY_NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }

        return mContent.query(Phone.CONTENT_URI, PEOPLE_PROJECTION,
                buffer == null ? null : buffer.toString(), args,
                		null);
    }

    private ContentResolver mContent;    
    
    private static final String[] PEOPLE_PROJECTION = new String[] {
    	Phone._ID,
    	Phone.IS_PRIMARY,
    	Phone.TYPE,
    	Phone.NUMBER,
    	Phone.LABEL,
    	Phone.DISPLAY_NAME,
    }; 
}


