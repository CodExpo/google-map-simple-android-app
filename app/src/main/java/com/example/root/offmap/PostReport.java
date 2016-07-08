package com.example.root.offmap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class PostReport extends Activity implements View.OnClickListener {
    String coord,des,cat;
    EditText desc;
    RadioButton category;
    Button send;
    String res;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_report);
        Intent intent = getIntent();
        coord = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        desc = (EditText) findViewById(R.id.editText);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        category = (RadioButton) findViewById(selectedId);
        send = (Button) findViewById(R.id.button6);
        send.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        category = (RadioButton) findViewById(selectedId);
        cat = category.getTag().toString();
        des = desc.getText().toString();
        new sendSync().execute("http://farshadjafari.pythonanywhere.com/jsave/");
    }
    public static String POST(String url, StringEntity se){
        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(se);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
            HttpResponse httpResponse = httpclient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }
    private class sendSync extends AsyncTask <String, Void, String>{
        private ProgressDialog dialog;
        @Override
        protected String doInBackground(String... urls) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("geo", coord);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonObject.accumulate("cat", cat );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                jsonObject.accumulate("des", des);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String json = jsonObject.toString();
            StringEntity se = null;
            try {
                se = new StringEntity(json);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return POST(urls[0], se);
        }
        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();
            Toast.makeText(getBaseContext(), "با موفقیت ارسال شد", Toast.LENGTH_LONG).show();
            onBackPressed();
        }
        @Override
        protected void onPreExecute(){
            dialog = new ProgressDialog(PostReport.this);
            dialog.setMessage("تلاش برای ارسال...");
            dialog.show();
        }
    }
}
