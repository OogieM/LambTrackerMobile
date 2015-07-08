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
	
	public void sheepManagement (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, SheepManagement.class);
		SheepManagementMenu.this.startActivity(i);
	}
	public void lambingSheep (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, LambingSheep.class);
		SheepManagementMenu.this.startActivity(i);
	} 
	public void groupSheepManagement (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, GroupSheepManagement.class);
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
	public void removeSheep (View v){
		Intent i = null;
		i = new Intent(SheepManagementMenu.this, RemoveSheep.class);
		SheepManagementMenu.this.startActivity(i);
	}
}
