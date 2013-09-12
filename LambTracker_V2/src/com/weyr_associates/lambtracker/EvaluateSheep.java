package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.database.Cursor;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class EvaluateSheep extends Activity {
	
	public Button button;
	
	String     	cmd;
	Integer 	i;
	
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07;
	int id;
	
	List<String> scored_evaluation_traits, data_evaluation_traits;
	List<Integer> which_traits;
	
	ArrayAdapter<String> dataAdapter;
	
	private RatingBar trait01_ratingbar ;
	private RatingBar trait02_ratingbar ;
	private RatingBar trait03_ratingbar ;
	private RatingBar trait04_ratingbar ;
	private RatingBar trait05_ratingbar ;
//	private RatingBar trait06_ratingbar ;
//	private RatingBar trait07_ratingbar ;
//	private RatingBar trait08_ratingbar ;
//	private RatingBar trait09_ratingbar ;
//	private RatingBar trait10_ratingbar ;
//	private RatingBar trait11_ratingbar ;
//	private RatingBar trait12_ratingbar ;
	public Float trait01_data, trait02_data, trait03_data, trait04_data, trait05_data, trait06_data, trait07_data;
	
	private DatabaseHandler dbh;
	private Cursor 	cursor;
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_sheep);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        
        TextView TV;       
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
       
        cmd = "select * from temp_table ";
        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        
        dbh.moveToFirstRecord();
        trait01 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait01 = " + String.valueOf(trait01));
        cursor.moveToNext();
        
        trait02 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait02 = " + String.valueOf(trait02));
        cursor.moveToNext();
        
        trait03 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait03 = " + String.valueOf(trait03));
        cursor.moveToNext();
        
        trait04 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait04 = " + String.valueOf(trait04));
        cursor.moveToNext();
        
        trait05 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait05 = " + String.valueOf(trait05));
        cursor.moveToNext();
        
        trait06 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait06 = " + String.valueOf(trait06));
        cursor.moveToNext();
        
        trait07 = cursor.getInt(1);
        Log.i("EvaluateSheep ", "trait07 = " + String.valueOf(trait07));
        cursor.moveToNext();    
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait01 );
        Log.i("get name ", cmd);
        crsr = dbh.exec( cmd );
        Log.i("EvaluateSheep ", "after get name");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait01_lbl );
        TV.setText(dbh.getStr(0));
        Log.i("EvaluateSheep ", "after get the text");
              
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait02 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait02_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait03 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait03_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait04 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait04_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait05 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait05_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait06 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait06_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait07 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait07_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
       	}
    public void saveScores( View v )
    {    		
    		trait01_ratingbar = (RatingBar) findViewById(R.id.trait01_ratingbar);
    		trait01_data = trait01_ratingbar.getRating();
    		Log.i("trait01_ratingbar ", String.valueOf(trait01_data));
    		
    		trait02_ratingbar = (RatingBar) findViewById(R.id.trait02_ratingbar);
    		trait02_data = trait01_ratingbar.getRating();
    		Log.i("trait02_ratingbar ", String.valueOf(trait02_data));
    		
    		trait03_ratingbar = (RatingBar) findViewById(R.id.trait03_ratingbar);
    		trait03_data = trait01_ratingbar.getRating();	
    		Log.i("trait03_ratingbar ", String.valueOf(trait03_data));
    		
    		trait04_ratingbar = (RatingBar) findViewById(R.id.trait04_ratingbar);
    		trait04_data = trait01_ratingbar.getRating();
    		Log.i("trait04_ratingbar ", String.valueOf(trait04_data));
    		
    		trait05_ratingbar = (RatingBar) findViewById(R.id.trait05_ratingbar);
    		trait05_data = trait01_ratingbar.getRating();
    		Log.i("trait05_ratingbar ", String.valueOf(trait05_data));
//    		
//    		trait06_ratingbar = (RatingBar) findViewById(R.id.trait06_ratingbar);
//    		rating_scores.add(trait06_ratingbar.getRating());	
//    		Log.i("trait06_ratingbar ", String.valueOf(trait06_ratingbar.getRating()));
//    		
//    		trait07_ratingbar = (RatingBar) findViewById(R.id.trait07_ratingbar);
//    		rating_scores.add(trait07_ratingbar.getRating());	
//    		Log.i("trait07_ratingbar ", String.valueOf(trait07_ratingbar.getRating()));
//    		
//    		trait08_ratingbar = (RatingBar) findViewById(R.id.trait08_ratingbar);
//    		rating_scores.add(trait08_ratingbar.getRating());	
//    		Log.i("trait08_ratingbar ", String.valueOf(trait08_ratingbar.getRating()));
//    		
//    		trait09_ratingbar = (RatingBar) findViewById(R.id.trait09_ratingbar);
//    		rating_scores.add(trait09_ratingbar.getRating());	
//    		Log.i("trait09_ratingbar ", String.valueOf(trait09_ratingbar.getRating()));
//    		
//    		trait10_ratingbar = (RatingBar) findViewById(R.id.trait10_ratingbar);
//    		rating_scores.add(trait10_ratingbar.getRating());	
//    		Log.i("trait10_ratingbar ", String.valueOf(trait10_ratingbar.getRating()));
//    		
//    		trait11_ratingbar = (RatingBar) findViewById(R.id.trait11_ratingbar);
//    		rating_scores.add(trait11_ratingbar.getRating());	
//    		Log.i("trait11_ratingbar ", String.valueOf(trait11_ratingbar.getRating()));
//    		
//    		trait12_ratingbar = (RatingBar) findViewById(R.id.trait12_ratingbar);
//    		rating_scores.add(trait12_ratingbar.getRating());	
//    		Log.i("trait12_ratingbar ", String.valueOf(trait12_ratingbar.getRating()));
//    		
    }
	
	   public void backBtn( View v )
	    {
      	dbh.closeDB();
//      	clearBtn( null );   	
   		finish();
	    }
	   
	public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_evaluate )
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	        	  
	    		   clearBtn( null );
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();
		
    }
	public void clearBtn( View v )
    {
		// clear out the display of everything
		TextView TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
    
    }
}
