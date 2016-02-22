package IndexerRetriver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import HTMLParser.ParsedDocument;
import HTMLParser.Parser;

public class Indexer {

	@Parameter(names={"--index", "-i"}, required=true)
	private String indexPath;
	@Parameter(names={"--docs", "-d"}, required=true)
	private String inputFilesPath;

	public void index() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		Directory dir = FSDirectory.open(Paths.get(indexPath));
		final IndexWriter writer = new IndexWriter(dir, conf);
		
		FileWalker walker = new FileWalker("*.{html,htm}", new FileAction() {
			
			public void doWithMatchingFiles(Path path) {
				System.out.println(path);
				Parser p = new Parser(path);
				try {
					ParsedDocument doc = p.parse();
					Document luceneDoc = new Document();
					luceneDoc.add(new TextField("title", doc.getTitle(), Store.YES));
					luceneDoc.add(new TextField("body", doc.getBody(), Store.YES));
					luceneDoc.add(new TextField("url", path.toString(), Store.YES));
					writer.addDocument(luceneDoc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		Path startingDir = Paths.get(inputFilesPath);
		
		try {
			Files.walkFileTree(startingDir, walker);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		walker.done();
		writer.close();
	}

	public static void main(String[] args) throws URISyntaxException, IOException {
		System.out.println("Args are " + args);
		Indexer indexer = new Indexer();
		new JCommander(indexer, args);
		indexer.index();
	}
}
