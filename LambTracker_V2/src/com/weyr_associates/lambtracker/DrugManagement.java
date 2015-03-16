package com.weyr_associates.lambtracker;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DrugManagement extends ListActivity {
	private DatabaseHandler dbh;
	private SimpleCursorAdapter drugAdapter;
	private long selectedItem;
	private String cmd, newPurchase, newExpiry, newLot, newQty, newPrice, drugName;
	private Cursor	drugCursor;
	private Context thisContext;
	private Dialog newLotDialog;
	public Object	crsr;
	public int 	numRecs;


	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drug_management);
		thisContext = this;

		String 	dbfile = getString(R.string.real_database_file) ;
		Log.i("DrugManagement", "Entry");
		
		try {
		dbh = new DatabaseHandler( this, dbfile );
		} catch (Exception e)
		{
			Toast.makeText(thisContext, "Couldn't open database", Toast.LENGTH_LONG).show();
			finish();
		}

		refreshLists();

	}

	// To be called every time we update the drug tables
	public void refreshLists ()
	{
		// Drop selection
		selectedItem = android.widget.AdapterView.INVALID_ROW_ID;

		TextView upperText = (TextView) findViewById(R.id.managedrugs_lbl);

		// Get all the drugs that are not drug_gone, sorted by type
		cmd =	"SELECT id_drugid AS _id, drug_type_table.drug_type AS drug_type, user_task_name, drug_lot, drug_expire_date FROM " +
				"drug_table, drug_type_table WHERE drug_type_table.id_drugtypeid = drug_table.drug_type " +
				"AND drug_gone != '1' ORDER BY drug_table.drug_type;";
		try {
		crsr = dbh.exec(cmd);
		} catch (Exception e)
		{
			Toast.makeText(thisContext, "SQL error", Toast.LENGTH_SHORT).show();
			return;
		}
		drugCursor = (Cursor) crsr;

		numRecs = drugCursor.getCount();
		drugCursor.moveToFirst();
		if (numRecs > 0)
		{
			String[] fromColumnsDrug = new String[ ]{ "drug_type", "user_task_name"};

			// Using simple_list_item_activated_2 to get activated background. Fields are text1 and text2
			int[] toViewsDrug = new int[] {android.R.id.text1, android.R.id.text2};
			drugAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2 ,drugCursor, fromColumnsDrug, toViewsDrug, 0);

			// Bind this adapter to a ViewBinder, to operate on the contents
			// This lets us re-format them in a sensible manner
			drugAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				@Override
				public boolean setViewValue(View view, Cursor cursor, int column) {

					// Column 1, drug_type, will be our formatted output for task name and lot number
					if( column == 1 ){
						TextView tv = (TextView) view;

						// Oh boy let's get these fields and jam them together
						tv.setText(String.format("%s: Lot #%s",cursor.getString(cursor.getColumnIndex("user_task_name")), cursor.getString(cursor.getColumnIndex("drug_lot"))));
						return true;
					}
					// Column 2, user_task_name, will hold the drug types and expiry dates (Sorry...)
					if (column == 2){
						TextView tv = (TextView) view;
						tv.setText(String.format("%s - expires on %s",cursor.getString(cursor.getColumnIndex("drug_type")),cursor.getString(cursor.getColumnIndex("drug_expire_date"))));
						return true;
					}
					return false;
				}
			});

			setListAdapter(drugAdapter);
		} else
		{
			// Let the user know we found nothing, point list at null adapter
			upperText.setText(R.string.managedrugs_nostock_lbl);
			drugAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, null, null, null, 0);
			setListAdapter(drugAdapter);

		}

	}

	// Catches clicks and updates selectedItem - can't believe this is necessary in this day and age
	@Override protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Log.i("DrugManagement", "Listitemclick set id" + String.format("%d", id));
		selectedItem = id;
	}

	// User clicked "Dispose"
	public void disposeBtn ( View v)
	{

		// Check to see that something has been selected
		if (selectedItem == android.widget.AdapterView.INVALID_ROW_ID)
		{
			Toast.makeText(thisContext, "No drug selected", Toast.LENGTH_SHORT).show();
			return;
		}

		// Grab the drug name for confirmation toast
		drugName = fetchSingleField(String.format("SELECT user_task_name FROM drug_table where id_drugid = %s;", selectedItem),"user_task_name");

		// Dispose of selected item today
		cmd = String.format("UPDATE drug_table SET drug_gone = 1, drug_dispose_date = '%s' WHERE id_drugid = %d;", Utilities.TodayIs(), selectedItem);
		Log.i("Dispose",cmd);
		try {
		dbh.exec(cmd);
		} catch (Exception e)
		{
			Toast.makeText(thisContext, "SQL error", Toast.LENGTH_SHORT).show();
			return;
		}
		refreshLists();
		Toast.makeText(thisContext, String.format("%s disposed of", drugName), Toast.LENGTH_SHORT).show();

	}

	// User clicked "New Lot"
	// This function will duplicate an existing drug, with new lot, purchase, and expiry dates.
	public void newLotBtn (View v)
	{

		// Check to see that something has been selected
		if (selectedItem == android.widget.AdapterView.INVALID_ROW_ID)
		{
			Toast.makeText(thisContext, "No drug selected", Toast.LENGTH_SHORT).show();
			return;
		}

		// Grab old data that we want to reuse
		drugName = fetchSingleField(String.format("SELECT user_task_name FROM drug_table where id_drugid = %s;", selectedItem),"user_task_name");
		
		// Create and title the date picker/lot number dialog
		newLotDialog = new Dialog(this);
		newLotDialog.setContentView(R.layout.drug_new_lot);
		newLotDialog.setTitle(String.format("Adding new lot of %s",drugName));
		newLotDialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corner);

		// Cancel button handler
		Button dialogButtonCancel = (Button) newLotDialog.findViewById(R.id.newlotCancel);

		dialogButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				newLotDialog.dismiss();
			}
		});

		// OK button handler
		Button dialogButtonOK = (Button) newLotDialog.findViewById(R.id.newlotOK);

		dialogButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// Extract data from dialog
				// Must use Dialog.findViewById() or null pointer will be returned due to scope issues
				DatePicker purchasePicker = (DatePicker) newLotDialog.findViewById(R.id.purchase_datePicker);
				DatePicker expiryPicker = (DatePicker) newLotDialog.findViewById(R.id.expiry_datePicker);
				TextView lotText = (TextView) newLotDialog.findViewById(R.id.newlot_text);
				TextView lotQty = (TextView) newLotDialog.findViewById(R.id.newlot_qty);
				TextView lotPrice = (TextView) newLotDialog.findViewById(R.id.newlot_price);

				// Format dates
				// Android pickers use zero index month
				newPurchase = String.format("%d-%d-%d", purchasePicker.getYear(), purchasePicker.getMonth()+1, purchasePicker.getDayOfMonth());
				newExpiry = String.format("%d-%d-%d", expiryPicker.getYear(), expiryPicker.getMonth()+1,expiryPicker.getDayOfMonth());
				newLot = lotText.getText().toString();

				newLotDialog.dismiss();

				// Duplicate the item with new lot, purchase/expiry date, quantity and price
				cmd = String.format("INSERT INTO drug_table (drug_type, official_drug_name, drug_lot, "+
						"drug_expire_date, drug_purchase_date, drug_meat_withdrawal, meat_withdrawal_units, "+
						"user_meat_withdrawal, generic_drug_name, official_drug_dosage, drug_amount_purchased, "+
						"drug_cost, off_label, off_label_vet, user_drug_dosage, user_task_name, drug_gone) "+
						"SELECT drug_type, official_drug_name, '%s', '%s', '%s', drug_meat_withdrawal, "+
						"meat_withdrawal_units, user_meat_withdrawal, generic_drug_name, official_drug_dosage, "+
						"'%s', '%s', off_label, off_label_vet, user_drug_dosage, "+
						"user_task_name, '0' "+
						"FROM drug_table WHERE id_drugid=%s;", newLot, newExpiry, newPurchase, newQty, newPrice, selectedItem);
				
				try {
				crsr = dbh.exec(cmd);
				} catch (Exception e)
				{
					Toast.makeText(thisContext, "SQL error", Toast.LENGTH_SHORT).show();
					return;
				}

				Toast.makeText(thisContext, String.format("Lot #%s of %s added to stock",newLot, drugName), Toast.LENGTH_SHORT).show();

				refreshLists();

			}
		});

		newLotDialog.show();

	}

	// user clicked the 'back' button
	public void backBtn( View v )
	{
		//	Close cursors if there are any but fall out if we don't have any in use
		try {
			//			Log.i("Back Button", " In try stmt cursor");
			drugCursor.close();

		}
		catch (Exception e) {
			Log.i("Back Button", "Failed to close cursors");
			// In this case there is no adapter so do nothing
		}

		dbh.closeDB();
		//Go back to main
		finish();
	}

	// User clicked Show Details
	public void showDetails( View v)
	{
		// Stub
		Toast.makeText(thisContext, "Stub!", Toast.LENGTH_SHORT).show();
	}

	// Fetches a single unique field. Will only retrieve the first if there are multiple rows returned.
	// Craft your query carefully.
	public String fetchSingleField(String query, String field)
	{
		Cursor tempCursor;
		Object tempCrsr;
		try {
		tempCrsr = dbh.exec(query);
		tempCursor = (Cursor) tempCrsr;
		numRecs = tempCursor.getCount();
		tempCursor.moveToFirst();
		} catch (Exception e)
		{
			Toast.makeText(thisContext, "SQL error", Toast.LENGTH_SHORT).show();
			return null;
		}

		if (numRecs > 0)
		{
			String fieldValue = dbh.getStr(field);
			tempCursor.close();
			return fieldValue;
		}
		else
		{
			tempCursor.close();
			// Return empty string instead of null for crash safety.
			return "";
		}
	}
}
