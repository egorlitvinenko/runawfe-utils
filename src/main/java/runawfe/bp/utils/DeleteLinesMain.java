package runawfe.bp.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DeleteLinesMain {

    public static void main(String[] args) {
    }

    public static void deleteLines(final List<Path> paths, final Predicate<? super String> deletePredicate) {
        paths.forEach(path -> {
            try {
                deleteLines(path, deletePredicate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void deleteLines(final Path path, final Predicate<? super String> deletePredicate) throws Exception {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    final String content = FileLineTransformer.create(DeleteLine.with(deletePredicate)).getContent(path);
                    Files.write(file, content.getBytes(StandardCharsets.UTF_8));
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            final String content = FileLineTransformer.create(DeleteLine.with(deletePredicate)).getContent(path);
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static class DeleteLine implements FileLineTransformer.LineVisitor {

        private final Predicate<? super String> predicate;

        private DeleteLine(Predicate<? super String> deletePredicate) {
            this.predicate = deletePredicate;
        }

        public static DeleteLine with(Predicate<? super String> deletePredicate) {
            return new DeleteLine(deletePredicate);
        }

        @Override
        public List<String> getNewLines(Path path, String line, List<String> resultLines) {
            final List<String> result = new ArrayList<>();
            if (!predicate.test(line)) {
                result.add(line);
            }
            return result;
        }

    }

}
