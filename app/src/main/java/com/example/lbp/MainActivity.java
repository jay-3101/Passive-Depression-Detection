package com.example.lbp;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.util.Iterator;
import com.example.lbp.NotificationHelper;
import android.content.Context;
import android.widget.Toast;
import java.nio.charset.StandardCharsets;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;



public class MainActivity extends AppCompatActivity {

    private TextView outputTextView;
    private TextView audioView;
    private Button buttonPickFile;
    private Button buttonPickFolder;

    private Handler handler;
    private static final long INTERVAL = 15000; // 60 seconds in milliseconds
    private Uri currentFileUri;
    private Uri audiFile;

    private Uri folderUri;

    private Uri loki;
    static int cur_len=0;
    static int notfCounter =0;
    static int smsCounter =0;
    Context context = this;
    static String phone ="+919044465278";
    static String userName = "Alice";
    static String helperName ="Bob";

    static double scoreThreshold = 0.4;
    static int smsThreshold = 5;
    static int notificationThreshold = 3;

    private static final int REQUEST_CODE_PICK_FILE = 1;
    private static final int REQUEST_CODE_PICK_FOLDER = 2;

    static  String hardCodedUrl="content://com.android.externalstorage.documents/document/primary%3AAndroid%2Fdata%2Fcom.abifog.lokiboard%2Ffiles%2Flokiboard_files_22_04_2024.txt";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputTextView = findViewById(R.id.outputTextView);
        audioView = findViewById(R.id.audioView);
        buttonPickFile = findViewById(R.id.buttonPickFile);
        buttonPickFolder = findViewById(R.id.buttonPickFolder);

        buttonPickFile.setOnClickListener(v -> pickFile());
        buttonPickFolder.setOnClickListener(v -> pickFolder());
        audioView.setText("This is a test message for audioView");
        outputTextView.setText("This is a test message for TextView");
//        Button sendSmsButton = findViewById(R.id.sendSmsButton);
//        sendSmsButton.setOnClickListener(v -> startThread());


        // Initialize handler
        handler = new Handler();
        // Start the repeating task
        handler.postDelayed(runnable, INTERVAL);
    }



    private void pickFile() {
        cur_len=0;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain"); // Specify the MIME type for audio files
        startActivityForResult(Intent.createChooser(intent, "Choose Audio File"), REQUEST_CODE_PICK_FILE);
    }

    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_FOLDER);
    }

