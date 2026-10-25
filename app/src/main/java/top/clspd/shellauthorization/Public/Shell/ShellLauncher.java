package top.clspd.shellauthorization.Public.Shell;

import android.os.Handler;
import android.os.IBinder;

import java.util.Objects;

import top.clspd.shellauthorization.Public.Client.ShellClient;

public class ShellLauncher {
    public static void main(String[] args) {
        throw new UnsupportedOperationException("Not supported, please use the correct API or see the documentation");
    }
    public static void main(String[] args, IBinder binder, Handler handler) {
        if (args.length < 1 || Objects.equals(args[0], "-h") || Objects.equals(args[0], "--help")) {
            System.out.println("Usage: <app> [-h|--help|--|<template>] [command] [...args]\n-h\t\tShow help message\n--help\t\tShow help message\n--\t\tDirectly run command if permitted\n<template>\tRun specified template");
            return;
        }

        try {
            System.exit(RealMain(args, binder, handler));
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.flush();
            throw new RuntimeException(e);
        }
    }

    private static int RealMain(String[] args, IBinder binder, Handler handler) {
        if (args.length < 2) {
            System.err.println("Empty command");
            return 22;
        }

        // we have binder now !!!
        ShellClient client = new ShellClient(binder);

        // 1. test if alive
        if (!client.ping()) {
            System.err.println("Cannot check connection status");
            return 1;
        }

        // 2. authorization


        return 0;
    }

}
