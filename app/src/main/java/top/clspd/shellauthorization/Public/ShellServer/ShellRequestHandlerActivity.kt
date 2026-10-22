package top.clspd.shellauthorization.Public.ShellServer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.IBinder
import android.os.Parcel
import top.clspd.shellauthorization.Public.ShellServer.ShellRequestHandler

class ShellRequestHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShellRequestHandler.handleRequest(this, intent)
        finish()
    }
}