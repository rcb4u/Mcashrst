package com.example.rspl_rahul.mcashrst;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CashIn extends AppCompatActivity {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    Response response;
    JSONObject jsonObject;
    String Message,test,responsecode,reference,merchantTransId;
    int success;
    EditText Pincode,Transactionamt,CustomerMobileNo;
    Button Submit,btnOK;
    AlertDialog cashIn;
    TextView Amount,Rs;
    AlertDialog.Builder dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Pincode=(EditText)findViewById(R.id.pincodecashIn);
        Transactionamt=(EditText)findViewById(R.id.transactionamountcashin);
        CustomerMobileNo=(EditText)findViewById(R.id.customermobilenumbercashin);
        Submit=(Button)findViewById(R.id.cashInSubmit);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Pincode.getText().toString().equals("") || Transactionamt.getText().toString().equals("") || CustomerMobileNo.getText().toString().equals(""))
                {
                    Toast.makeText(CashIn.this, "Please fill the mandatory fields", Toast.LENGTH_SHORT).show();
                    return;

                }else {
                    invoiceno();
                    CashInProcess();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void CashInProcess(){
        class WaitingforResponse extends AsyncTask<Void, Void, String> {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(CashIn.this, "Waiting for Response Cash In...", "Please Wait...", false, false);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                progressDialog.dismiss();
                try{
                    if (responsecode.matches("1000")) {

                        CheckwalletbalanceDialog();
                    }
                    else{
                        runOnUiThread(new Runnable(){

                            @Override
                            public void run(){
                                //update ui here
                                // display toast here
                                Toast.makeText(CashIn.this, ""+Message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        OutPutMessage();
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                catch (Exception e)
                { e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");

                String json=CashInJson();
                RequestBody body = RequestBody.create(mediaType,json);
                Request request = new Request.Builder()
                        .url("https://apphubstg.mobitel.lk/intstg/sb/mcashexternalapi/cashIn")
                        .post(body)
                        .addHeader("x-ibm-client-id", "2f0069c5-af70-4e1e-99d5-6bae07832d48")
                        .addHeader("content-type", "application/json")
                        .addHeader("accept", "application/json")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        success = response.code();

                        Headers responseHeaders = response.headers();
                        for (int i = 0; i < responseHeaders.size(); i++) {
                            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }
                        test = response.body().string();
                        System.out.println(test);
                        try {
                            jsonObject= new JSONObject(test);
                            Log.e(" transaction-amount ",""+jsonObject.get("transaction-amount"));

                            Message=(String)jsonObject.get("response");
                            responsecode=(String)jsonObject.get("response-code");
                            reference= (String) jsonObject.get("reference");
                            //"transaction-amount":"250.0","response":"SOAP validation error","response-code":"500","merchant-transaction-id":"Mer-1495789549702"}
                            // {"transaction-type":"COUNTER_SALES","reference":"20170526185553511726","transaction-amount":"500.0","response":"OTP request send to customer, One Time Password(OTP) send to customer","response-code":"2007","merchant-transaction-id":"Mer-1495805183373"}
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

        }
        WaitingforResponse WaitingforResponse = new WaitingforResponse();
        WaitingforResponse.execute();

    }

    String CashInJson() {
        return "{\n" +
                "  \"transaction-data\": {\n" +
                "    \"note\": \"Cash In Process\",\n" +
                "    \"transaction-amount\":"+Transactionamt.getText().toString()+",\n" +
                "    \"customer-mobile-number\": \""+CustomerMobileNo.getText().toString()+"\",\n" +
                "    \"merchant-outlet-code\": \"01\"\n" +
                "  },\n" +
                "  \"merchant-data\": {\n" +
                "    \"pin-code\": \""+Pincode.getText().toString()+"\",\n" +
                "    \"merchant-transaction-id\": \""+merchantTransId+"\",\n" +
                "    \"merchant-id\": \"ASIR00-5912\",\n" +
                "    \"mobile-number\": \"0712785277\"\n" +
                "  }\n" +
                "}";
    }
    public void invoiceno() {
        Long  Value = System.currentTimeMillis();
        String  result = Long.toString(Value);
        String  imei = "Mer-";
        Log.e("ImeiNo", imei.toString());
        String   x_imei = imei.toString();
        String x1 = x_imei.replace("[", "").replace("]", "").concat(result);
        Log.e("X1_imei is :", x1);
        merchantTransId = x1;
    }
    private void CheckwalletbalanceDialog() {


        LayoutInflater inflater = getLayoutInflater();
        final View cashInProcess = inflater.inflate(R.layout.popup_cash_in, null);
        dialog = new AlertDialog.Builder(CashIn.this);
        Amount = (TextView) cashInProcess.findViewById(R.id.performCashIn);
        Rs=(TextView)cashInProcess.findViewById(R.id.rs);
        btnOK = (Button)cashInProcess.findViewById(R.id.cashInOk);
        cashIn = dialog.create();
        cashIn.setView(cashInProcess);
        cashIn.setTitle("Processed Cash In");
        Amount.setText(Transactionamt.getText().toString());
        cashIn.show();
        cashIn.setCanceledOnTouchOutside(false);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashIn.dismiss();
                finish();
            }
        });
    }
    public void OutPutMessage(){

        LayoutInflater inflater = LayoutInflater.from(this);
        final View PayForGoodsProcess = inflater.inflate(R.layout.responsedisplaylayout, null);
        dialog = new AlertDialog.Builder(this);
        TextView responseText = (TextView) PayForGoodsProcess.findViewById(R.id.responseText);
        Button OKBtn = (Button) PayForGoodsProcess.findViewById(R.id.Okbutton);
        cashIn = dialog.create();
        cashIn.setTitle("          Response from Server");
        try {
            String s = Message;
            // System.out.println();
            responseText.setText(s.substring(s.lastIndexOf(',') + 1));
        }catch (Exception e){
            e.printStackTrace();
        }
        cashIn.setView(PayForGoodsProcess);
        cashIn.show();
        cashIn.setCanceledOnTouchOutside(false);
        OKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashIn.dismiss();
                finish();
            }
        });
    }
}
