package com.dilip.aidlclient

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dilip.aidlserver.ISimpl
import com.dilip.aidlclient.ui.theme.AIDLClientTheme

class MainActivity : ComponentActivity() {

    private var simpl: ISimpl? = null
    private var isBound by mutableStateOf(false)
    private var resultText by mutableStateOf("Result will appear here")

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            simpl = ISimpl.Stub.asInterface(service)
            isBound = true
            Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            simpl = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIDLClientTheme {
                CalculatorApp()
            }
        }
    }

    @Composable
    fun CalculatorApp() {
        var firstValue by remember { mutableStateOf("0") }
        var secondValue by remember { mutableStateOf("0") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AIDL Calculator", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Enter Numbers:")
            Spacer(modifier = Modifier.height(8.dp))

            // Input fields for numbers
            TextField(
                value = firstValue,
                onValueChange = { firstValue = it },
                label = { Text("First Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = secondValue,
                onValueChange = { secondValue = it },
                label = { Text("Second Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Result Text
            Text(text = resultText, style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // Operation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OperationButton("Add") {
                    performOperation("add", firstValue, secondValue)
                }
                OperationButton("Subtract") {
                    performOperation("sub", firstValue, secondValue)
                }
                OperationButton("Multiply") {
                    performOperation("mul", firstValue, secondValue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bind Service Button
            Button(onClick = { bindService() }) {
                Text("Bind Service")
            }
        }
    }

    @Composable
    fun OperationButton(operation: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
        ) {
            Text(operation)
        }
    }

    private fun performOperation(operation: String, firstValue: String, secondValue: String) {
        if (isBound) {
            try {
                val num1 = firstValue.toIntOrNull() ?: 0
                val num2 = secondValue.toIntOrNull() ?: 0
                val result = when (operation) {
                    "add" -> simpl?.add(num1, num2)
                    "sub" -> simpl?.sub(num1, num2)
                    "mul" -> simpl?.mul(num1, num2)
                    else -> 0
                }
                resultText = "$operation result: $result"
            } catch (e: RemoteException) {
                e.printStackTrace()
                Toast.makeText(this, "Error in operation", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Service not bound. Please bind the service first.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindService() {
        if (!isBound) {
            val intent = Intent("com.dilip.aidlserver.AIDL")
            intent.setPackage("com.dilip.aidlserver")
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
