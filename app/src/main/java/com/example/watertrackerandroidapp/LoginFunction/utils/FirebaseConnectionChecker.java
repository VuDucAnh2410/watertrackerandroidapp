package com.example.watertrackerandroidapp.LoginFunction.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseConnectionChecker {
    private static final String TAG = "FirebaseConnChecker";
    private static final long CONNECTION_TIMEOUT = 10000; // 10 seconds

    public interface ConnectionCallback {
        void onConnected();
        void onTimeout();
        void onNoInternet();
    }

    public static void checkConnection(Context context, ConnectionCallback callback) {
        // Kiểm tra kết nối internet
        if (!isNetworkAvailable(context)) {
            Log.e(TAG, "No internet connection");
            if (callback != null) {
                callback.onNoInternet();
            }
            return;
        }

        // Tạo handler cho timeout
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "Firebase connection timeout");
                if (callback != null) {
                    callback.onTimeout();
                }
            }
        };

        // Đặt timeout
        handler.postDelayed(timeoutRunnable, CONNECTION_TIMEOUT);

        // Kiểm tra kết nối Firebase
        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Hủy timeout
                handler.removeCallbacks(timeoutRunnable);

                Boolean connected = snapshot.getValue(Boolean.class);
                if (connected != null && connected) {
                    Log.d(TAG, "Connected to Firebase");
                    if (callback != null) {
                        callback.onConnected();
                    }
                } else {
                    Log.e(TAG, "Not connected to Firebase");
                    if (callback != null) {
                        callback.onTimeout();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Hủy timeout
                handler.removeCallbacks(timeoutRunnable);

                Log.e(TAG, "Firebase connection check cancelled: " + error.getMessage());
                if (callback != null) {
                    callback.onTimeout();
                }
            }
        });
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}