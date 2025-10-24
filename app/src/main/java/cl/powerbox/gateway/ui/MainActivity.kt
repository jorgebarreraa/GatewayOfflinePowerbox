package cl.powerbox.gateway.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cl.powerbox.gateway.R
import cl.powerbox.gateway.service.GatewayForegroundService
import cl.powerbox.gateway.util.BatteryOptHelper

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == GatewayForegroundService.ACTION_STATE_CHANGED) {
                val running = intent.getBooleanExtra(GatewayForegroundService.EXTRA_RUNNING, false)
                renderState(running)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop  = findViewById(R.id.btnStop)

        btnStart.setOnClickListener { GatewayForegroundService.start(this) }
        btnStop.setOnClickListener  { GatewayForegroundService.stop(this) }

        // Estado inicial por si el broadcast tarda
        renderState(GatewayForegroundService.isRunning(this))

        // 🔋 Pedir exención de batería SOLO UNA VEZ
        BatteryOptHelper.maybeRequestOnce(this)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            stateReceiver,
            IntentFilter(GatewayForegroundService.ACTION_STATE_CHANGED)
        )
        // Pedir estado actual (lo envía como broadcast)
        GatewayForegroundService.queryState(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(stateReceiver)
    }

    private fun renderState(running: Boolean) {
        tvStatus.text = if (running) "Servicio: EN EJECUCIÓN" else "Servicio: DETENIDO"
        btnStart.isEnabled = !running
        btnStop.isEnabled  = running
    }
}