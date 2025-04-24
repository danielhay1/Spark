package com.example.spark.objects.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (action != null) {
            val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            val deviceName = if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            } else {
                //
            }
            // permission asked
            device?.name ?: "Unknown device"

            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    // Device connected
                    // todo: PARK
                    // Handle the Bluetooth connection event
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    // Device disconnected
                    // todo: PARK
                    // Handle the Bluetooth disconnection event
                }

                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {
                    // Disconnection request
                    Toast.makeText(context, "Disconnection requested for $deviceName", Toast.LENGTH_SHORT).show()
                    // Handle the disconnection request event
                }

                else -> {
                    // Handle other actions if necessary
                }
            }
        }
    }
}