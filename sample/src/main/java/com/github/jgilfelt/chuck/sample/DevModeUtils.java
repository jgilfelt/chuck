package com.github.jgilfelt.chuck.sample;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class DevModeUtils {

    public static void browseSQLiteDatabase(Context context) {
        if (isIntentResolvable(context, getSQLiteDebuggerAppIntent("/"))) {
            String path = extractDatabase(context);
            if (path != null) {
                Intent intent = getSQLiteDebuggerAppIntent(path);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Unable to extract database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Unable to resolve a SQLite Intent", Toast.LENGTH_SHORT).show();
        }
    }

    private static String extractDatabase(Context context) {
        try {
            File external = context.getExternalFilesDir(null);
            File data = Environment.getDataDirectory();
            if (external != null && external.canWrite()) {
                String dataDBPath = "data/" + context.getPackageName() + "/databases/chuck.db";
                String extractDBPath = "chuckdb.temp";
                File dataDB = new File(data, dataDBPath);
                File extractDB = new File(external, extractDBPath);
                if (dataDB.exists()) {
                    FileChannel in = new FileInputStream(dataDB).getChannel();
                    FileChannel out = new FileOutputStream(extractDB).getChannel();
                    out.transferFrom(in, 0, in.size());
                    in.close();
                    out.close();
                    return extractDB.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Intent getSQLiteDebuggerAppIntent(String path) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setData(Uri.parse("sqlite:" + path));
        return intent;
    }

    private static boolean isIntentResolvable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }

}
