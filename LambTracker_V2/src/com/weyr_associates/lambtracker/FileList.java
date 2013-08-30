package com.weyr_associates.lambtracker;

import java.io.File;
import java.util.ArrayList;

//import com.weyr_associates.printutility.R;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/** 
 * make a activity to show a file list from SD card 
 *
 * @author  Brother Industries, Ltd.
 * @version 1.0 
 */
public class FileList extends ListActivity{
	private String m_strDirPath;
	private ArrayList<String> items = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filelist);
		Bundle extras = getIntent().getExtras();
		m_strDirPath = extras.getString("fileName");
		if(m_strDirPath.equals("")){
			m_strDirPath = Environment.getExternalStorageDirectory().toString();
		}else{
			m_strDirPath = getParentDirPath(m_strDirPath);
		}
		fillList();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		String strItem =(String)getListAdapter().getItem(position);

		if(strItem.equals("..")){
			m_strDirPath = getParentDirPath(m_strDirPath);
			fillList();
		}else if(strItem.substring(strItem.length() - 1).equals("/")){
			if(m_strDirPath.equals("/")){
				m_strDirPath += strItem;
			}else{
				m_strDirPath = m_strDirPath + "/" + strItem;
			}
			m_strDirPath = m_strDirPath.substring(0, m_strDirPath.length() - 1);
			fillList();
		}else{
			if(m_strDirPath.equals("/")){
				m_strDirPath += strItem;
			}else{
				m_strDirPath = m_strDirPath + "/" + strItem;
			}
			Intent mainMenu = new Intent(this, MainActivity.class);
			mainMenu.putExtra("fileName", m_strDirPath);
			setResult(RESULT_OK, mainMenu);
			finish();
		}
	}

	private void fillList()
	{
		File[] files = new File(m_strDirPath).listFiles();
		if(files == null){
			Toast.makeText(this, "Unable Access...", Toast.LENGTH_SHORT).show();
			m_strDirPath = getParentDirPath(m_strDirPath);
			return;
		}
		TextView txtDirName =(TextView)findViewById(R.id.txtDirName);
		txtDirName.setText(m_strDirPath);

		if(items != null){
			items.clear();
		}
		items = new ArrayList<String>();

		if(!m_strDirPath.equals("/") && !m_strDirPath.equals("/mnt")){
			items.add("..");
		}
		for(File file : files){
			if(file.isDirectory()){
				
				items.add(file.getName() + "/");
			}else{
				String filename = file.getName();
				String extention = filename.substring(filename.lastIndexOf(".", filename.length())+1,filename.length());

				if((extention.equalsIgnoreCase("jpg")) || (extention.equalsIgnoreCase("jpeg")) || (extention.equalsIgnoreCase("bmp"))
				|| (extention.equalsIgnoreCase("png")) || (extention.equalsIgnoreCase("prn")) || (extention.equalsIgnoreCase("gif"))
				|| (extention.equalsIgnoreCase("pdf")) || (extention.equalsIgnoreCase("pdz"))){
					items.add(filename);
				}
			}
		}
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(fileList);
	}

	private String getParentDirPath(String strPath)
	{
		if(strPath.lastIndexOf("/") <= 0){
			return strPath.substring(0, strPath.lastIndexOf("/") + 1);
		}else{
			return strPath.substring(0, strPath.lastIndexOf("/"));
		}
	}
}
