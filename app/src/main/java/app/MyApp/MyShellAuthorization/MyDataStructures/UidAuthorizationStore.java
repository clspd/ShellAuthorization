package app.MyApp.MyShellAuthorization.MyDataStructures;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;

/**
 * Stores per-UID authorization files and verifies them against the currently installed packages.
 *
 * <p>An authorization is only valid while the earliest {@code firstInstallTime} among all packages
 * sharing the UID is unchanged. This catches UID recycling: a newly installed package that received
 * a recycled UID has a different earliest installation time, so the authorization is deleted.</p>
 */
public final class UidAuthorizationStore {

    private static final String ALLOWLIST_DIR = "allowlist";
    private static final String RECYCLE_BIN_DIR = ".deleted";

    private static volatile UidAuthorizationStore instance;

    private final File allowlistDir;
    private final File recycledDir;
    private final Object lock = new Object();

    public UidAuthorizationStore(File dataRoot) {
        this.allowlistDir = new File(dataRoot, ALLOWLIST_DIR);
        this.recycledDir = new File(new File(dataRoot, RECYCLE_BIN_DIR), ALLOWLIST_DIR);
    }

    public static UidAuthorizationStore get() {
        UidAuthorizationStore local = instance;
        if (local == null) {
            synchronized (UidAuthorizationStore.class) {
                local = instance;
                if (local == null) {
                    String dataRoot = MyDataDirectory.get();
                    if (dataRoot == null) {
                        throw new IllegalStateException("Data directory is not initialized");
                    }
                    instance = local = new UidAuthorizationStore(new File(dataRoot));
                }
            }
        }
        return local;
    }

    public File allowFile(int uid) {
        return new File(allowlistDir, Integer.toString(uid));
    }

    public File recycledFile(int uid) {
        return new File(recycledDir, Integer.toString(uid));
    }

    /**
     * Writes an authorization based on an already computed earliest firstInstallTime.
     */
    public boolean authorize(int uid, long earliestFirstInstallTime) {
        if (uid <= 0 || earliestFirstInstallTime < 0) return false;
        synchronized (lock) {
            if (!ensureDirectory(allowlistDir)) return false;
            File allow = allowFile(uid);
            try {
                AllowDeclaration.create(uid, earliestFirstInstallTime).write(allow);
            } catch (IOException e) {
                return false;
            }
            // The active file was just rewritten; an old recycled copy must not be restored later.
            File recycled = recycledFile(uid);
            if (recycled.exists()) recycled.delete();
            return true;
        }
    }

    /**
     * Computes the current earliest firstInstallTime for the UID and writes an authorization.
     */
    public boolean authorize(PackageManager pm, int uid) {
        Long current = currentEarliestFirstInstallTime(pm, uid);
        return current != null && authorize(uid, current);
    }

    /**
     * Checks the authorization against an already computed earliest firstInstallTime. Deletes the
     * stored configuration when it no longer matches.
     */
    public boolean isAuthorized(int uid, long currentEarliestFirstInstallTime) {
        if (uid <= 0 || currentEarliestFirstInstallTime < 0) return false;
        synchronized (lock) {
            return readValidLocked(uid, currentEarliestFirstInstallTime) != null;
        }
    }

    /**
     * Reads the authorization of the UID after checking it against an already computed earliest
     * firstInstallTime. Deletes the stored configuration when it no longer matches.
     *
     * @return null when the UID is not authorized.
     */
    public AllowDeclaration readValidAuthorization(int uid, long currentEarliestFirstInstallTime) {
        if (uid <= 0 || currentEarliestFirstInstallTime < 0) return null;
        synchronized (lock) {
            return readValidLocked(uid, currentEarliestFirstInstallTime);
        }
    }

    /**
     * Recomputes the earliest firstInstallTime from the installed packages and reads the
     * authorization.
     *
     * @return null when the UID is not authorized.
     */
    public AllowDeclaration readValidAuthorization(PackageManager pm, int uid) {
        Long current = currentEarliestFirstInstallTime(pm, uid);
        if (current == null) {
            invalidate(uid);
            return null;
        }
        return readValidAuthorization(uid, current);
    }

    /**
     * Updates the per-application settings stored in an existing authorization. The UID and the
     * recorded firstInstallTime are preserved.
     *
     * @return false when the UID is not authorized or the file cannot be written.
     */
    public boolean updateSettings(int uid, long currentEarliestFirstInstallTime,
                                  List<String> allowedTemplates,
                                  boolean allowDirectCommands,
                                  CommandTemplate.Profile directCommandProfile,
                                  boolean promptForConsentBeforeDirectCommand) {
        if (uid <= 0 || currentEarliestFirstInstallTime < 0) return false;
        synchronized (lock) {
            AllowDeclaration declaration = readValidLocked(uid, currentEarliestFirstInstallTime);
            if (declaration == null) return false;
            if (!ensureDirectory(allowlistDir)) return false;

            declaration.allowedTemplates = allowedTemplates == null
                    ? new ArrayList<>() : new ArrayList<>(allowedTemplates);
            declaration.allowDirectCommands = allowDirectCommands;
            declaration.directCommandProfile = directCommandProfile == null
                    ? new CommandTemplate.Profile() : directCommandProfile.copy();
            declaration.promptForConsentBeforeDirectCommand = promptForConsentBeforeDirectCommand;
            declaration.normalize();

            try {
                declaration.write(allowFile(uid));
            } catch (IOException e) {
                return false;
            }
            // An old recycled copy must not be restored later.
            File recycled = recycledFile(uid);
            if (recycled.exists()) recycled.delete();
            return true;
        }
    }

