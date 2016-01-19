package com.weyr_associates.lambtracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class DesktopFunctionsMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desktop_functions);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.desktop_functions, menu);
		return true;
	}
	public void setAlerts (View v){
		Intent i = null;
		i = new Intent(DesktopFunctionsMenu.this, SetAlerts.class);
		DesktopFunctionsMenu.this.startActivity(i);
	}
	
	public void createRamBreedingRecord (View v){
		Intent i = null;
		i = new Intent(DesktopFunctionsMenu.this, CreateRamBreedingRecord.class);
		DesktopFunctionsMenu.this.startActivity(i);
	}
	
	public void createEweBreedingRecord (View v){
		Intent i = null;
		i = new Intent(DesktopFunctionsMenu.this, CreateEweBreedingRecord.class);
		DesktopFunctionsMenu.this.startActivity(i);
	}
	public void drugManagement (View v){
		Intent i = null;
		i = new Intent(DesktopFunctionsMenu.this, DrugManagement.class);
		DesktopFunctionsMenu.this.startActivity(i);
	}	
	
}
