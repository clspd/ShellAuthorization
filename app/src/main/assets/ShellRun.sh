#!/system/bin/sh


NAME=ShellRun.dex


# ----------
BASEDIR=$(dirname "$0")
DEX="$BASEDIR/$NAME"

if [ ! -f "$DEX" ]; then
	echo "Cannot find $DEX , please check if you have the correct filename"
	exit 2
fi

if [ -w $DEX ]; then
	chmod -w "$DEX"
fi

if [ -w $DEX ]; then
	echo "Cannot chmod, please try to put the file to the application data directory."
	exit 1
fi

exec /system/bin/app_process -Djava.class.path="$DEX" /system/bin top.clspd.shellauthorization.Public.Shell.ShellRun "$@"
