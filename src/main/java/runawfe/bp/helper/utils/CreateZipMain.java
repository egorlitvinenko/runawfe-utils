package runawfe.bp.helper.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author egorlitvinenko
 *
 */
public class CreateZipMain {

    public static void main(String[] args) {
        try {
            final String ROOT_FOLDER = "src/main/resources/processes/";
            final Pattern inputAddress = Pattern.compile("InputAddress");
            Map<Path, List<String>> result = SearchTextMain.searchPattern(ROOT_FOLDER, Arrays.asList(inputAddress), Integer.MAX_VALUE, false);
            Set<String> bpNames = new LinkedHashSet<>();
            result.keySet().forEach((Path path) -> bpNames.add(path.getParent().toString()));

            createZip(bpNames, "par");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void createZip(Collection<String> folders, final String ext) {
        folders.forEach((String folder) -> {
            try {
                createZip(folder, folder + "." + ext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void createZip(Collection<String> folders, final String newFolder, final String ext) {
        folders.forEach((String folder) -> {
            try {
                Path folderPath = Paths.get(folder);
                createZip(folder, newFolder + File.separator + folderPath.getFileName() + "." + ext);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void createZip(final String rootFolder, final String resultAbsoluteFileName) throws IOException {
        final Path dir = Paths.get(rootFolder);
        final File zipFile = Paths.get(resultAbsoluteFileName).toFile();
        try (ZipOutputStream output = new ZipOutputStream(new FileOutputStream(zipFile))) {
            DirectoryStream<Path> files = Files.newDirectoryStream(dir);
            files.forEach((Path file) -> addToZipFile(file, output));
        }
    }

    /**
     * http://www.thecoderscorner.com/team-blog/java-and-jvm/63-writing-a-zip-file-in-java-using-zipoutputstream Adds an extra file to the zip
     * archive, copying in the created date and a comment.
     * 
     * @param file
     *            file to be archived
     * @param zipStream
     *            archive to contain the file.
     */
    private static void addToZipFile(Path file, ZipOutputStream zipStream) {
        String inputFileName = file.toFile().getPath();
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            ZipEntry entry = new ZipEntry(file.toFile().getName());
            entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));
            entry.setComment("Created by " + System.getProperty("user.name"));
            zipStream.putNextEntry(entry);
            byte[] readBuffer = new byte[2048];
            int amountRead;
            while ((amountRead = inputStream.read(readBuffer)) > 0) {
                zipStream.write(readBuffer, 0, amountRead);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
