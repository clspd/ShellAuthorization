package app.MyApp.MyShellAuthorization.MyDataStructures;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helpers for resolving the packages that share a Linux UID.
 */
public final class UidPackages {

    private UidPackages() {
    }

    /**
     * Returns all packages sharing {@code uid}, or an empty list if the UID currently has no
     * packages.
     *
     * @return null if the UID could not be resolved reliably (for example because a package
     * disappeared in the middle of the query).
     */
    public static @Nullable List<PackageInfo> packagesForUid(PackageManager pm, int uid) {
        if (uid <= 0) return null;

        // getPackagesForUid() and getPackageInfo() can transiently disagree while a package is
        // being installed or uninstalled. Retry the whole snapshot once before giving up.
        for (int attempt = 0; attempt < 2; attempt++) {
            String[] names = pm.getPackagesForUid(uid);
            if (names == null || names.length == 0) return Collections.emptyList();

            List<PackageInfo> result = new ArrayList<>(names.length);
            Set<String> seen = new HashSet<>();
            boolean complete = true;
            for (String name : names) {
                if (name == null || !seen.add(name)) continue;
                try {
                    result.add(pm.getPackageInfo(name, 0));
                } catch (PackageManager.NameNotFoundException e) {
                    complete = false;
                    break;
                }
            }
            if (complete) return result;
        }
        return null;
    }

    /**
     * @return the earliest firstInstallTime among the packages, or -1 if the list is empty.
     */
    public static long earliestFirstInstallTime(List<PackageInfo> packages) {
        long earliest = -1;
        for (PackageInfo pkg : packages) {
            if (pkg == null) continue;
            long time = pkg.firstInstallTime;
            if (earliest < 0 || time < earliest) earliest = time;
        }
        return earliest;
    }

    /**
     * Picks a stable representative package: earliest firstInstallTime, ties broken by package name.
     */
    public static @Nullable PackageInfo primary(List<PackageInfo> packages) {
        PackageInfo primary = null;
        for (PackageInfo pkg : packages) {
            if (pkg == null) continue;
            if (primary == null
                    || pkg.firstInstallTime < primary.firstInstallTime
                    || (pkg.firstInstallTime == primary.firstInstallTime
                    && pkg.packageName.compareTo(primary.packageName) < 0)) {
                primary = pkg;
            }
        }
        return primary;
    }
}
