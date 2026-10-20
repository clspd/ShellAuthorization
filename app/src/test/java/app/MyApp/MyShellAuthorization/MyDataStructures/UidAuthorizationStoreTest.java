package app.MyApp.MyShellAuthorization.MyDataStructures;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UidAuthorizationStoreTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private UidAuthorizationStore store() {
        return new UidAuthorizationStore(temporaryFolder.getRoot());
    }

    private File allowFile(int uid) {
        return new File(new File(temporaryFolder.getRoot(), "allowlist"), Integer.toString(uid));
    }

    private File recycledFile(int uid) {
        return new File(new File(new File(temporaryFolder.getRoot(), "Recycle.Bin"), "allowlist"),
                Integer.toString(uid));
    }

    @Test
    public void authorizeWritesCanonicalUidFile() {
        UidAuthorizationStore store = store();
        assertTrue(store.authorize(10200, 1729412345678L));

        File file = allowFile(10200);
        assertTrue(file.isFile());
        AllowDeclaration declaration = AllowDeclaration.read(file);
        assertNotNull(declaration);
        assertTrue(declaration.isStillValidFor(10200, 1729412345678L));
        assertTrue(store.isAuthorized(10200, 1729412345678L));
    }

    @Test
    public void changedInstallTimeDeletesActiveAndRecycledConfiguration() {
        UidAuthorizationStore store = store();
        assertTrue(store.authorize(10200, 1000L));
        assertTrue(store.isAuthorized(10200, 1000L));

        assertFalse(store.isAuthorized(10200, 1001L));
        assertFalse(allowFile(10200).exists());
        assertFalse(recycledFile(10200).exists());
    }

    @Test
    public void revokeMovesConfigurationToRecycleBin() {
        UidAuthorizationStore store = store();
        assertTrue(store.authorize(10200, 1000L));

        store.revoke(10200);

        assertFalse(allowFile(10200).exists());
        assertTrue(recycledFile(10200).isFile());
        assertFalse(store.isAuthorized(10200, 1000L));

        assertTrue(store.authorize(10200, 1000L));
        assertFalse(recycledFile(10200).exists());
    }

    @Test
    public void uidMismatchIsRejectedAndDeleted() throws Exception {
        File dir = new File(temporaryFolder.getRoot(), "allowlist");
        assertTrue(dir.mkdirs());
        File file = new File(dir, "10200");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("{\"uid\":10201,\"earliest_first_install_time\":1000}");
        }

        assertFalse(store().isAuthorized(10200, 1000L));
        assertFalse(file.exists());
    }

    @Test
    public void malformedFileIsRejectedAndDeleted() throws Exception {
        File dir = new File(temporaryFolder.getRoot(), "allowlist");
        assertTrue(dir.mkdirs());
        File file = new File(dir, "10200");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("not json");
        }

        assertFalse(store().isAuthorized(10200, 1000L));
        assertFalse(file.exists());
    }

    @Test
    public void missingConfigurationIsNotAuthorized() {
        assertFalse(store().isAuthorized(10200, 1000L));
    }
}
