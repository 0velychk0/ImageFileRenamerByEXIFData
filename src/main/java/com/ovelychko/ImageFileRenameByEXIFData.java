package com.ovelychko;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Slf4j
public class ImageFileRenameByEXIFData {

    private static final String destination_dir =   "/Users/Oleksandr_Velychko2/Downloads/Image_Test/";
    private static final String source_dir =        "/Users/Oleksandr_Velychko2/Downloads/Image_Test/";

    public static void main(String[] argv) throws ImageProcessingException, IOException {
        File imageFile = new File(source_dir);
        printDateTime(imageFile);
        log.warn("Scan is done for: {}", imageFile.getAbsolutePath());
    }

    private static void printDateTime(File imageFile) {
        if (imageFile == null)
            return;

        if (imageFile.isFile()) {
            Metadata metadata = null;
            try {
                metadata = ImageMetadataReader.readMetadata(imageFile);
            } catch (Exception ex) {
                log.error("file {} read exception: {}", imageFile.getAbsolutePath(), ex.toString());
                return;
            }

            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (directory == null) {
                log.error("Cannot read: {}", imageFile.getName() + " - FAILED");
                return;
            }

            Date date1 = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);

            if (date1 == null)
                date1 = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_DIGITIZED);

            if (date1 != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
                String str = simpleDateFormat.format(date1);
                renameFile(imageFile, str, getFileExtension(imageFile.getName()).toLowerCase());
            }
        } else {
            Arrays.stream(imageFile.listFiles()).forEach(file -> printDateTime(file));
        }
    }

    private static void renameFile(File file, String newName, String extension) {

        String newFullName;
        // If destination_dir is not set then use file path
        if (destination_dir == null || destination_dir.isEmpty()) {
            int index = file.getAbsolutePath().lastIndexOf(file.getName());
            String path = file.getAbsolutePath().substring(0, index);
            newFullName = path + newName + extension;
        } else
            newFullName = destination_dir + newName + extension;

        if (file.getAbsolutePath().equals(newFullName)) {
            log.warn("File already renamed : {}", file.getAbsolutePath());
            return;
        }

        File checkFile = new File(newFullName);

        if (checkFile.exists()) {
            log.error("Cannot rename file {} because new name is used {}", file.getAbsolutePath(), newFullName);
            return;
        }

        // Rename file (or directory)
        boolean success = file.renameTo(checkFile);

        if (!success) {
            log.error("File was not successfully renamed: {}", file.getAbsolutePath());
        } else {
            log.debug("File renamed successfully: {}", file.getAbsolutePath());
        }
    }

    private static String getFileExtension(String name) {
        if (name == null && name.length() == 0)
            return "";

        int i = name.lastIndexOf(".");

        if (i < 0) return "";

        return name.substring(i);
    }


    // Print all available EXIF data
    private static void printAll() throws ImageProcessingException, IOException {
        File imageFile = new File(source_dir);

        Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                System.out.format("[%s] - %s = %s \n",
                        directory.getName(), tag.getTagName(), tag.getDescription());
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.err.format("ERROR: %s", error);
                }
            }
        }
    }
}
