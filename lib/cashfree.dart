import 'dart:async';

import 'package:flutter/services.dart';
import 'package:eventify/eventify.dart';

class Cashfree {
  // Response codes from platform
  static const _CODE_PAYMENT_SUCCESS = 0;
  static const _CODE_PAYMENT_ERROR = 1;

  /// 参数错误代码
  static const _CODE_PAYMENT_PARAMETERS_ERROR = 2;

  static const EVENT_PAYMENT_SUCCESS = 'payment.success';
  static const EVENT_PAYMENT_ERROR = 'payment.error';
  static const EVENT_ERROR = 'error';

  static const MethodChannel _channel = const MethodChannel('cashfree');

  EventEmitter _eventEmitter;

  Cashfree() {
    _eventEmitter = new EventEmitter();
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');

    return version;
  }

  void doPayment(Map<String, dynamic> options) async {
    Map<String, dynamic> validationResult = _validateOptions(options);
    if (!validationResult['success']) {
      _handleResult({
        'type': _CODE_PAYMENT_PARAMETERS_ERROR,
        'data': {'message': validationResult['message']}
      });
      return;
    }
    var response = await _channel.invokeMethod('doPayment', options);
    _handleResult(response);
  }

  void upiPayment(Map<String, dynamic> options) async {
    Map<String, dynamic> validationResult = _validateOptions(options);
    if (!validationResult['success']) {
      _handleResult({
        'type': _CODE_PAYMENT_PARAMETERS_ERROR,
        'data': {'message': validationResult['message']}
      });
      return;
    }
    var response = await _channel.invokeMethod('upiPayment', options);
    _handleResult(response);
  }

  /// Validate payment options
  static Map<String, dynamic> _validateOptions(Map<String, dynamic> options) {
    var token = options['token'];
    if (token == null) {
      return {
        'success': false,
        'message':
            'token is required. Please check if token is present in options.'
      };
    }
    var appId = options['appId'];
    if (appId == null) {
      return {
        'success': false,
        'message':
            'appId is required. Please check if appId is present in options.'
      };
    }
    var stage = options['stage'];
    if (stage == null) {
      return {
        'success': false,
        'message':
            'stage is required. Please check if stage is present in options.'
      };
    }
    var orderId = options['orderId'];
    if (orderId == null) {
      return {
        'success': false,
        'message':
            'orderId is required. Please check if orderId is present in options.'
      };
    }
    return {'success': true};
  }

  void on(String event, Function handler) {
    EventCallback cb = (event, cont) {
      handler(event.eventData);
    };
    _eventEmitter.on(event, null, cb);
    _resync();
  }

  void _resync() async {
    var response = await _channel.invokeMethod('resync');
    if (response != null) {
      _handleResult(response);
    }
  }

  /// Handles checkout response from platform
  void _handleResult(Map<dynamic, dynamic> response) {
    String eventName;
    Map<dynamic, dynamic> data = response["data"];

    dynamic payload;

    switch (response['type']) {
      case _CODE_PAYMENT_SUCCESS:
        eventName = EVENT_PAYMENT_SUCCESS;
        payload = CashfreePaymentSuccessResponse.fromMap(data);
        break;
      case _CODE_PAYMENT_ERROR:
        eventName = EVENT_PAYMENT_ERROR;
        payload = CashfreePaymentFailureResponse.fromMap(data);
        break;
      case _CODE_PAYMENT_PARAMETERS_ERROR:
        eventName = EVENT_ERROR;
        payload = CashfreeError(data["message"]);
        break;
      default:
        eventName = EVENT_ERROR;
        payload = CashfreeError('An unknown error occurred.');
    }

    _eventEmitter.emit(eventName, null, payload);
  }

  /// Clears all event listeners
  void clear() {
    _eventEmitter.clear();
  }
}

class CashfreePaymentSuccessResponse {
  String referenceId;
  String orderId;
  String signature;
  String orderAmount;
  String paymentMode;
  String type;

  CashfreePaymentSuccessResponse(this.referenceId, this.orderId, this.signature,
      this.orderAmount, this.paymentMode, this.type);

  static CashfreePaymentSuccessResponse fromMap(Map<dynamic, dynamic> map) {
    String referenceId = map["referenceId"];
    String signature = map["signature"];
    String orderId = map["orderId"];
    String orderAmount = map["orderAmount"];
    String paymentMode = map["paymentMode"];
    String type = map["type"];

    return new CashfreePaymentSuccessResponse(
        referenceId, orderId, signature, orderAmount, paymentMode, type);
  }
}

class CashfreePaymentFailureResponse {
  String txStatus;
  String txMsg;
  String type;

  CashfreePaymentFailureResponse(this.txStatus, this.txMsg, this.type);

  static CashfreePaymentFailureResponse fromMap(Map<dynamic, dynamic> map) {
    var txStatus = map["txStatus"];
    var txMsg = map["txMsg"] as String;
    var type = map["type"];

    return new CashfreePaymentFailureResponse(txStatus, txMsg, type);
  }
}

class CashfreeError {
  String message;

  CashfreeError(this.message);
}
