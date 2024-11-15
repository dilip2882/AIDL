package com.dilip.aidlserver

import android.app.Service
import android.content.Intent
import android.os.IBinder

class MyService : Service() {

    private val serviceImp: SimpServiceImp = SimpServiceImp()

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    class SimpServiceImp : ISimpl.Stub() {
        override fun add(a: Int, b: Int): Int {
            return a + b
        }

        override fun sub(a: Int, b: Int): Int {
            return a - b
        }

    }
}