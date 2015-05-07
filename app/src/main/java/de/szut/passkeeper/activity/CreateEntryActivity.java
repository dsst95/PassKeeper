package de.szut.passkeeper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import javax.crypto.SecretKey;

import de.szut.passkeeper.R;
import de.szut.passkeeper.interfaces.IActivity;
import de.szut.passkeeper.model.DatabaseModel;
import de.szut.passkeeper.model.Security;
import de.szut.passkeeper.property.EntryProperty;
import de.szut.passkeeper.utility.AlertBuilderHelper;
import de.szut.passkeeper.utility.GeneratePwdClickListener;
import de.szut.passkeeper.utility.ViewPwdTouchListener;

public class CreateEntryActivity extends Activity implements IActivity {

    private EditText editTextEntryTitle;
    private EditText editTextEntryUsername;
    private EditText editTextEntryPwd;
    private EditText editTextEntryNote;
    private ImageButton imageButtonDisplayPwd;
    private ImageButton imageButtonGeneratePwd;
    private ProgressDialog progressDialog;
    private DatabaseModel databaseModel;
    private int databaseId;
    private int categoryId;
    private String databasePwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaults();
        populateView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.createEntry:
                if (editTextEntryTitle.getText().length() != 0 && editTextEntryPwd.getText().length() != 0) {
                    new BackgroundTask().execute();
                } else {
                    AlertBuilderHelper alertBuilderHelper = new AlertBuilderHelper(CreateEntryActivity.this, R.string.dialog_title_missing_data, R.string.dialog_message_entry_required_data, false);
                    alertBuilderHelper.setPositiveButton(R.string.dialog_positive_button_default, null);
                    alertBuilderHelper.show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void setDefaults() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        databaseId = getIntent().getExtras().getInt("databaseId");
        categoryId = getIntent().getExtras().getInt("categoryId");
        databasePwd = getIntent().getExtras().getString("databasePwd");
        databaseModel = new DatabaseModel(this);
    }

    @Override
    public void populateView() {
        setContentView(R.layout.activity_create_entry_layout);
        editTextEntryTitle = (EditText) findViewById(R.id.editTextEntryTitle);
        editTextEntryUsername = (EditText) findViewById(R.id.editTextEntryUsername);
        editTextEntryPwd = (EditText) findViewById(R.id.editTextEntryPwd);
        editTextEntryNote = (EditText) findViewById(R.id.editTextEntryNote);
        imageButtonDisplayPwd = (ImageButton) findViewById(R.id.imageButtonDisplayPwd);
        imageButtonGeneratePwd = (ImageButton) findViewById(R.id.imageButtonGeneratePwd);
        imageButtonDisplayPwd.setOnTouchListener(new ViewPwdTouchListener(editTextEntryPwd));
        imageButtonGeneratePwd.setOnClickListener(new GeneratePwdClickListener(editTextEntryPwd));
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CreateEntryActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.dialog_loading_message_encrypting_data));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String username = editTextEntryUsername.getText().toString();
            String password = editTextEntryPwd.getText().toString();
            byte[] salt = Security.getInstance().generateSalt();
            SecretKey secret = Security.getInstance().getSecret(databasePwd, salt);
            byte[] iv = Security.getInstance().generateIV();
            String hashedUsername = Security.getInstance().encryptValue(username, secret, iv);
            String hashedPassword = Security.getInstance().encryptValue(password, secret, iv);
            databaseModel.createUserEntry(new EntryProperty(
                            databaseId,
                            categoryId,
                            editTextEntryTitle.getText().toString(),
                            hashedUsername,
                            hashedPassword,
                            Base64.encodeToString(salt, Base64.NO_WRAP),
                            editTextEntryNote.getText().toString(),
                            R.drawable.ic_lock,
                            Base64.encodeToString(iv, Base64.NO_WRAP)
                    )
            );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            onBackPressed();
        }
    }
}