//    private void startThread() {
//        try{
//            String TAG ="2912";
//
//            Log.d(TAG, "startThread: ");
//            loki = Uri.parse(hardCodedUrl);
//            String fileContent = readFileContent(loki);
//            if (fileContent != null && !fileContent.isEmpty()) {
//                new DepressionAnalysisTask().execute(fileContent);
//            } else {
//                outputTextView.setText("Error: Empty file content.");
//            }
//
//        }catch(Exception e){
//
//        }
//    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                currentFileUri = data.getData();
                String TAG="220";
                Log.d(TAG, currentFileUri.toString());
                try {
                    String fileContent = readFileContent(currentFileUri);
                    if (fileContent != null && !fileContent.isEmpty()) {
                        new DepressionAnalysisTask().execute(fileContent);
                    } else {
                        outputTextView.setText("Error: Empty file content.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    outputTextView.setText("Error reading file.");
                }
            }

                else if (requestCode == REQUEST_CODE_PICK_FOLDER && data != null) {

                String TAG = "220 ";
                Log.d(TAG, data.getData().toString());
                    audiFile = data.getData();
                new AudDepressionAnalysis().execute(audiFile.toString());

                }
        }
    }

    private String readFileContent(Uri uri) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }


    private static String removeSubstringsInSquareBrackets(String input) {
        StringBuilder output = new StringBuilder();
        int len = input.length();
        boolean inBrackets = false;

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '[') {
                inBrackets = true;
                continue;
            }
            if (c == ']') {
                inBrackets = false;
                continue;
            }
            if (!inBrackets) {
                output.append(c);
            }
        }

        return output.toString();
    }



    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (currentFileUri != null) {
                try {
                    String fileContent = readFileContent(currentFileUri);
                    if (fileContent != null && !fileContent.isEmpty()) {
                        new DepressionAnalysisTask().execute(fileContent);
                    }
                    new AudDepressionAnalysis().execute(audiFile.toString());

                } catch (IOException e) {
                    e.printStackTrace();
                    outputTextView.setText("Error reading file.");
                }
            }
            // Schedule the task to run again after INTERVAL milliseconds
            handler.postDelayed(this, INTERVAL);
        }
    };

    private class AudDepressionAnalysis extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {

                if(smsCounter>smsThreshold){
                    sendSMS();
                    smsCounter=0;
                }
                return queryAudio(Uri.parse(params[0]));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        @Override
//            audioView.setText(result);\
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        StringBuilder output = new StringBuilder();
                        String sad="sadness";
                        String fear="fear";
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String label = jsonObject.getString("label");
                            double score = jsonObject.getDouble("score");
                            output.append(label).append(" : ").append(score).append("\n");
                            if(label.equals(sad)||label.equals(fear)){
                                if(score>0.5){
                                    notfCounter++;
                                    smsCounter++;
                                }
                            }
                        }

                        if(notfCounter>=notificationThreshold){
                            notfCounter=0;
                            NotificationHelper.showNotification(context, "Hey "+userName, "Let's play some games");
                        }

                        // Display the formatted output
                        audioView.setText(output.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        audioView.setText("Error parsing JSON");
                    }
                } else {
                    audioView.setText("Error: Result is null");
                }
            }
    }

    private class DepressionAnalysisTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {



                if(smsCounter>smsThreshold){
                    sendSMS();
                    smsCounter=0;
                }

                String text = params[0].substring(cur_len);
                String text2 = params[0];

                String proc_text = removeSubstringsInSquareBrackets(text2);
                String TAG = "Do in backgroung -- string ";
                Log.d(TAG, proc_text);
                if(proc_text.length()<=5){
                    outputTextView.setText("No new data");
                    return  null;
                }
                cur_len=params[0].length();
                return query(proc_text);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }



        @Override

        protected void onPostExecute(String result) {
            if (result != null) {
                try {

                    JSONArray outerArray = new JSONArray(result);
                    JSONArray innerArray = outerArray.getJSONArray(0);
                    StringBuilder output = new StringBuilder();
                    String notDep = "Not Depressed";
                    String dep = "Depressed";
                    for (int i = 0; i < innerArray.length(); i++) {
                        JSONObject jsonObject = innerArray.getJSONObject(i);
                        String label = jsonObject.getString("label");
                        double score = jsonObject.getDouble("score");
                        if(label.equals(dep)){
//                            String TAG = "Counter viewer -- string ";
//                            Log.d(TAG, String.valueOf(smsCounter));
                            if(score>scoreThreshold){
                                smsCounter++;
                                notfCounter++;
                            }
                        }
                        output.append(label).append(" : ").append(score).append("\n");
                    }
//                    sendSMS();
//                    String TAG = "Counter viewer -- string ";
//                    Log.d(TAG, String.valueOf(counter));

                    if(notfCounter>=notificationThreshold){
                        notfCounter=0;
                        NotificationHelper.showNotification(context, "Hey "+userName, "Let's play some games");
                    }
                    // Display the formatted output
                    outputTextView.setText(output.toString());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing error
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                outputTextView.setText("Error occurred while fetching data.");
            }
        }

    }




    private String query(String input) throws Exception {
        String API_URL = "https://api-inference.huggingface.co/models/sanskar/DepressionAnalysis";
        String API_TOKEN = "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";
//        sendSMS();

        URL url = new URL(API_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
        con.setRequestProperty("Content-Type", "application/json");

        String payload = "{\"inputs\": \"" + input + "\"}";

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(payload);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        Log.d("DepressionAnalysis", "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Check if the response is a JSON object or array
        String jsonResponseString = response.toString();
        try {
            // Try parsing the response as a JSONObject
            JSONObject jsonResponse = new JSONObject(jsonResponseString);
            return jsonResponse.toString();
        } catch (JSONException e) {
            // If parsing as JSONObject fails, try parsing as JSONArray
            try {
                JSONArray jsonArray = new JSONArray(jsonResponseString);
                // Handle JSONArray here if needed
                // For example, you might want to loop through the array and extract values
                return jsonArray.toString();
            } catch (JSONException ex) {
                // If parsing as JSONArray also fails, log the error and return null
                Log.e("DepressionAnalysis", "Error parsing response as JSON", ex);
                return null;
            }
        }
    }

    private String queryAudio(Uri audioUri) throws Exception {
        String API_URL = "https://api-inference.huggingface.co/models/harshit345/xlsr-wav2vec-speech-emotion-recognition ";
        String API_TOKEN = "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";

        String TAG = "Aud dep analysis-- string ";
        Log.d(TAG,"query audio");
        HttpURLConnection con = null;
        try {
            URL url = new URL(API_URL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + API_TOKEN);
            con.setRequestProperty("Content-Type", "audio/mpeg");
            con.setDoOutput(true);

            try (OutputStream outputStream = con.getOutputStream();
                 InputStream inputStream = getContentResolver().openInputStream(audioUri)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
                 TAG = "Inside calling api-- string ";
                Log.d(TAG, response.toString());
                return response.toString();
            } else {
                throw new IOException("HTTP request failed with response code: " + responseCode);
            }
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
    private void sendSMS() throws Exception {
        String accountSid = "AC2d2b5037eccc113744102074d8c6f759";
        String authToken = "e0257a91a28feaa60f487bcbb0b90e94";
        String apiUrl = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

        // Hardcoded values
        String fromNumber = "+12513095765"; // Your Twilio phone number
        String toNumber = phone; // Recipient's phone number
        String message = "Hello," +helperName+" your friend "+userName+ " needs your help"; // Message content

        URL url = new URL(apiUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        String credentials = accountSid + ":" + authToken;
        String authHeaderValue = "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        con.setRequestProperty("Authorization", authHeaderValue);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String postData = "From=" + fromNumber + "&To=" + toNumber + "&Body=" + message;
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(postData);
        writer.flush();
        writer.close();
        os.close();

        int responseCode = con.getResponseCode();
        Log.d("Twilio", "Response Code: " + responseCode);

        InputStream inputStream = new BufferedInputStream(con.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            Log.d("Twilio", "Message sent successfully!");
        } else {
            Log.e("Twilio", "Error: " + response.toString());
        }
    }

}