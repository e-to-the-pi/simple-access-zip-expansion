//LocalFileContentProvider.java
//Provide local file access to application's user data directory

//package com.tourizo.android.content;
package com.westla.brockton.email;

import java.io.*;

import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;

import android.util.Log;


public class LocalFileContentProvider extends ContentProvider {
   //private static final String URI_PREFIX = "content://com.tourizo.android.localfile";
   //private static final String URI_PREFIX = "content://com.app.data.localfile";
	private static final String URI_PREFIX = "content://com.westla.brockton.email.localfile";


   public static String constructUri(String url) {
   		 Log.d("LocalFileContentProvider","constructUri(String url) arg: " + url);
       Uri uri = Uri.parse(url);
       //return uri.isAbsolute() ? url : URI_PREFIX + url;
       return uri.toString();
   }




   @Override
   public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
  		 Log.d("LocalFileContentProvider","openFile(Uri uri, String mode) args : " + uri.toString() +"   "+ mode );
        File file = new File(uri.getPath());
       ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
       return parcel;
   }

   @Override
   public boolean onCreate() {
   		 Log.d("LocalFileContentProvider","onCreate()");
       return true;
   }

   @Override
   public int delete(Uri uri, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }


     @Override
   public String getType(Uri uri) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

}