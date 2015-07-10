package project.richthofen911.matata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            context.startService(new Intent(context, MyService.class));
        }
        Intent service = new Intent(context, MyService.class);
        context.startService(service);
    }
}
