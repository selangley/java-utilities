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
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
/**
 * Test the WindowsFileAttributeReader
 * 
 * @author Scott Langley (https://github.com/selangley)
 *
 */
public class WindowsFileAttributesReaderJUnit4Test extends FileAttributesReaderJUnit4Test {
    @Test
    public void testWindowsAttributes() throws Exception {
        Assume.assumeTrue(FileAttributesReader.IS_WINDOWS);
        File createdFile = tempFolder.newFile("windowsfile.txt");
        WindowsFileAttributesReader fileAttributes = new WindowsFileAttributesReader(createdFile);

        assertNotNull( fileAttributes.getAttributes());
        assertNotNull( fileAttributes.getFileIndexLow());
        assertNotNull( fileAttributes.getFileIndexHigh());
        assertNotNull( fileAttributes.getVolSerialNumber());
        assertNotNull( fileAttributes.isDirectoryLink());
        assertNotNull( fileAttributes.isReparsePoint());
    }
}
