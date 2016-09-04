package runawfe.bp.helper.utils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author egorlitvinenko
 *
 */
public class SearchTextMain {

    public static void main(String[] args) {
    }

    public static void printResult(final Map<Path, List<String>> result) {
        printResult(result, System.out);
    }

    public static void printResult(final Map<Path, List<String>> result, final PrintStream out) {
        final Set<String> bpNames = new LinkedHashSet<>();
        result.keySet().forEach((Path path) -> {
            out.println("");
            out.println(path.resolve(""));
            bpNames.add(path.getParent().toString());
            result.get(path).forEach((String text) -> {
                out.println("\t" + text);
            });
        });
        out.println("\nБизнес процессы (" + bpNames.size() + "): ");
        bpNames.forEach((String bp) -> out.println(bp));
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
    public static Map<Path, List<String>> searchString(final String rootFolder, final List<String> texts, final int depth) throws IOException {
        final List<Pattern> patterns = new ArrayList<>();
        texts.forEach((String text) -> patterns.add(Pattern.compile(text)));
        return searchPattern(rootFolder, patterns, depth, false);
    }

    public static Map<Path, List<String>> searchPattern(final Set<String> rootFolders, final Set<String> excludeFiles, final List<Pattern> texts,
            final int depth, final boolean and) {
        final Map<Path, List<String>> result = new HashMap<Path, List<String>>();
        rootFolders.forEach(path -> {
            Map<Path, List<String>> temp;
            try {
                temp = searchPattern(path, texts, depth, and);
                final Set<Path> toRemove = new HashSet<>();
                excludeFiles.forEach(excludeField -> {
                    temp.keySet().forEach(tempPath -> {
                        if (tempPath.resolve("").toString().endsWith(excludeField)) {
                            toRemove.add(tempPath);
                        }
                    });
                });
                toRemove.forEach(tempPath -> temp.remove(tempPath));
                result.putAll(temp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return result;
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
    public static Map<Path, List<String>> searchPattern(final String rootFolder, final List<Pattern> texts, final int depth) throws IOException {
        return searchPattern(rootFolder, texts, depth, false);
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
    public static Map<Path, List<String>> searchPattern(final String rootFolder, final List<Pattern> texts, final int depth, final boolean and)
            throws IOException {
        final Map<Path, List<String>> results = new LinkedHashMap<>();
        final Path root = Paths.get(rootFolder);
        Files.walkFileTree(root, Collections.emptySet(), depth, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.resolve("").toString().endsWith("~")) {
                    return FileVisitResult.CONTINUE;
                }
                final List<String> findTexts = new LinkedList<>();
                try (Scanner scanner = new Scanner(file)) {
                    for (int lineNumber = 1; scanner.hasNextLine(); ++lineNumber) {
                        final String line = scanner.nextLine();
                        boolean allMatched = true;
                        for (Pattern token : texts) {
                            // scanner.findWithHorizon(token, Integer.MAX_VALUE);
                            Matcher matcher = token.matcher(line);
                            final boolean matched = matcher.find();
                            if (matched && !and) {
                                findTexts.add(lineNumber + " : " + line);
                            } else if (!matched) {
                                allMatched = false;
                            }
                        }
                        if (allMatched && and) {
                            findTexts.add(lineNumber + " : " + line);
                        }
                    }
                }
                if (!findTexts.isEmpty()) {
                    results.put(file, findTexts);
                }
                return FileVisitResult.CONTINUE;
            }

        });
        return results;
    }

}
