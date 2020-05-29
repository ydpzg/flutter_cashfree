# Cashfree PG Flutter SDK 

# author

zhaolong<zhaoyuen123@126.com>

# Integration Steps

## Step 1: Add Dependency


```yaml
  cashfree: 0.0.1
```

<br/>

## Step 3: Add permissions (Android)

The Cashfree PG SDK requires that you add the INTERNET permission in your `Android Manifest` file.

```xml
<manifest ...>
    <uses-permission android:name="android.permission.INTERNET" />
<application ...>
```
## Step 4: Add plist value (iOS)

Opt-in to the embedded views preview by adding a boolean property to the app's Info.plist file with the key io.flutter.embedded_views_preview and the value YES. This is required by the flutter_webview plugin.

```plist
    <key>io.flutter.embedded_views_preview</key>
    <true/>
```

<br>

## Step 4: Generate Token (From Backend)
You will need to generate a token from your backend and pass it to app while initiating payments. For generating token you need to use our token generation API. Please take care that this API is called only from your <b><u>backend</u></b> as it uses **secretKey**. Thus this API should **never be called from App**.

<br/>

### Request Description

<copybox>

  For production/live usage set the action attribute of the form to:
   `https://api.cashfree.com/api/v2/cftoken/order`

  For testing set the action attribute to:
   `https://test.cashfree.com/api/v2/cftoken/order`

</copybox>

You need to send orderId, orderCurrency and orderAmount as a JSON object to the API endpoint and in response a token will received. Please see  the description of request below.

```bash
curl -XPOST -H 'Content-Type: application/json' 
-H 'x-client-id: <YOUR_APP_ID>' 
-H 'x-client-secret: <YOUR_SECRET_KEY>' 
-d '{
  "orderId": "<ORDER_ID>",
  "orderAmount":<ORDER_AMOUNT>,
  "orderCurrency": "INR"
}' 'https://test.cashfree.com/api/v2/cftoken/order'
```
<br/>

### Request Example

Replace **YOUR_APP_ID** and **YOUR_SECRET_KEY** with actual values.
```bash
curl -XPOST -H 'Content-Type: application/json' -H 'x-client-id: YOUR_APP_ID' -H 'x-client-secret: YOUR_SECRET_KEY' -d '{
  "orderId": "Order0001",
  "orderAmount":1,
  "orderCurrency":"INR"
}' 'https://test.cashfree.com/api/v2/cftoken/order'
```
<br/>

### Response Example

```bash
{
"status": "OK",
"message": "Token generated",
"cftoken": "v79JCN4MzUIJiOicGbhJCLiQ1VKJiOiAXe0Jye.s79BTM0AjNwUDN1EjOiAHelJCLiIlTJJiOik3YuVmcyV3QyVGZy9mIsEjOiQnb19WbBJXZkJ3biwiIxADMwIXZkJ3TiojIklkclRmcvJye.K3NKICVS5DcEzXm2VQUO_ZagtWMIKKXzYOqPZ4x0r2P_N3-PRu2mowm-8UXoyqAgsG"
}
```

The "cftoken" is the token that is used authenticate your payment request that will be covered in the next step.
<br/>

## Step 5: Initiate Payment

- App passes the order info and the token to the SDK
- Customer is shown the payment screen where he completes the payment
- Once the payment is complete SDK verifies the payment
- App receives the response from SDK and handles it appropriately


## How to integrate

