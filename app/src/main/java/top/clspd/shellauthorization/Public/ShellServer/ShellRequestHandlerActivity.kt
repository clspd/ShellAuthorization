package top.clspd.shellauthorization.Public.ShellServer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import top.clspd.shellauthorization.Shared.ShellServer.ShellRequestHandler

class ShellRequestHandlerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ShellRequestHandler.handleRequest(this, intent)
        finish()
    }
}