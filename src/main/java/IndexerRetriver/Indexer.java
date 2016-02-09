package IndexerRetriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import HTMLParser.ParsedDocument;
import HTMLParser.Parser;

public class Indexer {

	public static void main(String[] args){
		FileWalker walker = new FileWalker("*.{html,htm}", new FileAction() {
			
			public void doWithMatchingFiles(Path path) {
				System.out.println(path);
				Parser p = new Parser(path);
				try {
					ParsedDocument doc = p.parse();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		
		Path startingDir = Paths.get(args[0]);
		
		try {
			Files.walkFileTree(startingDir, walker);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		walker.done();
	}
}
