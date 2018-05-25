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

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Test the UnixFileAttributesReader
 *
 * @author Scott Langley (https://github.com/selangley)
 */
public class UnixFileAttributesReaderJUnit4Test extends FileAttributesReaderJUnit4Test {

    UnixFileAttributesReader createFileAttributesReader(File createdFile) {
        return new UnixFileAttributesReader(createdFile);
    }

    @Test
    public void testUnixAttributes() throws Exception {
        Assume.assumeFalse(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("unixfile.txt");
        UnixFileAttributesReader fileAttributes = createFileAttributesReader(createdFile);

        assertNotNull(fileAttributes.getMode());
        assertNotNull(fileAttributes.getRdev());
        assertNotNull(fileAttributes.getNlink());
        assertNotNull(fileAttributes.getCtime());
        assertNotNull(fileAttributes.isDevice());
    }

    @Test
    public void testWindowsAndUnixAttributes() throws Exception {
        Assume.assumeFalse(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("windowsfile.txt");
        DeepFileAttributesReader fileAttributes = DeepFileAttributesReader.createOsSpecificReader(createdFile);
        // Windows-only attributes
        assertNull(fileAttributes.getAttributes());
        assertNull(fileAttributes.getFileIndexLow());
        assertNull(fileAttributes.getFileIndexHigh());
        assertNull(fileAttributes.getVolSerialNumber());
        assertNull(fileAttributes.isDirectoryLink());
        assertNull(fileAttributes.isReparsePoint());
        // UNIX-only attributes
        assertNotNull(fileAttributes.getMode());
        assertNotNull(fileAttributes.getRdev());
        assertNotNull(fileAttributes.getNlink());
        assertNotNull(fileAttributes.getCtime());
        assertNotNull(fileAttributes.isDevice());
    }
}
