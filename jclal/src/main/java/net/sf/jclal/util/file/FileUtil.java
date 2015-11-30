/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.jclal.util.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.core.Utils;

/**
 * Utility class to handle a file.
 *
 * @author Oscar Reyes Pupo
 * @author Eduardo Perez Perdomo
 */
public class FileUtil {

    /**
     * Creates a file
     *
     * @param file The file to create
     * @param replace If the file must be replaced.
     * @throws Exception The exception that will launched
     */
    public static void createFile(File file, boolean replace) throws Exception {

        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (replace) {
                file.delete();
                file.createNewFile();
            } else {
                throw new Exception("The file " + file.getAbsolutePath()
                        + " already exists.");
            }
        } else {
            file.createNewFile();
        }
    }

    /**
     * Creates a temporal file
     *
     * @param prefix The prefix of the file
     * @param suffix The suffix of the file
     * @return The temporal file created
     */
    public static File createTempFile(String prefix, String suffix) {

        try {

            Date date = new Date(System.currentTimeMillis());
            String time = date.toString().replaceAll(" ", "_").replaceAll(":", "-");
            String newName = prefix + "_" + time;

            return File.createTempFile(newName, suffix);

        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Utilitarian method to obtain all the files of a directory that fulfill
     * with a condition.
     *
     * @param directory The directory that will be analyzed.
     * @param filter The filter that will be used.
     * @param listToFill The list where there will be stored the files that
     * expire with the filter.
     */
    public static void getFilesInDirectory(File directory, FilenameFilter filter, List<File> listToFill) {

        if (directory.exists()) {

            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    getFilesInDirectory(file, filter, listToFill);
                }
            } else if (directory.isFile()) {
                if (filter.accept(directory, directory.getName())) {
                    listToFill.add(directory);
                }
            }

        }
    }

    /**
     *
     * @param files The files to order.
     * @param ascendentOrder True if the files will be ordered in ascendent
     * order according with the path, false otherwise.
     */
    public static void orderFilesByPathName(List<File> files, final boolean ascendentOrder) {
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                int value = file1.getPath().compareToIgnoreCase(file2.getPath());
                if (ascendentOrder) {
                    return value;
                }
                return -value;
            }
        });
    }

    /**
     *
     * @param file The file where the content will be stored, create a new file
     * if it does not exist, the content will be written to the end of the file
     * rather than the beginning
     * @param content The content
     * @throws IOException The exception to launch
     */
    public static void writeFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    /**
     *
     * @param file The file of which the content will be read
     * @return The content
     * @throws IOException The exception to launch
     */
    public static String readFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }

    /**
     *
     * @param file The file of which the content will be read
     * @return The string reader
     * @throws IOException The exception to launch
     */
    public static StringReader stringReader(File file) throws IOException {
        return new StringReader(readFile(file));
    }

    /**
     * Method that allows to load a file to be read that is located inside the
     * source code of a program, ej: config/util.props, if the file cannot be
     * taken to URI format, a copy of his content is created in the temporary
     * folder of the system and the above mentioned file it is returned
     *
     * @param fileSourcePath The path to the source
     * @return The file
     * @throws URISyntaxException The exceptions to launch
     * @throws IOException The exceptions to launch
     */
    public static File loadFileSourceCode(String fileSourcePath) throws URISyntaxException, IOException {
        Utils util = new Utils();
        URL url = util.getClass().getClassLoader().getResource(fileSourcePath);
        File x;
        try {
            x = new File(url.toURI());
        } catch (Exception e) {

            InputStream input = url.openStream();

            File temp = new File(fileSourcePath);
            x = File.createTempFile(temp.getName(), ".temp");

            Files.copy(input, x.toPath(), StandardCopyOption.REPLACE_EXISTING);

            input.close();
            input = null;
        }

        //clean
        util = null;
        url = null;

        return x;
    }

    /**
     * Delete a file if was created in the temporal directory of the system
     *
     * @param x	The file to delete
     * @throws IOException The exception to launch
     */
    public static void deleteFileIfTemp(File x) throws IOException {
        File test = File.createTempFile("test", null);
        if (test.getParentFile().equals(x.getParentFile())) {
            x.delete();
        }
        test.delete();
        test = null;
    }

    /**
     * Make a copy of a file in the temporary folder of the system
     *
     * @param origin The file to copy
     * @return The file copied
     * @throws IOException The exception to launch
     */
    public static File createTempCopyFile(File origin) throws IOException {
        File n = File.createTempFile("copy" + origin.getName(), null);
        copyFile(origin, n);
        return n;
    }

    /**
     * Make a copy of a file, if the destination file exists, then the
     * destination file is replaced
     *
     * @param origin The file to copy
     * @param destination The file copied
     * @throws IOException The exception to launch
     */
    public static void copyFile(File origin, File destination) throws IOException {
        Files.copy(origin.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}