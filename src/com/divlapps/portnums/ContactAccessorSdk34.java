package com.divlapps.portnums;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;


@SuppressWarnings("deprecation")
public class ContactAccessorSdk34 extends CursorAdapter implements Filterable {
	public ContactAccessorSdk34(Context context, Cursor c) {
        super(context, c);
        mContent = context.getContentResolver();
        c = mContent.query(Contacts.People.CONTENT_URI, PEOPLE_PROJECTION, null, null, Contacts.People.DEFAULT_SORT_ORDER);
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final TextView view = (TextView) inflater.inflate(R.layout.list_menu, parent, false);
        view.setText(cursor.getString(5));
        return view;
    }
    
    
    // display this in the dropdown list (5 = name, 3 = phone nr)
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
            buffer.append(Contacts.ContactMethods.NAME);
            buffer.append(") GLOB ?");
            args = new String[] { constraint.toString().toUpperCase() + "*" };
        }

        return mContent.query(Contacts.People.CONTENT_URI, PEOPLE_PROJECTION,
                buffer == null ? null : buffer.toString(), args,
                Contacts.People.DEFAULT_SORT_ORDER);
    }

    private ContentResolver mContent; 
    
    private static final String[] PEOPLE_PROJECTION = new String[] {
        Contacts.People._ID,
        Contacts.People.PRIMARY_PHONE_ID,
        Contacts.People.TYPE,
        Contacts.People.NUMBER,
        Contacts.People.LABEL,
        Contacts.People.NAME,
    }; 
}


