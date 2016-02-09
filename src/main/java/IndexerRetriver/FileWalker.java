package IndexerRetriver;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileWalker extends SimpleFileVisitor<Path> {
	private final PathMatcher matcher;
	private FileAction fileAction;
	private int numMatches = 0;

	public FileWalker(String pattern, FileAction action) {
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		fileAction = action;
	}

	void find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			numMatches++;
//			System.out.println(file);
			fileAction.doWithMatchingFiles(file);
		}
	}

	void done() {
		System.out.println("Matched: " + numMatches);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		find(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		find(dir);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}
}
