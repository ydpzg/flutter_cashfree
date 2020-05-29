import 'package:flutter/material.dart';
import 'package:cashfree/cashfree.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final Cashfree _cashfree = Cashfree();

  @override
  void initState() {
    super.initState();

    // 初始化Cashfree
    _cashfree.on(Cashfree.EVENT_PAYMENT_SUCCESS, _handleCashfreePaymentSuccess);
    _cashfree.on(Cashfree.EVENT_PAYMENT_ERROR, _handleCashfreePaymentError);
    _cashfree.on(Cashfree.EVENT_ERROR, _handleCashfreeError);
  }

  @override
  void dispose() {
    super.dispose();
    _cashfree.clear();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: SizedBox(
            height: 150,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                RaisedButton(
                  onPressed: _doPayment,
                  child: Text("doPayment"),
                ),
                RaisedButton(
                  onPressed: _upiPayment,
                  child: Text("upiPayment"),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _doPayment() {

    var options = {
      'token': "y99JCN4MzUIJiOicGbhJCLiQ1VKJiOiAXe0Jye.Gm0nIwQWO4QjNyUDOwQWZ1IiOiQHbhN3XiwCO3gTNxMzM5UTM6ICc4VmIsISOyUDMwIDMyQ3clRlI6ICZJJXZkJ3biwiIS5USiojI5NmblJnc1NkclRmcvJCLwATM6ICduV3btFkclRmcvJye.MTKRzp9dPtKos6ZgZtqn_03qR259hYti-hXb6om86YAlQTETO6ZVDOxRsSx-YAayRI",
      'stage': "TEST",
      'appId': "14375168c50ba084d18e92a3e57341",
      'orderId': "Test20200529",
      'orderAmount': 100,
      'orderNote': "TestNote",
      'orderCurrency': "INR",
      'customerName': "Test",
      'customerPhone': "15264328733",
      'customerEmail': "test@126.com",
      'notifyUrl': '',
      "paymentModes": ''
    };
    _cashfree.doPayment(options);
  }

  void _upiPayment() {
    var options = {
      'token': "639JCN4MzUIJiOicGbhJCLiQ1VKJiOiAXe0Jye.rPQficTZxMDMjRTY5ADZlVjI6ICdsF2cfJCLykjMxIzMzkTNxojIwhXZiwiIxETOyUDMwIDMyQ3clRlI6ICZJJXZkJ3biwiIS5USiojI5NmblJnc1NkclRmcvJCLwATM6ICduV3btFkclRmcvJye.g7ldJ_Oo7Ea-F2cHU87S_tIP5spAwNCc0xKMH_SiySeuz33JPlQhjkUEyCx7lnwHXT",
      'stage': "TEST",
      'appId': "14375168c50ba084d18e92a3e57341",
      'orderId': "Test2020052911",
      'orderAmount': 100,
      'orderNote': "TestNote",
      'orderCurrency': "INR",
      'customerName': "Test",
      'customerPhone': "15264328733",
      'customerEmail': "test@126.com",
      'notifyUrl': '',
      "paymentModes": ''
    };
    _cashfree.upiPayment(options);
  }

  // 支付成功处理
  void _handleCashfreePaymentSuccess(
      CashfreePaymentSuccessResponse response) async {
    print("Cashfree payment success orderId:${response.orderId}");
  }

  // 支付失败事件处理
  void _handleCashfreePaymentError(
      CashfreePaymentFailureResponse response) async {
    print(
        "Cashfree payment error msg:${response.txMsg}, status: ${response.txStatus}");
  }

  // 错误事件处理
  void _handleCashfreeError(CashfreeError cashfreeError) async {
    print("Cashfree error:${cashfreeError.message}");
  }
}
