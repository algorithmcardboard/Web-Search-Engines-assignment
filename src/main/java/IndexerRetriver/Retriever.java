package IndexerRetriver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class Retriever {

	@Parameter(names={"--index", "-i"}, required=false)
	private String indexPath = "../index";
	
	@Parameter(names={"--query", "-q"}, required=true)
	private String query="";
	
	public Retriever(String path) {
		this.indexPath = path;
	}
	
	public Retriever(){
	}

	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("Arguments are "+args);
		Retriever ret = new Retriever();
		new JCommander(ret, args);
		List<Map<String,String>> results = ret.search(ret.query);
		for(Map<String, String> res : results){
			System.out.println(res.get("title"));
		}
	}

	public List<Map<String,String>> search(String searchTerm) throws IOException, ParseException {
		System.out.println("CAlling search with "+searchTerm);
		Directory directory = FSDirectory.open(Paths.get(indexPath));
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("body", analyzer);
		Query query = parser.parse(searchTerm);

		TopDocs docs = searcher.search(query, 10);
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

		for (int i = 0; i < docs.scoreDocs.length; i++) {
			Document doc = searcher.doc(docs.scoreDocs[i].doc);
			
			Map<String, String> docMap = new HashMap<String, String>();
			docMap.put("title", doc.getField("title").stringValue());
			docMap.put("url", doc.getField("url").stringValue());
			resultList.add(docMap);
//			System.out.println(doc.getField("title").stringValue());
		}
		
		indexReader.close();

		return resultList;

	}
}
