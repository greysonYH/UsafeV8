package com.example.greyson.test1.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

/**
 * Created by greyson on 29/3/17.
 */

public class AppInitService extends IntentService {
    private static final String ACTION_INIT_WHEN_APP_CREATE = "service.action.INIT";
    private static final String DEFULT_LOG = "DOTA_FANS";

    public AppInitService() {
        super("AppInitService");
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, AppInitService.class);
        intent.setAction(ACTION_INIT_WHEN_APP_CREATE);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_INIT_WHEN_APP_CREATE.equals(action)) {
                handleInit();
            }
        }

    }

    private void handleInit() {

    }
}
