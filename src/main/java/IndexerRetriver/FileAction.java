package IndexerRetriver;

import java.nio.file.Path;

public interface FileAction {
	public void doWithMatchingFiles(Path path);
}
