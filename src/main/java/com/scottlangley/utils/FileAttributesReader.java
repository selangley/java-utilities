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
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for reading the file attributes available via the Java NIO package in a safe, cross-platform manner.
 *
 * @author Scott Langley (https://github.com/selangley)
 * @see <a href="https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/test/java/nio/file/Files/FileAttributes.java">https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/test/java/nio/file/Files/FileAttributes.java</a>
 */
public class FileAttributesReader {

    // BasicFileAttributes
    private FileTime lastModifiedTime;
    private FileTime lastAccessTime;
    private FileTime creationTime;
    private Long lastModifiedTimeSec;
    private Long lastAccessTimeSec;
    private Long creationTimeSec;
    private Boolean isRegularFile;
    private Boolean isDirectory;
    private Boolean isSymbolicLink;
    private Boolean isOther;
    private Long size;
    private Object fileKey;

    // DosFileAttributes
    private Boolean isReadOnly;
    private Boolean isHidden;
    private Boolean isArchive;
    private Boolean isSystem;

    // PosixFileAttributes
    private String userName;
    private Integer userId;
    private String groupName;
    private Integer groupId;

    // Values possibly available from fileKey object.
    private String dev = null;
    private String inode = null;

    // Posix File Permissions
    private Set<PosixFilePermission> posixFilePermissions;

    private Boolean isOwnerRead;
    private Boolean isOwnerWrite;
    private Boolean isOwnerExecute;
    private Boolean isGroupRead;
    private Boolean isGroupWrite;
    private Boolean isGroupExecute;
    private Boolean isOthersRead;
    private Boolean isOthersWrite;
    private Boolean isOthersExecute;

    private List<AclEntry> accessControlList = null;

    /**
     * The basic file attributes.
     */
    BasicFileAttributes basicFileAttributes = null;

    /**
     * The posix file attributes.
     */
    PosixFileAttributes posixFileAttributes = null;

    /**
     * The dos file attributes.
     */
    DosFileAttributes dosFileAttributes = null;

    private final static Pattern fileKeyPattern = Pattern.compile("^\\(dev=(\\w+),ino=(\\w+)\\)$");
    private final static String OS_NAME = System.getProperty("os.name").toUpperCase();

    /**
     * The Constant IS_WINDOWS.
     */
    public final static Boolean IS_WINDOWS = OS_NAME.startsWith("WINDOWS");

    /**
     * The Constant IS_LINUX.
     */
    public final static Boolean IS_LINUX = (!IS_WINDOWS) && OS_NAME.startsWith("LINUX");

    /**
     * The Constant IS_MAC_OSX.
     */
    public final static Boolean IS_MAC_OSX = (!IS_WINDOWS) && (!IS_LINUX) && OS_NAME.startsWith("MAC OS X");
    private Path path = null;
    private Boolean fileKeyAvailable = false;
    private String failureMessage = null;

    /**
     * Instantiates a new file attributes reader.
     *
     * @param file the file
     */
    public FileAttributesReader(File file) {
        this(file.toPath());
    }

    /**
     * Instantiates a new file attributes reader.
     *
     * @param path the path
     */
    public FileAttributesReader(Path path) {
        this.path = path;
        try {
            if (IS_WINDOWS) {
                dosFileAttributes = Files.readAttributes(path, DosFileAttributes.class);
                basicFileAttributes = dosFileAttributes;
                processDosFileAttributes();
                fileKeyAvailable = true;
            } else {
                posixFileAttributes = Files.readAttributes(path, PosixFileAttributes.class);
                basicFileAttributes = posixFileAttributes;
                processPosixFileAttributes();
                fileKeyAvailable = true;
            }
        } catch (FileSystemException | UnsupportedOperationException e) {
            failureMessage = e.getMessage();
            try {
                basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
                processBasicFileAttributes();
                fileKeyAvailable = true;
            } catch (IOException ioe) {
                fileKeyAvailable = false;
                failureMessage += ";" + ioe.getMessage();
            }
        } catch (IOException ioe) {
            fileKeyAvailable = false;
            failureMessage = ioe.getMessage();
        }
    }

    private void processBasicFileAttributes() {
        this.lastModifiedTime = basicFileAttributes.lastModifiedTime();
        this.lastModifiedTimeSec = basicFileAttributes.lastModifiedTime().to(TimeUnit.SECONDS);
        this.creationTime = basicFileAttributes.creationTime();
        this.creationTimeSec = basicFileAttributes.creationTime().to(TimeUnit.SECONDS);
        this.lastAccessTime = basicFileAttributes.lastAccessTime();
        this.lastAccessTimeSec = basicFileAttributes.lastAccessTime().to(TimeUnit.SECONDS);
        isRegularFile = basicFileAttributes.isRegularFile();
        isDirectory = basicFileAttributes.isDirectory();
        isSymbolicLink = basicFileAttributes.isSymbolicLink();
        isOther = basicFileAttributes.isOther();
        size = basicFileAttributes.size();
        fileKey = basicFileAttributes.fileKey();
    }

    private void processDosFileAttributes() {
        processBasicFileAttributes();
        isReadOnly = dosFileAttributes.isReadOnly();
        isHidden = dosFileAttributes.isHidden();
        isArchive = dosFileAttributes.isArchive();
        isSystem = dosFileAttributes.isSystem();
    }

    private void processPosixFileAttributes() {
        processBasicFileAttributes();
        UserPrincipal ownerUserPrincipal = posixFileAttributes.owner();
        GroupPrincipal groupPrincipal = posixFileAttributes.group();
        userName = ownerUserPrincipal.getName();
        userId = ownerUserPrincipal.hashCode();
        groupName = groupPrincipal.getName();
        groupId = groupPrincipal.hashCode();
        posixFilePermissions = posixFileAttributes.permissions();
        processPosixFilePermissions();

    }

    private void processPosixFilePermissions() {
        isOwnerRead = false;
        isOwnerWrite = false;
        isOwnerExecute = false;
        isGroupRead = false;
        isGroupWrite = false;
        isGroupExecute = false;
        isOthersRead = false;
        isOthersWrite = false;
        isOthersExecute = false;

        for (PosixFilePermission pfp : posixFilePermissions) {
            switch (pfp) {
                case OWNER_READ:
                    isOwnerRead = true;
                    break;
                case OWNER_WRITE:
                    isOwnerWrite = true;
                    break;
                case OWNER_EXECUTE:
                    isOwnerExecute = true;
                    break;
                case GROUP_READ:
                    isGroupRead = true;
                    break;
                case GROUP_WRITE:
                    isGroupWrite = true;
                    break;
                case GROUP_EXECUTE:
                    isGroupExecute = true;
                    break;
                case OTHERS_READ:
                    isOthersRead = true;
                    break;
                case OTHERS_WRITE:
                    isOthersWrite = true;
                    break;
                case OTHERS_EXECUTE:
                    isOthersExecute = true;
                    break;
            }
        }
    }


    /**
     * Gets the posix file permissions as string.
     *
     * @return the posix file permissions as string
     */

    public String getPosixFilePermissionsAsString() {
        return PosixFilePermissions.toString(posixFilePermissions);
    }

    /**
     * Gets the posix file permissions in numeric form.
     *
     * @return the posix file permissions in numeric form
     */

    public String getPosixFilePermissionsInNumericForm() {
        return "" + convertPermissionsFlagsToInteger(isOwnerRead, isOwnerWrite, isOwnerExecute)
                + convertPermissionsFlagsToInteger(isGroupRead, isGroupWrite, isGroupExecute)
                + convertPermissionsFlagsToInteger(isOthersRead, isOthersWrite, isOthersExecute);
    }

    private Integer convertPermissionsFlagsToInteger(Boolean readFlag, Boolean writeFlag, Boolean executeFlag) {
        return ((readFlag) ? 4 : 0) + ((writeFlag) ? 2 : 0) + ((executeFlag) ? 1 : 0);
    }

    private void parseFileKey() {
        String fileKeyString = fileKey.toString();
        Matcher m = fileKeyPattern.matcher(fileKeyString);
        if (m.matches()) {
            dev = m.group(1);
            inode = m.group(2);
        }
    }

    /**
     * Gets the device
     *
     * @return the dev
     */
    public String getDev() {
        if (dev == null) {
            parseFileKey();
        }
        return dev;
    }

    /**
     * Gets the inode.
     *
     * @return the inode
     */
    public String getInode() {
        if (dev == null) {
            parseFileKey();
        }
        return inode;
    }


    /**
     * Gets the last modified time.
     *
     * @return the last modified time
     */
    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Gets the last access time.
     *
     * @return the last access time
     */
    public FileTime getLastAccessTime() {
        return lastAccessTime;
    }

    /**
     * Gets the creation time.
     *
     * @return the creation time
     */
    public FileTime getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the last modified time in seconds
     *
     * @return the last modified time sec
     */
    public Long getLastModifiedTimeSec() {
        return lastModifiedTimeSec;
    }

    /**
     * Gets the last access time in seconds.
     *
     * @return the last access time sec
     */
    public Long getLastAccessTimeSec() {
        return lastAccessTimeSec;
    }

    /**
     * Gets the creation time in seconds.
     *
     * @return the creation time sec
     */
    public Long getCreationTimeSec() {
        return creationTimeSec;
    }

    /**
     * Checks if is regular file.
     *
     * @return the boolean
     */
    public Boolean isRegularFile() {
        return isRegularFile;
    }

    /**
     * Checks if is directory.
     *
     * @return the boolean
     */
    public Boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Checks if is symbolic link.
     *
     * @return the boolean
     */
    public Boolean isSymbolicLink() {
        return isSymbolicLink;
    }

    /**
     * Checks if is other.
     *
     * @return the boolean
     */
    public Boolean isOther() {
        return isOther;
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public Long getSize() {
        return size;
    }

    /**
     * Gets the file key.
     *
     * @return the file key
     */
    public Object getFileKey() {
        return fileKey;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets the group id.
     *
     * @return the group id
     */
    public Integer getGroupId() {
        return groupId;
    }

    /**
     * Gets the access control list.
     *
     * @return the access control list
     */
    public List<AclEntry> getAccessControlList() {
        return accessControlList;
    }

    /**
     * Checks if is read only.
     *
     * @return the boolean
     */
    public Boolean isReadOnly() {
        return isReadOnly;
    }

    /**
     * Checks if is hidden.
     *
     * @return the boolean
     */
    public Boolean isHidden() {
        return isHidden;
    }

    /**
     * Checks if is archive.
     *
     * @return the boolean
     */
    public Boolean isArchive() {
        return isArchive;
    }

    /**
     * Checks if is system.
     *
     * @return the boolean
     */
    public Boolean isSystem() {
        return isSystem;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Checks if is file key available.
     *
     * @return the boolean
     */
    public Boolean isFileKeyAvailable() {
        return fileKeyAvailable;
    }

    /**
     * Gets the failure message if an exception was thrown retrieving the file attributes.
     *
     * @return the failure message
     */
    public String getFailureMessage() {
        return failureMessage;
    }

    /**
     * Gets the basic file attributes.
     *
     * @return the basic file attributes
     */
    public BasicFileAttributes getBasicFileAttributes() {
        return basicFileAttributes;
    }

    /**
     * Gets the posix file attributes.
     *
     * @return the posix file attributes
     */
    public PosixFileAttributes getPosixFileAttributes() {
        return posixFileAttributes;
    }

    /**
     * Gets the DOS file attributes.
     *
     * @return the DOS file attributes
     */
    public DosFileAttributes getDosFileAttributes() {
        return dosFileAttributes;
    }

}