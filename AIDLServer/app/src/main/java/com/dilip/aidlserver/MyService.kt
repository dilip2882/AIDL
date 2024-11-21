package com.dilip.aidlserver

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyService : Service() {
    private val binder = object : ISimpl.Stub() {
        override fun add(a: Int, b: Int): Int {
            return a + b
        }

        override fun sub(a: Int, b: Int): Int {
            return a - b
        }

        override fun mul(a: Int, b: Int): Int {
            return a * b
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}
