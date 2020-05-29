package com.zm.flutter.cashfree;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CashfreePlugin
 */
public class CashfreePlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;

    private CashfreeDelegate cashfreeDelegate;

    private ActivityPluginBinding pluginBinding;

    private static String CHANNEL_NAME = "cashfree";

    public CashfreePlugin() {
    }

    /**
     * Plugin registration for Flutter version < 1.12
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_NAME);
        channel.setMethodCallHandler(new CashfreePlugin(registrar));
    }

    /**
     * Constructor for Flutter version < 1.12
     *
     * @param registrar
     */
    private CashfreePlugin(Registrar registrar) {
        this.cashfreeDelegate = new CashfreeDelegate(registrar.activity());
        registrar.addActivityResultListener(cashfreeDelegate);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), CHANNEL_NAME);
        channel.setMethodCallHandler(this);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "doPayment":
                try {
                    cashfreeDelegate.doPayment((Map<String, Object>) call.arguments, result);
                } catch (JSONException e) {
                    Log.d("Cashfree doPayment error={}", e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "upiPayment":
                try {
                    cashfreeDelegate.upiPayment((Map<String, Object>) call.arguments, result);
                } catch (JSONException e) {
                    Log.d("Cashfree upiPayment error={}", e.getMessage());
                    e.printStackTrace();
                }
                break;
            case "resync":
                cashfreeDelegate.resync(result);
                break;
            default:
                result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        this.cashfreeDelegate = new CashfreeDelegate(binding.getActivity());
        this.pluginBinding = binding;
        binding.addActivityResultListener(cashfreeDelegate);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        pluginBinding.removeActivityResultListener(cashfreeDelegate);
        pluginBinding = null;
    }
}
