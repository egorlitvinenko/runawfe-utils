package runawfe.bp.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ReplaceTextMain {

    public static void main(String[] args) {
    }

    /**
     * walk tree
     * 
     * @param rootFolder
     * @param texts
     * @param depth
     * @throws IOException,
     *             {@link RuntimeException} when IO problems
     */
    public static void replacePattern(final Set<String> rootFolders, final List<Pattern> texts, final List<String> replaces, final int depth) {
        rootFolders.forEach(folder -> {
            try {
                replacePattern(folder, texts, replaces, depth);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * walk tree
     * 
     * @param rootFolder
     * @param texts
     * @param depth
     * @throws IOException,
     *             {@link RuntimeException} when IO problems
     */
    public static void replacePattern(final String rootFolder, final List<Pattern> texts, final List<String> replaces, final int depth)
            throws IOException {
        final Path root = Paths.get(rootFolder);
        if (texts.size() != replaces.size()) {
            throw new RuntimeException("texts.size() != replaces.size()");
        }
        Files.walkFileTree(root, Collections.emptySet(), depth, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!SearchTextMain.isFileForSearching(file)) {
                    return FileVisitResult.CONTINUE;
                }
                String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                for (int i = 0; i < texts.size(); ++i) {
                    final Pattern token = texts.get(i);
                    final String replace = replaces.get(i);
                    content = content.replaceAll(token.pattern(), replace);
                }
                Files.write(file, content.getBytes(StandardCharsets.UTF_8));
                return FileVisitResult.CONTINUE;
            }

        });
    }

}
