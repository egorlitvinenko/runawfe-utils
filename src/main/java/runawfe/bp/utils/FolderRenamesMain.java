package runawfe.bp.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author egorlitvinenko
 *
 */
public class FolderRenamesMain {

    public static void main(String[] args) {
        final String ROOT_FOLDER = "src/main/resources/processes/";
        try {
            renameProcessFolders(ROOT_FOLDER, new INewNameGenerator() {
                private static final String PAR_FILES = ".par_FILES";

                @Override
                public boolean isNeedRenaming(String previousName) {
                    return previousName.endsWith(PAR_FILES);
                }

                @Override
                public String gen(String previousName) {
                    return previousName.contains(ROOT_FOLDER) ? previousName.replace(PAR_FILES, "")
                            : ROOT_FOLDER + previousName.replace(PAR_FILES, "");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param rootFolder
     *            - rootFolder/process_type_folder/process_folder
     * @param renamer
     * @throws IOException,
     *             {@link RuntimeException} when IO problems
     */
    public static void renameProcessFolders(final String rootFolder, INewNameGenerator renamer) throws IOException {
        Path path = FileSystems.getDefault().getPath(rootFolder);
        try (DirectoryStream<Path> processesFolder = Files.newDirectoryStream(path)) {
            processesFolder.forEach((Path processType) -> {
                System.out.println(processType.getFileName());
                try (DirectoryStream<Path> processes = Files.newDirectoryStream(processType)) {
                    processes.forEach((Path process) -> {
                        if (renamer.isNeedRenaming(process.getFileName().toString())) {
                            System.out.print("rename = " + process.getFileName());
                            Path newNameProcess = Paths.get(renamer.gen(process.resolve("").toString()));
                            try {
                                Files.move(process, newNameProcess);
                                System.out.println(" to " + newNameProcess.getFileName().toString());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw e;
        }
    }

    public static interface INewNameGenerator {
        String gen(String previousName);

        boolean isNeedRenaming(String previousName);
    }

}
