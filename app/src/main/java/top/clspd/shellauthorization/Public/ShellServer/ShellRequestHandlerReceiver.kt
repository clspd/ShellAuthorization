package top.clspd.shellauthorization.Public.ShellServer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import top.clspd.shellauthorization.Shared.ShellServer.ShellRequestHandler

class ShellRequestHandlerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("top.clspd.shellauthorization.intent.action.REQUEST_BINDER" == intent.action) {
            ShellRequestHandler.handleRequest(context, intent)
        }
    }
}