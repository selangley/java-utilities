/*
* Copyright 2018 Scott Langley
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.scottlangley.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

/**
 * Test the FileAttributesReader utility class for accessing file-level attributes available via the Java NIO package.
 *
 * @author Scott Langley (https://github.com/selangley)
 */
public class FileAttributesReaderJUnit4Test {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testBasicFileAttributes() throws IOException {
        File createdFile = tempFolder.newFile("basic.txt");
        byte[] testBytes = {1};
        Long currentTimeInMilliseconds = new Date().getTime();
        java.nio.file.Files.write(createdFile.toPath(), testBytes, StandardOpenOption.SYNC);
        FileAttributesReader fileAttributes = new FileAttributesReader(createdFile);

        assertTrue(isWithinOneSecond(fileAttributes.getCreationTimeSec(), currentTimeInMilliseconds));
        assertTrue(isWithinOneSecond(fileAttributes.getLastAccessTimeSec(), currentTimeInMilliseconds));
        assertTrue(isWithinOneSecond(fileAttributes.getLastModifiedTimeSec(), currentTimeInMilliseconds));
        assertNotNull(fileAttributes.getCreationTime());
        assertNotNull(fileAttributes.getLastAccessTime());
        assertNotNull(fileAttributes.getLastModifiedTime());
        assertTrue(fileAttributes.isRegularFile());
        assertFalse(fileAttributes.isDirectory());
        assertFalse(fileAttributes.isSymbolicLink());
        assertFalse(fileAttributes.isOther());
        assertEquals(new Long(1), fileAttributes.getSize());
    }

    @Test
    public void testFileKeyAttributes() throws IOException {
        Assume.assumeFalse(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("testfile.txt");
        FileAttributesReader fileAttributes = new FileAttributesReader(createdFile);

        assertNotNull(fileAttributes.getFileKey());
        assertNotNull(fileAttributes.getDev());
        assertNotNull(fileAttributes.getInode());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testPosixFileAttributes()
            throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            ClassNotFoundException, InstantiationException, NoSuchMethodException, SecurityException {
        Assume.assumeFalse(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("posixfile.txt");
        FileAttributesReader fileAttributes = new FileAttributesReader(createdFile);
        String UNIX_AUTH_MODULE_CLASSNAME = "com.sun.security.auth.module.UnixSystem";
        String UNIX_AUTH_MODULE_USERNAME_METHOD = "getUsername";
        String UNIX_AUTH_MODULE_UID_METHOD = "getUid";
        String UNIX_AUTH_MODULE_GID_METHOD = "getGid";
        Class UnixSystem = Class.forName(UNIX_AUTH_MODULE_CLASSNAME);
        Object unixSystem = UnixSystem.newInstance();
        Method getUserName = UnixSystem.getMethod(UNIX_AUTH_MODULE_USERNAME_METHOD);
        Method getUid = UnixSystem.getMethod(UNIX_AUTH_MODULE_UID_METHOD);
        Method getGid = UnixSystem.getMethod(UNIX_AUTH_MODULE_GID_METHOD);
        Integer gid = ((Long) getGid.invoke(unixSystem)).intValue();
        Integer uid = ((Long) getUid.invoke(unixSystem)).intValue();
        String userName = (String) getUserName.invoke(unixSystem);

        assertEquals(gid, fileAttributes.getGroupId());
        assertEquals(uid, fileAttributes.getUserId());
        assertEquals(userName, fileAttributes.getUserName());
        assertNotNull(fileAttributes.getGroupName());
    }

    @Test
    public void testPosixFilePermissions() throws IOException {
        Assume.assumeFalse(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("posixfilewithperms.txt");
        createdFile.setReadable(false, false);
        createdFile.setWritable(false, false);
        createdFile.setExecutable(false, false);
        FileAttributesReader fileAttributes = new FileAttributesReader(createdFile);

        assertEquals("---------", fileAttributes.getPosixFilePermissionsAsString());
        assertEquals("000", fileAttributes.getPosixFilePermissionsInNumericForm());

        createdFile.setReadable(true, true);
        createdFile.setWritable(true, true);
        createdFile.setExecutable(true, true);
        FileAttributesReader fileAttributes2 = new FileAttributesReader(createdFile);

        assertEquals("rwx------", fileAttributes2.getPosixFilePermissionsAsString());
        assertEquals("700", fileAttributes2.getPosixFilePermissionsInNumericForm());
    }

    @Test
    public void testDosFileAttributes() throws IOException {
        Assume.assumeTrue(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("dosfile.txt");
        FileAttributesReader fileAttributes = new FileAttributesReader(createdFile);

        assertFalse(fileAttributes.isReadOnly());
        assertFalse(fileAttributes.isHidden());
        assertTrue(fileAttributes.isArchive());
        assertFalse(fileAttributes.isSystem());
    }

    /* Java 7 friendly comparison within one second */
    private boolean isWithinOneSecond(Long epochSecond, Long epochMilliSeconds) {
        return Math.abs((epochMilliSeconds / 1000) - epochSecond) <= 1;
    }

}