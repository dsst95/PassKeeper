package de.szut.passkeeper.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Vector;

import de.szut.passkeeper.Interface.IActivity;
import de.szut.passkeeper.Interface.IUserProperty;
import de.szut.passkeeper.Model.DatabaseModel;
import de.szut.passkeeper.Property.CategoryProperty;
import de.szut.passkeeper.Property.EntryProperty;
import de.szut.passkeeper.R;
import de.szut.passkeeper.Utility.AlertBuilderHelper;
import de.szut.passkeeper.Utility.ListViewAdapter;

public class ListCategoryActivity extends Activity implements AdapterView.OnItemClickListener, IActivity {

    private ListView listView;
    private Vector<IUserProperty> vectorCategoryProperty;
    private DatabaseModel databaseModel;
    private int databaseId;
    private String password;

    //TODO implement context menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview_layout);
        setDefaults();
        populateView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.addCategory:
                AlertBuilderHelper alertDialog = new AlertBuilderHelper(this, R.string.dialog_title_add_category, R.string.dialog_message_add_category, true);
                final EditText editText = new EditText(this);
                editText.setHint(R.string.hint_category_name);
                alertDialog.setView(editText);
                alertDialog.setPositiveButton(R.string.dialog_positive_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseModel.createCategory(databaseId, editText.getText().toString());
                        populateView();
                    }
                });
                alertDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ListCategoryActivity.this, ListEntryActivity.class);
        intent.putExtra("categoryName", ((CategoryProperty) vectorCategoryProperty.get(position)).getCategoryName());
        intent.putExtra("databaseId", ((CategoryProperty) vectorCategoryProperty.get(position)).getDatabaseId());
        intent.putExtra("categoryId", ((CategoryProperty) vectorCategoryProperty.get(position)).getCategoryId());
        intent.putExtra("password", password);
        startActivity(intent);
    }

    @Override
    public void setDefaults() {
        setTitle(getIntent().getExtras().getString("databaseName"));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        databaseId = getIntent().getExtras().getInt("databaseId");
        password = getIntent().getExtras().getString("password");
        databaseModel = new DatabaseModel(getApplicationContext());
        listView = (ListView) findViewById(R.id.listViewDefault);

    }

    @Override
    public void populateView() {
        vectorCategoryProperty = databaseModel.getUserCategoryPropertyVector(databaseId);
        listView.setAdapter(new ListViewAdapter(vectorCategoryProperty, this));
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);
    }
}