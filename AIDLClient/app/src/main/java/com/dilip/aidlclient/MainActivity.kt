package com.dilip.aidlclient

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dilip.aidlserver.ISimpl
import com.dilip.aidlclient.ui.theme.AIDLClientTheme

class MainActivity : ComponentActivity() {
    private lateinit var simpl: ISimpl
    private var isBound = false  // Track service binding status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AIDLClientTheme {
                ServiceBindingApp()
            }
        }
    }

    @Composable
    fun ServiceBindingApp() {
        var firstValue by remember { mutableStateOf("") }
        var secondValue by remember { mutableStateOf("") }
        var resultText by remember { mutableStateOf("Operation:") }
        var isServiceBound by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TopAppBar()

            Spacer(modifier = Modifier.height(20.dp))

            // Result
            Text(
                text = resultText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputField(
                value = firstValue,
                onValueChange = { firstValue = it },
                placeholder = "Enter first number"
            )

            Spacer(modifier = Modifier.height(10.dp))

            InputField(
                value = secondValue,
                onValueChange = { secondValue = it },
                placeholder = "Enter second number"
            )

            Spacer(modifier = Modifier.height(24.dp))

            OperationButtons(firstValue, secondValue, { result ->
                resultText = "Operation: $result"
            })

            Spacer(modifier = Modifier.height(24.dp))

            // Bind Service
            Button(
                onClick = {
                    initService()
                    isServiceBound = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = buttonColors(containerColor = Color(0xFF6200EA))
            ) {
                Text(text = "Bind Service", color = Color.White)
            }

            // Message if service is not bound
            if (!isServiceBound) {
                Text(text = "Service is not bound", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    @Composable
    fun TopAppBar() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF6200EE)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Client App",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }

    @Composable
    fun InputField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color(0xFFF1F1F1), CircleShape)
                .padding(16.dp),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        )
    }

    @Composable
    fun OperationButtons(firstValue: String, secondValue: String, updateResult: (String) -> Unit) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    performOperation("add", firstValue, secondValue) { a, b -> simpl.add(a, b) }
                    updateResult("Addition: ${firstValue.toIntOrNull() ?: 0} + ${secondValue.toIntOrNull() ?: 0}")
                },
                modifier = Modifier.weight(1f),
                colors = buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                Text("Add")
            }
            Button(
                onClick = {
                    performOperation("sub", firstValue, secondValue) { a, b -> simpl.sub(a, b) }
                    updateResult("Subtraction: ${firstValue.toIntOrNull() ?: 0} - ${secondValue.toIntOrNull() ?: 0}")
                },
                modifier = Modifier.weight(1f),
                colors = buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                Text("Subtract")
            }
            Button(
                onClick = {
                    performOperation("mul", firstValue, secondValue) { a, b -> simpl.mul(a, b) }
                    updateResult("Multiplication: ${firstValue.toIntOrNull() ?: 0} * ${secondValue.toIntOrNull() ?: 0}")
                },
                modifier = Modifier.weight(1f),
                colors = buttonColors(containerColor = Color(0xFF03DAC5))
            ) {
                Text("Multiply")
            }
        }
    }

    private fun initService() {
        if (!isBound) {
            val intent = Intent("com.dilip.aidlserver.AIDL")
            convertImplicitIntentToExplicitIntent(intent)?.let { explicitIntent ->
                Log.d("ServiceBinding", "Binding to service: ${explicitIntent.component}")
                bindService(explicitIntent, serviceConnection, BIND_AUTO_CREATE)
            } ?: run {
                Log.e("ServiceBinding", "Could not resolve service.")
                Toast.makeText(this, "Service binding failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service != null) {
                simpl = ISimpl.Stub.asInterface(service)
                isBound = true
                Log.d("ServiceBinding", "Service successfully bound.")
                Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("ServiceBinding", "Failed to bind service: service is null.")
                Toast.makeText(this@MainActivity, "Service connection failed", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            Toast.makeText(this@MainActivity, "Service Disconnected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performOperation(
        operation: String,
        firstValue: String,
        secondValue: String,
        operationFunc: (Int, Int) -> Int
    ) {
        if (isBound) {
            try {
                val firstNum = firstValue.toIntOrNull()
                val secondNum = secondValue.toIntOrNull()

                if (firstNum == null || secondNum == null) {
                    Toast.makeText(this, "Please enter valid integers", Toast.LENGTH_SHORT).show()
                    return
                }

                val result = operationFunc(firstNum, secondNum)
                Toast.makeText(this, "$operation result: $result", Toast.LENGTH_SHORT).show()
            } catch (e: RemoteException) {
                e.printStackTrace()
                Toast.makeText(this, "Error in operation: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                this,
                "Service not bound. Please bind the service first.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun convertImplicitIntentToExplicitIntent(implicitIntent: Intent?): Intent? {
        val pm: PackageManager = packageManager
        val resolveInfoList = pm.queryIntentServices(implicitIntent!!, 0)
        if (resolveInfoList == null || resolveInfoList.size != 1) {
            return null
        }

        val serviceInfo = resolveInfoList[0]
        val component =
            ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name)
        val explicitIntent = Intent(implicitIntent)
        explicitIntent.component = component

        return explicitIntent
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            Toast.makeText(this, "Service Unbound", Toast.LENGTH_SHORT).show()
        }
    }
}
