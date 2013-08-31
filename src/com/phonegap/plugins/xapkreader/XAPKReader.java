/**
 * cordova is available under *either* the terms of the modified BSD license *or* the
 * MIT License (2008). See http://opensource.org/licenses/alphabetical for full text.
 *
 * Copyright (c) Quentin Aupetit 2013
 */

package com.phonegap.plugins.xapkreader;

import java.io.BufferedInputStream;
import java.io.IOException;

//Added to Support ZipHelper
import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.Enumeration;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Base64;
import android.util.Log;

import android.os.Environment; //For Environment access

import com.android.vending.expansion.zipfile.APKExpansionSupport;
import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.google.android.vending.expansion.downloader.Helpers;

public class XAPKReader extends CordovaPlugin
{
	final static int mainVersion = 1;
	final static int patchVersion = 1;
    //private ZipHelper __zipHelper = new ZipHelper();
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals("get")) {
			final String filename = args.getString(0);
			try {
        		Context ctx = cordova.getActivity().getApplicationContext();
        		// Read file as array buffer
        		byte[] data = XAPKReader.readFile(ctx, filename);
	        	if (null != data) {
	        		// Encode to Base64 string
					String encoded = Base64.encodeToString(data, Base64.DEFAULT);
					// Return file data as base64 string
					callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, encoded));
	        	}
	        	else {
	        		callbackContext.error("File not found.");
	        	}
        	}
    		catch(Exception e) {
    			e.printStackTrace();
    			callbackContext.error(e.getMessage());
    		}
			return true;
		}
		return false;
	}
	
	private static byte[] readFile(Context ctx, String filename) throws IOException {
		// Get APKExpensionFile
		ZipResourceFile expansionFile = APKExpansionSupport.getAPKExpansionZipFile(ctx, XAPKReader.mainVersion, XAPKReader.patchVersion);
		
		if (null == expansionFile) {
			Log.e("XAPKReader", "APKExpansionFile not found.");
			//Log.e()
			return null;
    	}
		
		// Find file in ExpansionFile
		Log.d("XAPKReader", "ZipResourceFile expansionFile = APKExpansionSupport.getAPKExpansionZipFile  :   " + expansionFile);
		Log.d("XAPKReader", "  expansionFile.getAllEntries()   " + expansionFile.getAllEntries());

		Log.d("XAPKReader",  "Environment.getExternalStorageDirectory() :  " + Environment.getExternalStorageDirectory() ) ;
		Log.d("XAPKReader",  "getExternalFilesDir() :  " + ctx.getExternalFilesDir(null)) ;

		String fileName = Helpers.getExpansionAPKFileName(ctx, true, XAPKReader.patchVersion);
        String fileNameForZipHelper = fileName;
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
	    //AssetFileDescriptor file = expansionFile.getAssetFileDescriptor(fileName + "/" + filename);
		AssetFileDescriptor file = expansionFile.getAssetFileDescriptor(filename);
		
		if (null == file) {
			Log.d("XAPKReader", "String fileName = Helpers.getExpansionAPKFileName :  " + fileName);
			Log.e("XAPKReader", "File not found (" + filename + ").");
    		return null;
    	}
		

        Log.d("XAPKReader","attempt unzip to data directory");
        try{       
            //f is type string or zipfile, file to unzip
            //outputDir is type file, output directory for zip contents
            //ZipHelper.unzip(f, outputDir);     
            /*use Helper to get string representation of file location with full path and name
            Helpers:
            static public String generateSaveFileName(Context c, String fileName)
            */
            ZipHelper __zipHelper = new ZipHelper();  

            //unzips to apps data storage folder specified by getExternalFilesDir - mostly either   or  /mnt/shell/emulated/0/Android/data/com..../files 
            __zipHelper.unzip(Helpers.generateSaveFileName(ctx, Helpers.getExpansionAPKFileName(ctx, true, XAPKReader.mainVersion)), ctx.getExternalFilesDir(null));

            //
            __zipHelper.unzip(Helpers.generateSaveFileName(ctx, Helpers.getExpansionAPKFileName(ctx, true, XAPKReader.mainVersion)), new File("assets/www/zzzip")  );

        }
        catch (Exception e) {
            Log.d("XAPKReader","unzip error: "+e);
        }
 








		// Read file
		int size = (int) file.getLength();
	    byte[] data = new byte[size];
		BufferedInputStream buf = new BufferedInputStream(file.createInputStream());
        buf.read(data, 0, data.length);
        buf.close();
		
		return data;
	}
}




/*
Unzip helper from stack overflow at :
http://stackoverflow.com/questions/11715855/steps-to-create-apk-expansion-file/11717019#11717019
*/
class ZipHelper
{
    boolean zipError=false;

    //ZipHelper(){}

    public boolean isZipError() {
        return zipError;
    }

    public void setZipError(boolean zipError) {
        this.zipError = zipError;
    }

    public void unzip(String archive, File outputDir)
    {
        try {
            Log.d("control","ZipHelper.unzip() - File: " + archive);
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);

            }
        }
        catch (Exception e) {
            Log.d("control","ZipHelper.unzip() - Error extracting file " + archive+": "+ e);
            setZipError(true);
        }
    }

    public void unzip(ZipFile zipfile, File outputDir)
    {
        try {
            Log.d("control","ZipHelper.unzip() - File: " + zipfile.toString());
            //ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);

            }
        }
        catch (Exception e) {
            Log.d("control","ZipHelper.unzip() - Error extracting file " + zipfile.toString()+": "+ e);
            setZipError(true);
        }
    }



    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException
    {
        if (entry.isDirectory()) {
            createDirectory(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            createDirectory(outputFile.getParentFile());
        }

        Log.d("control","ZipHelper.unzipEntry() - Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e) {
            Log.d("control","ZipHelper.unzipEntry() - Error: " + e);
            setZipError(true);
        }
        finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDirectory(File dir)
    {
        Log.d("control","ZipHelper.createDir() - Creating directory: "+dir.getName());
        if (!dir.exists()){
            if(!dir.mkdirs()) throw new RuntimeException("Can't create directory "+dir);
        }
        else Log.d("control","ZipHelper.createDir() - Exists directory: "+dir.getName());
    }
}