package app.MyApp.MyShellAuthorization.MyDataStructures;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AllowDeclarationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void roundTripPreservesUidAndInstallTime() throws Exception {
        File file = temporaryFolder.newFile("10200");
        AllowDeclaration.create(10200, 1729412345678L).write(file);

        AllowDeclaration read = AllowDeclaration.read(file);
        assertNotNull(read);
        assertEquals(Integer.valueOf(10200), read.uid);
        assertEquals(Long.valueOf(1729412345678L), read.earliestFirstInstallTime);
        assertTrue(read.isStillValidFor(10200, 1729412345678L));
    }

    @Test
    public void validityRequiresSameUidAndInstallTime() throws Exception {
        File file = temporaryFolder.newFile("10200");
        AllowDeclaration.create(10200, 1000L).write(file);

        AllowDeclaration read = AllowDeclaration.read(file);
        assertNotNull(read);
        assertTrue(read.isStillValidFor(10200, 1000L));
        assertFalse(read.isStillValidFor(10201, 1000L));
        assertFalse(read.isStillValidFor(10200, 1001L));
    }

    @Test
    public void malformedOrIncompleteJsonIsRejected() throws Exception {
        File missingFields = temporaryFolder.newFile("1");
        try (FileWriter writer = new FileWriter(missingFields)) {
            writer.write("{\"uid\":10200}");
        }
        assertNull(AllowDeclaration.read(missingFields));

        File invalidUid = temporaryFolder.newFile("2");
        try (FileWriter writer = new FileWriter(invalidUid)) {
            writer.write("{\"uid\":0,\"earliest_first_install_time\":1000}");
        }
        assertNull(AllowDeclaration.read(invalidUid));

        File garbage = temporaryFolder.newFile("3");
        try (FileWriter writer = new FileWriter(garbage)) {
            writer.write("not json");
        }
        assertNull(AllowDeclaration.read(garbage));

        assertNull(AllowDeclaration.read(new File(temporaryFolder.getRoot(), "missing")));
    }
}
