package top.clspd.shellauthorization.Shared.ShellServer

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.util.Log
import top.clspd.shellauthorization.Public.Client.ShellBinder

object ShellRequestHandler {

    fun handleRequest(context: Context, intent: Intent): Boolean {
        Log.i("ShellRequestHandler", "Handling request, action = " + intent.action)

        if (intent.action != "top.clspd.shellauthorization.intent.action.REQUEST_BINDER") {
            return false
        }

        val binder = intent.getBundleExtra("data")?.getBinder("binder") ?: return false
        val shellBinderInstance = ShellBinder()
        val isBindSuccess = shellBinderInstance.bind(context.applicationContext, 4000)
        if (!isBindSuccess) {
            Log.e("ShellRequestHandler", "Binder not received");
        }

        val data = Parcel.obtain()
        return try {
            if (isBindSuccess) data.writeStrongBinder(shellBinderInstance.binder)
            data.writeString(context.applicationInfo.sourceDir)
            binder.transact(1, data, null, IBinder.FLAG_ONEWAY)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        } finally {
            data.recycle()
        }
    }
}