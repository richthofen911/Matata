package project.richthofen911.dexclass;

import android.app.Activity;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import dalvik.system.DexClassLoader;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("store path", getFilesDir().toString());
        Button btnFire = (Button) findViewById(R.id.btn_fire);

        btnFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dexFilePath = new File(getFilesDir().toString() + File.separator + "test.dex");
                DexClassLoader dexClassLoader = new DexClassLoader(dexFilePath.getAbsolutePath(), getCacheDir().toString(),
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

}
