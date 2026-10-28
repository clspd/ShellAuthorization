package top.clspd.shellauthorization.Public.ShellServer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import top.clspd.shellauthorization.Shared.ShellServer.ShellRequestHandler

class ShellRequestHandlerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("top.clspd.shellauthorization.intent.action.REQUEST_BINDER" == intent.action) {
            val pendingResult = goAsync()
            val appContext = context.applicationContext

            Thread({
                try {
                    ShellRequestHandler.handleRequest(appContext, intent)
                } catch (e: Throwable) {
                    Log.e(
                        "ShellRequestHandler",
                        "Failed to handle request",
                        e
                    )
                } finally {
                    pendingResult.finish()
                }
            }, "ShellRequestHandlerWorker").start()
        }
    }
}