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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dilip.aidlclient.ui.theme.AIDLClientTheme
import com.dilip.aidlserver.ISimpl

class MainActivity : ComponentActivity() {

    private var simpl: ISimpl? = null
    private var isBound by mutableStateOf(false)
    private var resultText by mutableStateOf("Result will appear here")

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            simpl = ISimpl.Stub.asInterface(service)
            isBound = true
            showToast("Service Bound")
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CalculatorApp() {
        var firstValue by remember { mutableStateOf("") }
        var secondValue by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "AIDL Calculator",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Enter Numbers")
                Spacer(modifier = Modifier.height(8.dp))

                NumberInputField("First Number", firstValue) { firstValue = it }
                NumberInputField("Second Number", secondValue) { secondValue = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Result
                Text(text = resultText, style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(16.dp))

                OperationButtons(firstValue, secondValue)

                Spacer(modifier = Modifier.height(24.dp))

                // Service Bind/Unbind
                ServiceBindToggle()
            }
        }

    }

    @Composable
    fun NumberInputField(label: String, value: String, onValueChange: (String) -> Unit) {
        TextField(
            value = value,
            onValueChange = {
                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                    onValueChange(it)
                }
            },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = value.isNotEmpty() && value.any { !it.isDigit() }
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    @Composable
    fun OperationButtons(firstValue: String, secondValue: String) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically

        ) {
            OperationButton("Add") { performOperation("add", firstValue, secondValue) }
            OperationButton("Subtract") { performOperation("sub", firstValue, secondValue) }
            OperationButton("Multiply") { performOperation("mul", firstValue, secondValue) }
        }
    }


    @Composable
    fun OperationButton(operation: String, onClick: () -> Unit) {
        Button(
            onClick = onClick,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(operation)
        }
    }

    @Composable
    fun ServiceBindToggle() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBound) "Service is Bound" else "Service is not Bound",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = if (isBound) Color.Green else Color.Red
            )

            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isBound,
                onCheckedChange = { isChecked ->
                    if (isChecked) {
                        bindService()
                    } else {
                        unbindService()
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Green,
                    uncheckedThumbColor = Color.Red
                )
            )
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
                showToast("Error in operation")
            }
        } else {
            showToast("Service not bound. Please bind the service first.")
        }
    }

    private fun bindService() {
        if (!isBound) {
            val intent = Intent("com.dilip.aidlserver.AIDL")
            intent.setPackage("com.dilip.aidlserver")
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            showToast("Service already bound.")
        }
    }

    private fun unbindService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            showToast("Service Unbound")
        } else {
            showToast("Service is not bound")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
