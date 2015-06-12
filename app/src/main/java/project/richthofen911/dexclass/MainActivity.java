package project.richthofen911.dexclass;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity {

    String downloadResult = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("store path", getFilesDir().toString());
        Button btnFire = (Button) findViewById(R.id.btn_fire);
        Button btnDownload = (Button) findViewById(R.id.btn_download);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadFile downloadFile = new DownloadFile();
                downloadFile.execute();
            }
        });

        btnFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dexFilePath = new File(getFilesDir().toString() + File.separator + "test.dex");
                DexClassLoader dexClassLoader = new DexClassLoader(dexFilePath.getAbsolutePath(), getFilesDir().toString(),
                        null, getClassLoader());
                Class libProviderClazz = null;
                try{
                    libProviderClazz = dexClassLoader.loadClass("project.richthofen911.dexclass.DynamicTest");
                    IDynamic lib = (IDynamic) libProviderClazz.newInstance();
                    Toast.makeText(getApplicationContext(), lib.helloWorld(), Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    private void downloadDexFile(String targetUrl, String fileName){
       // String path = "file";
        //String fileName = "test.dex";
        OutputStream output = null;
        try{
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //String savePath = Environment.getExternalStorageDirectory() + "";
            String pathName = getFilesDir() + File.separator + fileName;

            File file = new File(pathName);
            InputStream inputStream = conn.getInputStream();
            if(file.exists()){
                Log.e("File already exist", "");
                downloadResult = "File already exist";
            }else{
                //String dir = SDCard + "/" + path;
                //new File(dir).mkdir();
                file.createNewFile();
                output = new FileOutputStream(file);
                byte[] buffer = new byte[676];
                while (inputStream.read(buffer) != -1){
                    output.write(buffer);
                }
                output.flush();
                Log.e("arrive here", "");
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            downloadResult = "fail";
        }catch (IOException e){
            e.printStackTrace();
            downloadResult = "fail";
        }finally {
            if(output != null){
                try{
                    output.close();
                    //Toast.makeText(getApplicationContext(), "download success", Toast.LENGTH_SHORT).show();
                    Log.e("download", "success");
                    downloadResult = "success";
                }catch (IOException e){
                    //Toast.makeText(getApplicationContext(), "download fail", Toast.LENGTH_SHORT).show();
                    Log.e("download", "fail");
                    downloadResult = "fail";
                    e.printStackTrace();
                }
            }
        }
    }

    public class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            downloadDexFile("http://104.167.102.201/test.dex", "test.dex");
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            Toast.makeText(getApplicationContext(), downloadResult, Toast.LENGTH_SHORT).show();
        }
    }

}
