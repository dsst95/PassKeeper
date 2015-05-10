package de.szut.passkeeper.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
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

public class EntryActivity extends Activity implements IActivity {

    private EntryProperty entryProperty;
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
    private int entryId;
    private String decryptedUsername;
    private String decryptedUserPwd;
    private String databasePwd;
    private boolean hasDecrypted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaults();
        new BackgroundTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.entry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextEntryTitle.getWindowToken(), 0);
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.saveEntry:
                if (editTextEntryTitle.getText().length() != 0 && editTextEntryPwd.getText().length() != 0) {
                    hasDecrypted = !hasDecrypted;
                    new BackgroundTask().execute();
                } else {
                    AlertBuilderHelper alertBuilderHelper = new AlertBuilderHelper(this, R.string.dialog_title_missing_data, R.string.dialog_message_entry_required_data, false);
                    alertBuilderHelper.setPositiveButton(R.string.dialog_positive_button_default, null);
                    alertBuilderHelper.show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setDefaults() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        databaseId = getIntent().getExtras().getInt("databaseId");
        categoryId = getIntent().getExtras().getInt("categoryId");
        databasePwd = getIntent().getExtras().getString("databasePwd");
        databaseModel = new DatabaseModel(this);
        hasDecrypted = false;
        entryId = getIntent().getExtras().getInt("entryId");
        entryProperty = databaseModel.getUserEntryProperty(entryId);
        if (entryProperty != null) {
            setTitle(R.string.title_activity_update_entry);
        }
    }

    @Override
    public void populateView() {
        setContentView(R.layout.activity_entry_layout);
        editTextEntryTitle = (EditText) findViewById(R.id.editTextEntryTitle);
        editTextEntryUsername = (EditText) findViewById(R.id.editTextEntryUsername);
        editTextEntryPwd = (EditText) findViewById(R.id.editTextEntryPwd);
        editTextEntryNote = (EditText) findViewById(R.id.editTextEntryNote);
        imageButtonDisplayPwd = (ImageButton) findViewById(R.id.imageButtonDisplayPwd);
        imageButtonGeneratePwd = (ImageButton) findViewById(R.id.imageButtonGeneratePwd);
        imageButtonDisplayPwd.setOnTouchListener(new ViewPwdTouchListener(editTextEntryPwd));
        imageButtonGeneratePwd.setOnClickListener(new GeneratePwdClickListener(editTextEntryPwd));
        if (entryProperty != null) {
            editTextEntryTitle.setText(entryProperty.getEntryTitle());
            editTextEntryUsername.setText(decryptedUsername);
            editTextEntryPwd.setText(decryptedUserPwd);
            editTextEntryNote.setText(entryProperty.getEntryNote());
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void decryptData() {
        if (entryProperty != null) {
            String username = entryProperty.getEntryUsername();
            String password = entryProperty.getEntryPwd();
            byte[] salt = Base64.decode(entryProperty.getEntryHash(), Base64.NO_WRAP);
            SecretKey secret = Security.getInstance().getSecret(databasePwd, salt);
            byte[] iv = Base64.decode(entryProperty.getEntryIV(), Base64.NO_WRAP);
            decryptedUsername = Security.getInstance().decryptValue(username, secret, iv);
            decryptedUserPwd = Security.getInstance().decryptValue(password, secret, iv);
        }
    }

    private void encryptData() {
        String username = editTextEntryUsername.getText().toString();
        String password = editTextEntryPwd.getText().toString();
        byte[] salt = Security.getInstance().generateSalt();
        SecretKey secret = Security.getInstance().getSecret(databasePwd, salt);
        byte[] iv = Security.getInstance().generateIV();
        String encryptedUsername = Security.getInstance().encryptValue(username, secret, iv);
        String encryptedPassword = Security.getInstance().encryptValue(password, secret, iv);
        if (entryProperty != null) {
            databaseModel.updateUserEntry(new EntryProperty(
                    entryId,
                    editTextEntryTitle.getText().toString(),
                    encryptedUsername,
                    encryptedPassword,
                    Base64.encodeToString(salt, Base64.NO_WRAP),
                    editTextEntryNote.getText().toString(),
                    Base64.encodeToString(iv, Base64.NO_WRAP)
            ));
        } else {
            databaseModel.createUserEntry(new EntryProperty(
                            databaseId,
                            categoryId,
                            editTextEntryTitle.getText().toString(),
                            encryptedUsername,
                            encryptedPassword,
                            Base64.encodeToString(salt, Base64.NO_WRAP),
                            editTextEntryNote.getText().toString(),
                            R.drawable.ic_lock,
                            Base64.encodeToString(iv, Base64.NO_WRAP)
                    )
            );
        }
    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(EntryActivity.this);
            progressDialog.setMessage(getResources().getString(!hasDecrypted ? R.string.dialog_loading_message_decrypting_data : R.string.dialog_loading_message_encrypting_data));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!hasDecrypted) {
                decryptData();
            } else {
                encryptData();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aBoolean) {
            super.onPostExecute(aBoolean);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!hasDecrypted) {
                populateView();
            } else {
                onBackPressed();
            }
        }
    }
}