    /**
     * Recomputes the earliest firstInstallTime and updates the per-application settings.
     */
    public boolean updateSettings(PackageManager pm, int uid,
                                  List<String> allowedTemplates,
                                  boolean allowDirectCommands,
                                  CommandTemplate.Profile directCommandProfile,
                                  boolean promptForConsentBeforeDirectCommand) {
        Long current = currentEarliestFirstInstallTime(pm, uid);
        if (current == null) {
            invalidate(uid);
            return false;
        }
        return updateSettings(uid, current, allowedTemplates, allowDirectCommands,
                directCommandProfile, promptForConsentBeforeDirectCommand);
    }

    /**
     * Recomputes the earliest firstInstallTime from the installed packages and checks the
     * authorization.
     */
    public boolean isAuthorized(PackageManager pm, int uid) {
        Long current = currentEarliestFirstInstallTime(pm, uid);
        if (current == null) {
            invalidate(uid);
            return false;
        }
        return isAuthorized(uid, current);
    }

    /**
     * Moves the active authorization to the recycle bin.
     */
    public void revoke(int uid) {
        synchronized (lock) {
            File allow = allowFile(uid);
            if (!allow.exists()) return;

            File recycled = recycledFile(uid);
            if (!ensureDirectory(recycledDir)) {
                allow.delete();
                return;
            }
            if (recycled.exists() && !recycled.delete()) {
                allow.delete();
                return;
            }
            if (!allow.renameTo(recycled)) allow.delete();
        }
    }

    /**
     * Validates every stored authorization, deleting active and recycled configurations whose
     * recorded install time no longer matches the installed packages. Files whose names are not
     * canonical decimal UIDs are ignored.
     */
    public void pruneInvalid(PackageManager pm) {
        synchronized (lock) {
            File[] activeFiles = allowlistDir.listFiles();
            if (activeFiles != null) {
                for (File file : activeFiles) {
                    int uid = parseCanonicalUid(file);
                    if (uid < 0) continue;
                    Long current = currentEarliestFirstInstallTime(pm, uid);
                    if (current == null) {
                        invalidateLocked(uid);
                    } else {
                        isAuthorized(uid, current);
                    }
                }
            }

            File[] recycledFiles = recycledDir.listFiles();
            if (recycledFiles != null) {
                for (File file : recycledFiles) {
                    int uid = parseCanonicalUid(file);
                    if (uid < 0) continue;
                    AllowDeclaration declaration = AllowDeclaration.read(file);
                    if (declaration == null) {
                        file.delete();
                        continue;
                    }
                    Long current = currentEarliestFirstInstallTime(pm, uid);
                    if (current == null || !declaration.isStillValidFor(uid, current)) {
                        file.delete();
                    }
                }
            }
        }
    }

    private Long currentEarliestFirstInstallTime(PackageManager pm, int uid) {
        List<PackageInfo> packages = UidPackages.packagesForUid(pm, uid);
        if (packages == null || packages.isEmpty()) return null;
        return UidPackages.earliestFirstInstallTime(packages);
    }

    /**
     * Reads a declaration and deletes it when it is unreadable or no longer matches the UID.
     * Callers must hold {@link #lock}.
     */
    private AllowDeclaration readValidLocked(int uid, long currentEarliestFirstInstallTime) {
        File allow = allowFile(uid);
        AllowDeclaration declaration = AllowDeclaration.read(allow);
        if (declaration == null) {
            if (allow.exists()) invalidateLocked(uid);
            return null;
        }
        if (!declaration.isStillValidFor(uid, currentEarliestFirstInstallTime)) {
            invalidateLocked(uid);
            return null;
        }
        declaration.normalize();
        return declaration;
    }

    private void invalidate(int uid) {
        synchronized (lock) {
            invalidateLocked(uid);
        }
    }

    private void invalidateLocked(int uid) {
        File allow = allowFile(uid);
        if (allow.exists()) allow.delete();
        File recycled = recycledFile(uid);
        if (recycled.exists()) recycled.delete();
    }

    private static boolean ensureDirectory(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    /**
     * @return the UID encoded by the file name, or -1 if the name is not a canonical decimal UID.
     */
    private static int parseCanonicalUid(File file) {
        if (!file.isFile()) return -1;
        String name = file.getName();
        if (name.isEmpty() || name.charAt(0) < '1' || name.charAt(0) > '9') return -1;
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < '0' || c > '9') return -1;
        }
        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
