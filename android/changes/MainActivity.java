/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.cloudvision;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Block;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MainActivity extends AppCompatActivity {
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAdARmRTaeqcmRsSgFIqsHDdD56EwgIxUk";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final int LOCATION_PERMISSIONS_REQUEST = 4;

    private TextView mImageDetails;
    private ImageView mMainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
        });

        mImageDetails = findViewById(R.id.image_details);
        mMainImage = findViewById(R.id.main_image);

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            //권한을 확인하고, 위치권한이 없다면
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSIONS_REQUEST);
            //이것이 권한 허용에 대한 요구 다이얼로그를 띄우는 것이며, 그 결과가 onRequestPermissionsResult 에 반영된다.
            //또한 Don't ask again(다시 묻지 않음)에 체크되어 거절된 이력이 있는 경우, 요구 다이얼로그를 띄우지 않으며 자동적으로 grantResult 가 PackageManager.PERMISSION_DENIED 가 된다.
        }
    }

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"), GALLERY_IMAGE_REQUEST);
        }
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",
                    getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider",
                    getCameraFile());
            uploadImage(photoUri);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
            case LOCATION_PERMISSIONS_REQUEST:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION)) // 권한 허용 여부에 대해 허락받는 요구 다이얼로그를 띄울 수 있는 지 확인한다.
                    {
                        // Deny (거부) 버튼을 눌렀던 이력이 있는 경우이면서 Don't ask again은 아닌 경우
                        Toast.makeText(this,"주변 AP를 스캔하기 위해 위치 권한이 필요합니다. 앱을 종료합니다", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //Deny & Don't Ask again 의 경우
                        Toast.makeText(this,"주변 AP를 스캔하기 위해 위치 권한이 필요합니다. 위치 권한을 수동설정 해주세요", Toast.LENGTH_SHORT).show();
                    }

                    // 권한을 허용받지 못한 경우이므로 어쨋든 2초 후 종료된다.
                    Handler ExitDelayHandler = new Handler();
                    ExitDelayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ActivityCompat.finishAffinity(MainActivity.this);
                            System.exit(0);
                        }
                    },2000);
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                        MAX_DIMENSION);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
            /**
             * We override this so we can inject important identifying fields into the HTTP
             * headers. This enables use of a restricted cloud platform API key.
             */
            @Override
            protected void initializeVisionRequest(VisionRequest<?> visionRequest) throws IOException {
                super.initializeVisionRequest(visionRequest);

                String packageName = getPackageName();
                visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
            }
        };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {
            {
                AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                // Add the image
                Image base64EncodedImage = new Image();
                // Convert the bitmap to a JPEG
                // Just in case it's a format that Android understands but Cloud Vision
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Base64 encode the JPEG
                base64EncodedImage.encodeContent(imageBytes);
                annotateImageRequest.setImage(base64EncodedImage);

                // add the features we want
                annotateImageRequest.setFeatures(new ArrayList<Feature>() {
                    {
                        Feature textDetection = new Feature();
                        textDetection.setType("TEXT_DETECTION");
                        // textlDetection.setMaxResults(MAX_LABEL_RESULTS);
                        add(textDetection);
                    }
                });

                // Add the list of one thing to the request
                add(annotateImageRequest);
            }
        });

        Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when
        // GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, String, String> labelDetectionTask = new LableDetectionTask(this,
                    prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message.append(labels.get(0).getDescription());
        } else {
            message.append("nothing");
        }

        return message.toString();
    }


    private class LableDetectionTask extends AsyncTask<Object, String, String> implements com.google.sample.cloudvision.LableDetectionTask {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;
        WifiMonitor br;
        private BlockingQueue q;    //추가됨

        LableDetectionTask(MainActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                wifiSearching(convertResponseToString(response));//백그라운드 작업으로 돌렸다
                                                    //이유는 onPostExecute에서는 wifi의 변화상태를
                                                    //기다리면 앱이 먹통이 되나 AsyncTask의 doInBackground
                                                    //내에서 진행을 하면 먹통이 되지 않는다.
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void wifiSearching(String result) {   //함수 이름 바꿔서 doInBackground에 넣은이유
                                            //위의 doInBackground의 주석에 명시
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {

                WifiManager wifienabler = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifienabler.setWifiEnabled(true);

                Log.d(result, "result");

                // parsing result text
                String delims = "[ :\n]+";
                String[] texts = result.split(delims);

                for (String text : texts) {
                    Log.d(text, "text");
                }

                // make Wifi ssid, key list
                ArrayList<String> ssid_list = new ArrayList<String>();
                ArrayList<String> key_list = new ArrayList<String>();

                ssid_list = makeSsidList();
                key_list = makeKeyList(texts);

                for (String ssid : ssid_list) {
                    Log.d(ssid, "ssid");
                }
                for (String key : key_list) {
                    Log.d(key, "key");
                }


                String ssid = "U+Net0A9B";
                String key = "5000334648";

                try {
                    Thread.sleep(7000); //기존의 postdelayed를 대체한다.
                }catch (Exception e) {
                    Log.d("KSS","sleep err");
                }
                register();
                multiConnect(ssid_list, key_list);

            }
        }


        protected void multiConnect(ArrayList<String> ssid_list, ArrayList<String> listKEY) { //여러번의 연결을 시도하게 2중 포문으로 묶음
            for(String key2: listKEY) {
                for (String ssid2 : ssid_list) {
                    Log.d("KSS",ssid2+ " " + key2);
                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    wifiConfig.SSID = String.format("\"%s\"", ssid2);
                    wifiConfig.preSharedKey = String.format("\"%s\"", key2);
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    // remember id

                    int netId = wifiManager.addNetwork(wifiConfig);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
                    if(checkAuth(ssid2)) return;    //바로아래 함수에서 authentication 체크 q에는 wifimonitor가 정보를 보내줌
                }
            }
        }
        protected Boolean checkAuth(String ssid) {
            Log.d("KSS", "before q");
            try {                                       //q.take()를 쓰기위해선 try-catch문이 요구가 된다.
                String strA = String.valueOf(q.take()); //인증성공시 wifimonitor에서 auth_suc이란 문자열을, 실패시 auth_fail이라는 문자열 담아줌.
                if (strA.equals("auth_suc")) {
                    Log.d("KSS", "onpost exit");
                    return true;

                }
                else publishProgress("We request to make connetion to " + ssid + ". But authentication was not accepted.");
            } catch (Exception e) {
                Log.d("KSS", String.valueOf(e));
            }
            //결과가 나올시 화면으로 피드백을 해준다.
            return false;
        }
        protected void register() {
            q = new ArrayBlockingQueue(50);
            br = new WifiMonitor(getApplicationContext(),q);
            IntentFilter i1 = new IntentFilter();
            i1.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            i1.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            try {
                registerReceiver(br,i1);
            }catch(Exception e) {
                Log.d("KSS", "no receiver wifimonitor");
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {// mainthread에서 돌아가는 함수 onpost중간에 실행되며 onpost의 쓰레드에서 돌아가지않고 메인에서돌아간다.
            MainActivity activity = mActivityWeakReference.get();
            //TextView imageDetail = activity.findViewById(R.id.image_details);
            mImageDetails.setText(values[0]);
            Log.d("KSS","onProgress");
        }
        @Override
        protected void onPostExecute(String result) {
            MainActivity activity = mActivityWeakReference.get();
            TextView imageDetail = activity.findViewById(R.id.image_details);
            imageDetail.setText(result);
            unregisterReceiver(br);
        }
    }

    public ArrayList<String> makeSsidList() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        BlockingQueue q2 = new ArrayBlockingQueue(50);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                q2.add("1");
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver,intentFilter);
        try {
            q2.take();
        }catch(Exception e) {
            Log.d("KSS","make SSIDlist err");
        }
        boolean success = wifiManager.startScan();

        if(success) {
            scanSuccess();
        } else {
            scanFailure();
        }

        List<ScanResult> results = wifiManager.getScanResults();
        ArrayList<String> ssid_list = new ArrayList<String>();

        for(ScanResult result : results ){
            ssid_list.add(result.SSID);
        }
        unregisterReceiver(wifiScanReceiver);
        return ssid_list;
    }

    private void scanSuccess() {
        Log.d("wifi", "scanSuccess");
    }

    private void scanFailure() {
        Log.d("wifi", "scanFailure");
    }

    private ArrayList<String> makeKeyList(String[] texts) {
        ArrayList<String> key_list = new ArrayList<String>();

        for (String text : texts) {
            if (text.length() <= 2 || hasHangul(text) == true)
                continue;

            String key = getKey(text);

            if (key != "") {
                key_list.add(key);
            }
        }

        return key_list;
    }

    // public static String getSsid(String text) {
    //     // extract ssid from google_vision text

    //     if (text.length() >= 2 && isDigit(text) == false)
    //         return text;

    //     return "";
    // }

    public static String getKey(String text) {
        // extract key from google_vision text
        if (text.length() >= 8) {
            String removed_text = removeSpecialCharacters(text);
            if (isAlpha(removed_text) == false && isAlnum(removed_text) == true)
                return text;
        }

        return "";
    }

    public static boolean isAlpha(String text) {
        for (char c : text.toCharArray())
            if (Character.isLetter(c) == false)
                return false;

        return true;
    }

    public static boolean isDigit(String text) {
        for (char c : text.toCharArray())
            if (Character.isDigit(c) == false)
                return false;

        return true;
    }

    public static boolean isAlnum(String text) {
        for (char c : text.toCharArray())
            if (!(Character.isLetter(c) == true || Character.isDigit(c) == true))
                return false;

        return true;
    }

    public static String removeSpecialCharacters(String text) {
        String special_characters = "~!@#$%&*?";

        for (char special_character : special_characters.toCharArray())
            text = text.replace(Character.toString(special_character), "");

        return text;
    }

    public static boolean hasHangul(String text) {
        if (text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
            return true;
        } else {
            return false;
        }
    }
    class WifiMonitor extends BroadcastReceiver { // 인증실패와 인증성공을 감지하는 Reciever
        Context context;
        private BlockingQueue q;
        public WifiMonitor(Context context,BlockingQueue q) {
                this.q = q;
                this.context = context;
        }
        @Override
        public void onReceive(Context context, Intent intent){
            String strAction = intent.getAction();
            //Log.d("checkWifi", "" + strAction);
            Toast t;
            String strA;
            NetworkInfo wifi;
            if (strAction.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ) {
                wifi = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if(wifi.getDetailedState()==
                        NetworkInfo.DetailedState.OBTAINING_IPADDR){ //DHCP 프로토콜에 의해 ip주소를 가져오고 있다? 인증된것
                    //q.add("you");
                    strA = "Obtaining!";;
                    t = Toast.makeText(context,
                            String.valueOf(wifi.getDetailedState()), Toast.LENGTH_SHORT);
                    Log.d("KSS","Obtainning");
                    t.show();
                    q.add("auth_suc");
                }
            } else if (strAction.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {;
                if (WifiManager.ERROR_AUTHENTICATING
                        ==intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,0)) {
                    strA="Authentication Error";                                //SUPPlicant에러가 authenticating으로 됐으면 실패했다고본다.
                    t = Toast.makeText(context, strA, Toast.LENGTH_SHORT);
                    Log.d("KSS","au_err");
                    //q.add("you");
                    t.show();
                    q.add("auth_fail");
                }
            }
        }
    }
}
