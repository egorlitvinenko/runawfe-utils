package runawfe.bp.helper.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileLineTransformer {

    private Map<Path, String> resultFilesContent;
    private final LineVisitor visitor;

    private FileLineTransformer(LineVisitor visitor) {
        this.visitor = visitor;
        this.resultFilesContent = new HashMap<>();
    }

    public static FileLineTransformer create(LineVisitor visitor) {
        return new FileLineTransformer(visitor);
    }

    public void transform(final List<Path> paths) {
        paths.forEach(this::transform);
    }

    public String getContent(final Path path) {
        return resultFilesContent.get(path);
    }

    public void transform(final Path sourcePath) {
        if (Files.isDirectory(sourcePath)) {
            try {
                Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        transformFile(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            transformFile(sourcePath);
        }
    }

    public void transformFile(final Path sourcePath) {
        if (Files.isDirectory(sourcePath)) {
            return;
        }
        final List<String> newLines = new ArrayList<>();
        Stream.of(sourcePath).flatMap(path -> {
            try {
                return Files.lines(path, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return Stream.of();
            }
        }).forEach(line -> {
            newLines.addAll(visitor.getNewLines(sourcePath, line, newLines));
        });
        if (!newLines.isEmpty()) {
            resultFilesContent.put(sourcePath, newLines.stream().collect(Collectors.joining("\n")));
        }
    }

    public static interface LineVisitor {

        List<String> getNewLines(final Path path, final String line, final List<String> resultLines);

    }

}
