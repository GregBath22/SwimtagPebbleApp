package com.example.app;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Greg on 14/07/2014.
 */
public class zip {

   public boolean zipFolder() {
        String sourceFolder = new String("/storage/emulated/0/SWIM APP DATA");
        String toLocation = new String("/storage/emulated/0/DCIM/test.zip");
        final int BUFFER = 2048;

            File sourceFile = new File(sourceFolder);
            try {
                BufferedInputStream origin = null;
                FileOutputStream dest = new FileOutputStream(toLocation);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                if(sourceFile.isDirectory()) {
                    zipSubFolder(out, sourceFile, sourceFile.getParent().length());
                }else{
                    byte[] data = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(sourceFolder);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(String.valueOf(sourceFolder));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        private void zipSubFolder(ZipOutputStream out, File folder,
                                  int basePathLength) throws IOException {

            final int BUFFER = 2048;

            File[] fileList = folder.listFiles();
            BufferedInputStream origin = null;
            for (File file : fileList) {
                if (file.isDirectory()) {
                    zipSubFolder(out, file, basePathLength);
                } else {
                    byte data[] = new byte[BUFFER];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath
                            .substring(basePathLength);
                    //Log.i("ZIP SUBFOLDER", "Relative Path : " + relativePath);
                    FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(relativePath);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        }

        /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
        public String getLastPathComponent(String filePath) {
            String[] segments = filePath.split("/");
            String lastPathComponent = segments[segments.length - 1];
            return lastPathComponent;
        }

    }


