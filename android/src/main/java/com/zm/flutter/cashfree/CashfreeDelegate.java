package com.zm.flutter.cashfree;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gocashfree.cashfreesdk.CFPaymentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_APP_ID;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_CUSTOMER_EMAIL;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_CUSTOMER_NAME;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_CUSTOMER_PHONE;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_NOTIFY_URL;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_ORDER_AMOUNT;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_ORDER_ID;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_ORDER_NOTE;
import static com.gocashfree.cashfreesdk.CFPaymentService.PARAM_PAYMENT_MODES;

public class CashfreeDelegate implements ActivityResultListener {

    private final Activity activity;
    private Result pendingResult;
    private Map<String, Object> pendingReply;

    // Response codes for communicating with plugin
    private static final int CODE_PAYMENT_SUCCESS = 0;
    private static final int CODE_PAYMENT_ERROR = 1;


    public CashfreeDelegate(Activity activity) {
        this.activity = activity;
    }

    // 拉起CashFree
    void doPayment(Map<String, Object> arguments, Result result) throws JSONException {
        this.pendingResult = result;

        JSONObject options = new JSONObject(arguments);

        // 必填参数
        String token = options.getString("token");
        String stage = options.getString("stage");
        // 选填参数
        String color1 = options.optString("color1", "#333333");
        String color2 = options.optString("color2", "#FFFFFF");
        boolean hideOrderId = options.optBoolean("hideOrderId", false);

        // 订单参数
        Map<String, String> params = getOrderParams(options);

        CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
        cfPaymentService.setOrientation(0);
        cfPaymentService.doPayment(this.activity, params, token, stage, color1, color2, hideOrderId);
    }

    // 拉起CashFreeUpi
    void upiPayment(Map<String, Object> arguments, Result result) throws JSONException {
        this.pendingResult = result;

        JSONObject options = new JSONObject(arguments);

        // 必填参数
        String token = options.getString("token");
        String stage = options.getString("stage");

        // 订单参数
        Map<String, String> params = getOrderParams(options);

        CFPaymentService cfPaymentService = CFPaymentService.getCFPaymentServiceInstance();
        cfPaymentService.setOrientation(0);
        cfPaymentService.upiPayment(this.activity, params, token, stage);
    }

    private Map<String, String> getOrderParams(JSONObject options) throws JSONException {
        // 订单参数
        String appId = options.getString(PARAM_APP_ID);
        String orderId = options.getString(PARAM_ORDER_ID);
        Double orderAmount = options.getDouble(PARAM_ORDER_AMOUNT);
        String orderNote = options.optString(PARAM_ORDER_NOTE, "");
        String customerName = options.optString(PARAM_CUSTOMER_NAME, "");
        String customerPhone = options.optString(PARAM_CUSTOMER_PHONE, "");
        String customerEmail = options.optString(PARAM_CUSTOMER_EMAIL, "");
        String notifyUrl = options.optString(PARAM_NOTIFY_URL, "");
        String paymentModes = options.optString(PARAM_PAYMENT_MODES, "");

        Map<String, String> params = new HashMap<>();
        params.put(PARAM_APP_ID, appId);
        params.put(PARAM_ORDER_ID, orderId);
        params.put(PARAM_ORDER_AMOUNT, orderAmount.toString());
        params.put(PARAM_ORDER_NOTE, orderNote);
        params.put(PARAM_CUSTOMER_NAME, customerName);
        params.put(PARAM_CUSTOMER_PHONE, customerPhone);
        params.put(PARAM_CUSTOMER_EMAIL, customerEmail);
        params.put(PARAM_NOTIFY_URL, notifyUrl);
        params.put(PARAM_PAYMENT_MODES, paymentModes);

        return params;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Cashfree支付结果
        if (requestCode == CFPaymentService.REQ_CODE) {
            Log.d("Cashfree", "API Response : ");
            if (intent == null || intent.getExtras() == null) {
                return false;
            }

            Bundle bundle = intent.getExtras();
            for (String key : bundle.keySet()) {
                if (bundle.getString(key) != null) {
                    Log.d("Cashfree", key + " : " + bundle.getString(key));
                }
            }

            Map<String, Object> reply = new HashMap<>();
            Map<String, Object> data = new HashMap<>();

            //支付状态
            //txStatus : SUCCESS, FLAGGED, PENDING, FAILED, CANCELLED.
            String txStatus = bundle.getString("txStatus");
            String txMsg = bundle.getString("txMsg");
            String orderId = bundle.getString("orderId");
            String type = bundle.getString("type");
            if ("SUCCESS".equalsIgnoreCase(txStatus)) {
                String referenceId = bundle.getString("referenceId");
                String orderAmount = bundle.getString("orderAmount");
                String signature = bundle.getString("signature");
                String paymentMode = bundle.getString("paymentMode");
                Log.d("Cashfree SUCCESS", txStatus);
                reply.put("type", CODE_PAYMENT_SUCCESS);
                data.put("txStatus", txStatus);
                data.put("txMsg", txMsg);
                data.put("orderId", orderId);
                data.put("referenceId", referenceId);
                data.put("signature", signature);
                data.put("orderAmount", orderAmount);
                data.put("paymentMode", paymentMode);
                data.put("type", type);
                reply.put("data", data);
            } else {
                Log.d("Cashfree ERROR", txStatus);
                reply.put("type", CODE_PAYMENT_ERROR);
                data.put("txStatus", txStatus);
                data.put("txMsg", txMsg);
                data.put("type", type);
                reply.put("data", data);
            }

            sendReply(reply);
        }
        return false;
    }

    private void sendReply(Map<String, Object> data) {
        if (pendingResult != null) {
            pendingResult.success(data);
            pendingReply = null;
        } else {
            pendingReply = data;
        }
    }

    public void resync(Result result) {
        result.success(pendingReply);
        pendingReply = null;
    }
}
