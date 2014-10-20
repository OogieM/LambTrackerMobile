package com.weyr_associates.lambtracker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class SheepManagementMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sheep_management_menu);
		
		   
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sheep_management_menu, menu);
		return true;
	}
	public void sheepManagement (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, SheepManagement.class);
		SheepManagementMenu.this.startActivity(i);
	}
	public void drawBlood (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, DrawBlood.class);
		SheepManagementMenu.this.startActivity(i);
	}
	public void sortSheep (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, SortSheep.class);
		SheepManagementMenu.this.startActivity(i);
	}
	public void evaluateSheep (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, EvaluateSheep2.class);
		SheepManagementMenu.this.startActivity(i);
	}
}
