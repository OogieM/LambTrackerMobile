package com.weyr_associates.lambtracker;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.app.Activity;
import java.text.DecimalFormat;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Spinner;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView parent, View view, int pos, long id) {

//			Toast.makeText(parent.getContext(), "Selected Task : " + parent.getItemAtPosition(pos).toString(), Toast.LENGTH_SHORT).show();
		
			switch (parent.getSelectedItemPosition()){
			
			case 1:
				Intent i = new Intent(this, LoadSheepList.class);
		        startActivity(i);
		        break;
			case 2:
				Intent i = new Intent(this, DoSheepTask.class);
		        startActivity(i);
		        break;
			case 3:
				Intent i = new Intent(this, ConvertToEID.class);
		        startActivity(i);
		        break;
			case 4:
				Intent i = new Intent(this, EvaluateSheep.class);
		        startActivity(i);
		        break;
			case 5:
				Intent i = new Intent(this, EditDB.class);
		        startActivity(i);
		        break;
		
		}
		
		}

		@Override
		public void onNothingSelected(AdapterView parent) {

		}
	}