For both the modes (normal and [seamless](https://docs.cashfree.com/docs/android/guide/#seamless-integration)) you need to invoke the <b>doPayment()</b> method. However, there are a few extra parameters you need to pass incase of seamless mode.


### doPayment

```dart
    var options = {
      'token': "token",
      'stage': "you stage",
      'appId': "you appId",
      'orderId': "you orderId",
      'orderAmount': 100,
      'orderNote': "TestNote",
      'orderCurrency': "INR",
      'customerName': "Test",
      'customerPhone': "15264328733",
      'customerEmail': "test@126.com",
      'notifyUrl': 'notifyUrl',
      "paymentModes": 'paymentModes'
    };
    _cashfree.doPayment(options);

```

Initiates the payment in a webview. The customer will be taken to the payment page on cashfree server where they will have the option of paying through any payment option that is activated on their account. Once the payment is done the webview will close and the response will be delivered in the callback.

<b>options:</b>

-  <code>token</code>: The token generated from **Step 4**.

-  <code>stage</code>: Value should be either "**TEST**" or "**PROD**" for testing server or production server respectively.
 
-  <code>color1</code>: Toolbar background [color](https://api.flutter.dev/flutter/dart-ui/Color-class.html)

-  <code>color2</code>: Toolbar text and back arrow [color](https://api.flutter.dev/flutter/dart-ui/Color-class.html)

-  <code>hideOrderId</code>: Whether to show the order number is hidden by default [bool]

  
```dart
    var options = {
      'token': "token",
      'stage': "you stage",
      'appId': "you appId",
      'orderId': "you orderId",
      'orderAmount': 100,
      'orderNote': "TestNote",
      'orderCurrency': "INR",
      'customerName': "Test",
      'customerPhone': "15264328733",
      'customerEmail': "test@126.com",
      'notifyUrl': 'notifyUrl',
      "paymentModes": 'paymentModes'
    };
    _cashfree.upiPayment(options);

```

Initiate the payment in a webview. The customer will be taken to the payment page on cashfree server where they will have the option of paying through any payment option that is activated on their account. Once the payment is done the webview will close and the response will be delivered in the callback.

  
<b>option:</b>

-  <code>token</code>: The token generated from **Step 4**.

-  <code>stage</code>: Value should be either "**TEST**" or "**PROD**" for testing server or production server respectively.

## example

```dart
    final Cashfree _cashfree = Cashfree();
    _cashfree.on(Cashfree.EVENT_PAYMENT_SUCCESS, _handleCashfreePaymentSuccess);
    _cashfree.on(Cashfree.EVENT_PAYMENT_ERROR, _handleCashfreePaymentError);
    _cashfree.on(Cashfree.EVENT_ERROR, _handleCashfreeError);
    
    // 支付成功处理
    void _handleCashfreePaymentSuccess(
      CashfreePaymentSuccessResponse response) async {
      print("Cashfree payment success orderId:${response.orderId}");
    }
    
    // 支付失败事件处理
    void _handleCashfreePaymentError(
      CashfreePaymentFailureResponse response) async {
        print("Cashfree payment error msg:${response.txMsg}, status: ${response.txStatus}");
    }
    
    // 错误事件处理
    void _handleCashfreeError(CashfreeError cashfreeError) async {
        print("Cashfree error:${cashfreeError.message}");
    }
```

<br/>

  

# Request Parameters

| Parameter                                 | Required | Description                                      |
|-------------------------------------|-----------|----------------------------------------------------|
| <code>appId</code>            | Yes      | Your app id      |
| <code>orderId</code> | Yes       | Order/Invoice Id  |
| <code>orderAmount</code> | Yes       | Bill amount of the order      |
| <code>orderNote</code>            | No       | A help text to make customers know more about the order                                |
| <code>orderCurrency</code> | Yes    | Currency code of the order. Default is INR.     |
| <code>customerName</code> | No    | Name of the customer     |
| <code>customerPhone</code> | Yes    | Phone number of customer     |
| <code>customerEmail</code> | Yes    | Email id of the customer     |
| <code>notifyUrl</code> | No    | Notification URL for server-server communication. Useful when user’s connection drops after completing payment.     |
| <code>paymentModes</code> | No    | Allowed payment modes for this order. Available values: cc, dc, nb, paypal, upi, wallet. <strong>Leave it blank if you want to display all modes</strong>     |


# Response parameters

These parameters are sent as extras to the onActivityResult(). They contain the details of the transaction.

| Parameter                                  | Description                                      |
|------------------------------------------------|----------------------------------------------------|
| <code>orderId</code>  | Order id for which transaction has been processed. Ex: GZ-212  |
| <code>orderAmount</code> | Amount of the order. Ex: 256.00      |
| <code>paymentMode</code> | Payment mode of the transaction.      |
| <code>referenceId</code>      | Cashfree generated unique transaction Id. Ex: 140388038803                                |
| <code>txStatus</code>   | Payment status for that order. Values can be : SUCCESS, FLAGGED, PENDING, FAILED, CANCELLED.     |
| <code>paymentMode</code>   | Payment mode used by customer to make the payment. Ex: DEBIT_CARD, MobiKwik, etc     |
| <code>txMsg</code>   | Message related to the transaction. Will have the reason, if payment failed     |
| <code>txTime</code>  | Time of the transaction    |
| <code>type</code>  | Fixed value : <b>CashFreeResponse</b>. To identify the response is from cashfree SDK.    |
| <code>signature</code>  | Response signature, more [here.](https://docs.cashfree.com/pg/cf-checkout/#response-verification)    |



### NOTE

There can be scenarios where the SDK is not able to verify the payment within a short period of time. The status of such orders will be <code><b>PENDING</b></code>