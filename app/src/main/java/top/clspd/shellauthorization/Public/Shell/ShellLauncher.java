package top.clspd.shellauthorization.Public.Shell;

import java.util.Objects;

public class ShellLauncher {
    public static void main(String[] args) {
        if (args.length < 1 || Objects.equals(args[0], "-h") || Objects.equals(args[0], "--help")) {
            System.out.println("Usage: <app> [-h|--help|--|<template>] [command] [...args]\n-h\t\tShow help message\n--help\t\tShow help message\n--\t\tDirectly run command if permitted\n<template>\tRun specified template");
            return;
        }

        try {
            System.out.println("Not implemented");
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
