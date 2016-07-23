/*
 * Copyright (C) 2008 ZXing authors
 * Modifications Copyright (C) 2013-2014 Weyr Associates LLC authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.encode;

import android.view.Display;
import android.view.MenuInflater;
import android.view.WindowManager;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends Activity {

  private static final String TAG = EncodeActivity.class.getSimpleName();

  private static final int MAX_BARCODE_FILENAME_LENGTH = 24;
  private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");
  private static final String USE_VCARD_KEY = "USE_VCARD";

  private QRCodeEncoder qrCodeEncoder;
  
  private boolean autoPrint = false;
  
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    Intent intent = getIntent();

    if (intent == null) {
      finish();
    } else {
      String action = intent.getAction();

      
      if (Intents.Encode.ACTION.equals(action) || Intent.ACTION_SEND.equals(action)) {
 //       setContentView(R.layout.encode); // This actually displays the barcode and optionally text

          String data = intent.getStringExtra(Intents.Encode.AUTOPRINT);
		   
    	  if (data.equals("true")) {
    	  autoPrint = true;
    	  };
    	  
    	  share();   // Try to send pix directly to print utility
    	  finish();  // needed if above commented out
      } else {
        finish();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.encode, menu);
    boolean useVcard = qrCodeEncoder != null && qrCodeEncoder.isUseVCard();
    int encodeNameResource = useVcard ? R.string.menu_encode_mecard : R.string.menu_encode_vcard;
    MenuItem encodeItem = menu.findItem(R.id.menu_encode);
    encodeItem.setTitle(encodeNameResource);
    Intent intent = getIntent();

    if (intent != null) {
      String type = intent.getStringExtra(Intents.Encode.TYPE);
      encodeItem.setVisible(Contents.Type.CONTACT.equals(type));
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
	if (itemId == R.id.menu_share) {
		share();
		return true;
	} else if (itemId == R.id.menu_encode) {
		Intent intent = getIntent();
		if (intent == null) {
          return false;
        }
		intent.putExtra(USE_VCARD_KEY, !qrCodeEncoder.isUseVCard());
		startActivity(intent);
		finish();
		return true;
	} else {
		return false;
	}
  }
  
  private void share() {
	  
	    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
	    Display display = manager.getDefaultDisplay();
	    int width = display.getWidth();
	    int height = display.getHeight();
	    int smallerDimension = width < height ? width : height;
	    smallerDimension = smallerDimension * 7 / 8;
	    Bitmap bitmap = null;
	    
	    try {
	      qrCodeEncoder = new QRCodeEncoder(this, getIntent(), smallerDimension, false);
	      bitmap = qrCodeEncoder.encodeAsBitmap();
	      if (bitmap == null) {
	        Log.w(TAG, "Could not encode barcode");
	        showErrorMessage(R.string.msg_encode_contents_failed);
	        qrCodeEncoder = null;
	        return;
	      }
	    } catch (WriterException e) {
	      Log.w(TAG, "Could not encode barcode", e);
	      showErrorMessage(R.string.msg_encode_contents_failed);
	      qrCodeEncoder = null;
	    }
  
    String contents = qrCodeEncoder.getContents();

    if (contents == null) {
      Log.w(TAG, "No existing barcode to send?2");
      return;
    }

    File bsRoot = new File(Environment.getExternalStorageDirectory(), "BarcodeScanner");
    File barcodesRoot = new File(bsRoot, "Barcodes");
    if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
      Log.w(TAG, "Couldn't make dir " + barcodesRoot);
      showErrorMessage(R.string.msg_unmount_usb);
      return;
    }
//    File barcodeFile = new File(barcodesRoot, makeBarcodeFileName(contents) + ".png");
    File barcodeFile = new File(barcodesRoot, "temp_barcode" + ".png");
    barcodeFile.delete();
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(barcodeFile);
      bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
    } catch (FileNotFoundException fnfe) {
      Log.w(TAG, "Couldn't access file " + barcodeFile + " due to " + fnfe);
      showErrorMessage(R.string.msg_unmount_usb);
      return;
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException ioe) {
          // do nothing
        }
      }
    }
  
// Code to select specific share app
    String type = "weyr_associates.print";
	    boolean found = false;

	    Intent share = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
	    share.setType("image/jpeg");

	    // gets the list of intents that can be loaded.
	    List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
	    if (!resInfo.isEmpty()){
	        for (ResolveInfo info : resInfo) {
	            if (info.activityInfo.packageName.toLowerCase().contains(type) || 
	                    info.activityInfo.name.toLowerCase().contains(type) ) {
	    		    share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " - " + qrCodeEncoder.getTitle());
	    		    share.putExtra(Intent.EXTRA_TEXT, contents);
	    		    share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + barcodeFile.getAbsolutePath()));
	    		    share.setType("image/png");
	    		    share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	    		    
	    		     if (autoPrint) {
	    		    share.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
	    		     };
	    		    
	                share.setPackage(info.activityInfo.packageName);
	                found = true;
	                break;
	            }
	        }
	        if (!found)
	            return;

	        startActivity(Intent.createChooser(share, "Select"));
	    }
	}  

  private static CharSequence makeBarcodeFileName(CharSequence contents) {
    String fileName = NOT_ALPHANUMERIC.matcher(contents).replaceAll("_");
    if (fileName.length() > MAX_BARCODE_FILENAME_LENGTH) {
      fileName = fileName.substring(0, MAX_BARCODE_FILENAME_LENGTH);
    }
    return fileName;
  }

  @Override
  protected void onResume() {
    super.onResume();
    // This assumes the view is full screen, which is a good assumption
    WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
    Display display = manager.getDefaultDisplay();
    int width = display.getWidth();
    int height = display.getHeight();
    int smallerDimension = width < height ? width : height;
    smallerDimension = smallerDimension * 7 / 8;

    Intent intent = getIntent();

    if (intent == null) {
      return;
    }
 
    try {
      boolean useVCard = intent.getBooleanExtra(USE_VCARD_KEY, false);
      qrCodeEncoder = new QRCodeEncoder(this, intent, smallerDimension, useVCard);
      Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
      if (bitmap == null) {
        Log.w(TAG, "Could not encode barcode");
        showErrorMessage(R.string.msg_encode_contents_failed);
        qrCodeEncoder = null;
        return;
      }

      ImageView view = (ImageView) findViewById(R.id.image_view);
      view.setImageBitmap(bitmap);

      TextView contents = (TextView) findViewById(R.id.contents_text_view);
      if (intent.getBooleanExtra(Intents.Encode.SHOW_CONTENTS, true)) {
        contents.setText(qrCodeEncoder.getDisplayContents());
        setTitle(qrCodeEncoder.getTitle());
      } else {
        contents.setText("");
        setTitle("");
      }
    } catch (WriterException e) {
      Log.w(TAG, "Could not encode barcode", e);
      showErrorMessage(R.string.msg_encode_contents_failed);
      qrCodeEncoder = null;
    }
  }


  private void showErrorMessage(int message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage(message);
    builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
    builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }
}
