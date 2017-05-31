package com.example.rspl_rahul.mcashrst;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    //Response response;
    Button CashOut,PayforGoods,CashIn;
    int success;
    String Message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         CashOut = (Button) findViewById(R.id.CashOut);
         CashIn=(Button)findViewById(R.id.CashIn);
         PayforGoods=(Button)findViewById(R.id.PayForGoods);
         CashOut.setOnClickListener(this);
         CashIn.setOnClickListener(this);
         PayforGoods.setOnClickListener(this);

        PingProcess();
    }



    public void PingProcess(){

        class WaitingforResponse extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loading = ProgressDialog.show(MainActivity.this, "Waiting for Response Ping...", "Please Wait...", false, false);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();


            }

            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, "");
                Request request = new Request.Builder()
                        .url("https://apphubstg.mobitel.lk/intstg/sb/mcashexternalapi/ping")
                        .post(body)
                        .addHeader("x-ibm-client-id", "2f0069c5-af70-4e1e-99d5-6bae07832d48")
                        .addHeader("content-type", "application/json")
                        .addHeader("accept", "application/json")
                        .build();


                try {
                Response response = client.newCall(request).execute();
                    //   Toast.makeText(MainActivity.this, "the response from server"+response, Toast.LENGTH_SHORT).show();
                    Log.e("Response from server","Response from server "+response);
                    success=response.code();
                    Message=response.message();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }
        WaitingforResponse WaitingforResponse = new WaitingforResponse();
        WaitingforResponse.execute();


    }

    @Override
    public void onClick(View v) {
        if(v==CashOut)
        {
            Intent intent = new Intent(MainActivity.this, CashOut.class);
            startActivity(intent);

        }if(v==CashIn){
            Intent intent = new Intent(MainActivity.this, CashIn.class);
            startActivity(intent);
        }if(v==PayforGoods)
        {
            Intent intent = new Intent(MainActivity.this, PayForGoodsActivity.class);
            startActivity(intent);
        }
    }
}

