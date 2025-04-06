package com.rncamera.rnbridge

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class OnnxBridge(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "OnnxBridge"
    }

    @ReactMethod
    fun executeImageRead() {

    }
}