package com.example.rspl_rahul.mcashrst;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.http2.Header;

/**
 * A placeholder fragment containing a simple view.
 */
public class CashOutFragment extends Fragment implements View.OnClickListener {
    int success;
    String Message,otps,merchantTransId,test,reference,responsecode;
    EditText Pincode,Transactionamt,CustomerMobileNo,otpCashOut;
    Button Submit,btnOK;
    String Transamt,custmerno;
    AlertDialog cashOut;
    AlertDialog.Builder dialog;
    JSONObject jsonObject;
    public CashOutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_cash_out, container, false);
        Pincode=(EditText)view.findViewById(R.id.pincode);
        Transactionamt=(EditText)view.findViewById(R.id.transactionamount);
        CustomerMobileNo=(EditText)view.findViewById(R.id.customermobilenumber);
        Submit=(Button)view.findViewById(R.id.cashOutSubmit);
        Submit.setOnClickListener(this);

       return view;
    }

    private void CheckwalletbalanceDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View cashInProcess = inflater.inflate(R.layout.gettingotpcashout, null);
        dialog = new AlertDialog.Builder(getContext());
        otpCashOut=(EditText)cashInProcess.findViewById(R.id.otp_cashOut);

        btnOK = (Button) cashInProcess.findViewById(R.id.cashOutOk);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otps= otpCashOut.getText().toString();
                cashOut.dismiss();
                CashOutProcess();

            }
        });
        cashOut = dialog.create();
        cashOut.setView(cashInProcess);
        cashOut.setTitle("Enter OTP");

        cashOut.show();
        cashOut.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        if (v==Submit){
            if (Pincode.getText().toString().equals("") || Transactionamt.getText().toString().equals("") || CustomerMobileNo.getText().toString().equals(""))
            {
                Toast.makeText(getContext(), "Please fill the mandatory fields", Toast.LENGTH_SHORT).show();
               return;

            }
            else {
                Transamt=Transactionamt.getText().toString();
                custmerno=CustomerMobileNo.getText().toString();
                invoiceno();
                ValidateCustomerprocess();

            }
        }
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
    public void ValidateCustomerprocess(){
        class WaitingforResponse extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loading = ProgressDialog.show(getContext(), "Waiting for Response  Validate Customer...", "Please Wait...", false, false);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Log.e("Message",""+success  + "   "+Message);
                try{
                    if (responsecode.matches("2007")) {

                        CheckwalletbalanceDialog();
                    }
                    else{

                        OutPutMessage();

                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");

                String json=ValidateCustomerJson();
                RequestBody body = RequestBody.create(mediaType,json);
                Request request = new Request.Builder()
                        .url("https://apphubstg.mobitel.lk/intstg/sb/mcashexternalapi/validateCustomer")
                        .post(body)
                        .addHeader("x-ibm-client-id", "2f0069c5-af70-4e1e-99d5-6bae07832d48")
                        .addHeader("content-type", "application/json")
                        .addHeader("accept", "application/json")
                        .build();


                try {
                    Response response = client.newCall(request).execute();
                    test = response.body().string();
                    if (response.isSuccessful()) {
                        success = response.code();
                        Headers responseHeaders = response.headers();
                        for (int i = 0; i < responseHeaders.size(); i++) {
                            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }

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
                    }else {
                        Log.e("SocketTimeOut",""+response);
                        System.out.println(test);
                        try {
                            jsonObject= new JSONObject(test);

                            Message=(String)jsonObject.get("httpMessage");

                            /*responsecode=(String)jsonObject.get("response-code");
                            reference= (String) jsonObject.get("reference");*/
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
    public void OutPutMessage(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View PayForGoodsProcess = inflater.inflate(R.layout.responsedisplaylayout, null);
        dialog = new AlertDialog.Builder(getContext());
        TextView responseText=(TextView)PayForGoodsProcess.findViewById(R.id.responseText);
        Button OKBtn=(Button)PayForGoodsProcess.findViewById(R.id.Okbutton);
        cashOut = dialog.create();
        cashOut.setTitle("          Response from Server");

        try {
            String s = Message;
            // System.out.println();
            Toast.makeText(getContext(), ""+s.substring(s.lastIndexOf(',') + 1), Toast.LENGTH_SHORT).show();
            responseText.setText(s.substring(s.lastIndexOf(',') + 1));
        }catch (Exception e){
            e.printStackTrace();
        }
        cashOut.setView(PayForGoodsProcess);
        cashOut.show();
        cashOut.setCanceledOnTouchOutside(false);
        OKBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashOut.dismiss();
                getActivity().finish();
            }
        });
    }
    @Override
    public String toString() {
        return Message;
    }

    public void CashOutProcess(){
        class WaitingforResponse extends AsyncTask<Void, Void, String> {
            ProgressDialog loading;
            int success;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                loading = ProgressDialog.show(getContext(), "Waiting for Response Cash Out...", "Please Wait...", false, false);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                try{
                    if (responsecode.matches("1000")) {
                        OutPutMessage();

                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            protected String doInBackground(Void... params) {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");

                String json=CashOutJson();
                RequestBody body = RequestBody.create(mediaType,json);
                Request request = new Request.Builder()
                        .url("https://apphubstg.mobitel.lk/intstg/sb/mcashexternalapi/cashOut")
                        .post(body)
                        .addHeader("x-ibm-client-id", "2f0069c5-af70-4e1e-99d5-6bae07832d48")
                        .addHeader("content-type", "application/json")
                        .addHeader("accept", "application/json")
                        .build();


                try {
                    Response response = client.newCall(request).execute();
                    test = response.body().string();
                    if (response.isSuccessful()) {
                        success = response.code();
                        Headers responseHeaders = response.headers();
                        for (int i = 0; i < responseHeaders.size(); i++) {
                            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }

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
                    }else {
                        Log.e("SocketTimeOut",""+response);
                        System.out.println(test);
                        try {
                            jsonObject= new JSONObject(test);
                            Message=(String)jsonObject.get("httpMessage");

                            /*responsecode=(String)jsonObject.get("response-code");
                            reference= (String) jsonObject.get("reference");*/
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

    String ValidateCustomerJson() {
        return " \n" +
                "{\n" +
                "  \"merchant-data\": {\n" +
                "    \"pin-code\": \""+Pincode.getText().toString()+"\",\n" +
                "    \"merchant-transaction-id\": \""+merchantTransId+"\",\n" +
                "    \"merchant-id\": \"ASIR00-5912\",\n" +
                "    \"mobile-number\": \"0712785277\"\n" +
                "  },\n" +
                "  \"customer-data\": {\n" +
                "    \"note\": \"Validate CASH_OUT\",\n" +
                "    \"transaction-amount\":"+Transactionamt.getText().toString()+",\n" +
                "    \"customer-mobile-number\": \""+CustomerMobileNo.getText().toString()+"\",\n" +
                "    \"merchant-outlet-code\": \"01\"\n" +
                "  },\n" +
                "  \"validate-data\": {\n" +
                "    \"validate-for\": \"CASH_OUT\"\n" +
                "  }\n" +
                "}";
      /*return"{\n" +
              "  \"merchant-data\": {\n" +
              "    \"pin-code\": \"1111\",\n" +
              "    \"merchant-transaction-id\": \"val1\",\n" +
              "    \"merchant-id\": \"ASIR00-5912\",\n" +
              "    \"mobile-number\": \"0712785148\"\n" +
              "  },\n" +
              "  \"customer-data\": {\n" +
              "    \"note\": \"val1\",\n" +
              "    \"transaction-amount\": 32,\n" +
              "    \"customer-mobile-number\": \"0711070171\",\n" +
              "    \"merchant-outlet-code\": \"01\"\n" +
              "  },\n" +
              "  \"validate-data\": {\n" +
              "    \"validate-for\": \"COUNTER_PAYMENTS\"\n" +
              "  }\n" +
              "}";*/
    }
    String CashOutJson() {
     /*   return " {\n" +
                "  \"merchant-data\": {\n" +
                "    \"pin-code\": \"'"+Pincode.getText().toString()+"'\",\n" +
                "    \"merchant-transaction-id\": \""+merchantTransId+"\",\n" +
                "    \"merchant-id\": \"ASIR00-5912\",\n" +
                "    \"mobile-number\": \"0712785148\"\n" +
                "  },\n" +
                "  \"transaction-data\": {\n" +
                "    \"note\": \"PAY for goods Process\",\n" +
                "    \"transaction-amount\":"+Transactionamt.getText().toString()+",\n" +
                "    \"customer-mobile-number\": \""+CustomerMobileNo.getText().toString()+"\",\n" +
                "    \"merchant-outlet-code\": \"01\"\n" +
                "  },\n" +
                "  \"otp-details\": {\n" +
                "    \"customer-otp\": \"'"+otps+"'\",\n" +
                "    \"reference\": \""+reference+"\"\n" +
                "  }\n" +
                "}";*/


        return"{\n" +
                "  \"merchant-data\": {\n" +
                "    \"pin-code\": \""+Pincode.getText().toString()+"\",\n" +
                "    \"merchant-transaction-id\": \""+merchantTransId+"\",\n" +
                "    \"merchant-id\": \"ASIR00-5912\",\n" +
                "    \"mobile-number\": \"0712785277\"\n" +
                "  },\n" +
                "  \"transaction-data\": {\n" +
                "    \"note\": \"Cash Out Process\",\n" +
                "    \"transaction-amount\":"+Transactionamt.getText().toString()+",\n" +
                "    \"customer-mobile-number\": \""+CustomerMobileNo.getText().toString()+"\",\n" +
                "    \"merchant-outlet-code\": \"01\"\n" +
                "  },\n" +
                "  \"otp-details\": {\n" +
                "    \"customer-otp\": \""+otps+"\",\n" +
                "    \"reference\": \""+reference+"\"\n" +
                "  }\n" +
                "}";
    }


}

