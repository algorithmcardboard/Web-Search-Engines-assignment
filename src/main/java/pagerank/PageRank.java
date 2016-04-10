package pagerank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import IndexerRetriver.FileAction;
import IndexerRetriver.FileWalker;

public class PageRank {

	@Parameter(names = { "-docs" }, required = true)
	private String docsLocation;

	@Parameter(names = { "-f" }, required = true)
	private float fParam = 0.75F;

	public Map<Path, Page> getPageList() {

		Path startingDir = Paths.get(this.docsLocation);
		final Map<Path, Page> pageList = new LinkedHashMap<>();

		FileWalker walker = new FileWalker("*.{html,htm}", new FileAction() {

			@Override
			public void doWithMatchingFiles(Path path) {
				if (Files.notExists(path) || !Files.isReadable(path)) {
					return;
				}

				try {
					pageList.put(path, new Page(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});

		try {
			Files.walkFileTree(startingDir, walker);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pageList;
	}

	private void rankPages() {
		Map<Path, Page> pages = getPageList();
		int totalPages = pages.size();
		double epsilon = 0.01 / totalPages;

		double sum = getSumOfBases(pages);

		calculateScores(pages, sum);

		Map<Page, Map<Page, Double>> weightMatrix = getWeightMatrix(pages);

		printWeightMatrix(weightMatrix);

		Map<Page, Map<Page, Double>> inboundWeightMatrix = convertToInboundWeightMatrix(weightMatrix);

		boolean changed = false;
		do {
			changed = false;
			
			for (Entry<Path, Page> entry : pages.entrySet()) {
				Page p = entry.getValue();
				double newScore = ((1-this.fParam)*p.getBase()) + (this.fParam * getInboundScore(p, inboundWeightMatrix));
				if(Math.abs(p.getScore() - newScore) > epsilon){
					changed = true;
				}
				p.setNewScore(newScore);
			}
			for (Entry<Path, Page> entry : pages.entrySet()) {
				entry.getValue().swapScores();
			}

		} while (changed);
		
		for (Entry<Path, Page> entry2 : pages.entrySet()) {
			Page p = entry2.getValue();
			System.out.println(p.getPageName() + " " +  p.getScore());
		}
	}

	private double getInboundScore(Page p, Map<Page, Map<Page, Double>> inboundWeightMatrix) {
		if(!inboundWeightMatrix.containsKey(p)){
			return 0.0d;
		}
		
		Map<Page, Double> inbounds = inboundWeightMatrix.get(p);
		double score = 0.0d;
		for (Entry<Page, Double> entry : inbounds.entrySet()) {
			score += entry.getKey().getScore() * entry.getValue();
		}
//		System.out.println("Returns score "+score);
		return score;
	}

	private Map<Page, Map<Page, Double>> convertToInboundWeightMatrix(Map<Page, Map<Page, Double>> weightMatrix) {
		Map<Page, Map<Page, Double>> inboundWeightMatrix = new HashMap<Page, Map<Page, Double>>();

		if (weightMatrix == null) {
			return inboundWeightMatrix;
		}

		for (Entry<Page, Map<Page, Double>> entry : weightMatrix.entrySet()) {
			Page source = entry.getKey();
			for (Entry<Page, Double> entry2 : entry.getValue().entrySet()) {
				Page destination = entry2.getKey();
				double score = entry2.getValue();

				if (!inboundWeightMatrix.containsKey(destination)) {
					inboundWeightMatrix.put(destination, new HashMap<Page, Double>());
				}
				inboundWeightMatrix.get(destination).put(source, score);
			}
		}
		return inboundWeightMatrix;
	}

	private void printWeightMatrix(Map<Page, Map<Page, Double>> weightMatrix) {
		System.out.println("\n\n");
		for (Entry<Page, Map<Page, Double>> entry : weightMatrix.entrySet()) {
			Page source = entry.getKey();
			for (Entry<Page, Double> entry2 : entry.getValue().entrySet()) {
				Page destination = entry2.getKey();
				double score = entry2.getValue();

				System.out.println(source.getPageName() + " -> " + destination.getPageName() + " " + score);
			}
		}
		System.out.println("\n\n");
	}

	private void calculateScores(Map<Path, Page> pages, double sum) {
		for (Entry<Path, Page> entry : pages.entrySet()) {
			Page page = entry.getValue();
			page.calculateScore(sum);

			System.out
					.println(String.format("%-20s%-6s%10s", page.getPageName(), page.getWordCount(), page.getScore()));
		}
	}

	private double getSumOfBases(Map<Path, Page> pages) {
		double sum = 0.0d;
		for (Entry<Path, Page> entry : pages.entrySet()) {
			sum += entry.getValue().getBase();
		}
		return sum;
	}

	private Map<Page, Map<Page, Double>> getWeightMatrix(Map<Path, Page> pages) {
		int totalPages = pages.size();
		Map<Page, Map<Page, Double>> weightMatrix = new LinkedHashMap<>();

		for (Entry<Path, Page> entry : pages.entrySet()) {
			Page page = entry.getValue();

			weightMatrix.put(page, new LinkedHashMap<Page, Double>());
			Map<Page, Double> innerMap = weightMatrix.get(page);

			if (page.hasOutboundLinks()) {
				Map<Path, Integer> outboundLinks = page.getOutboundLinks();

//				System.out.println(page.getPath() + " has outbound links " + outboundLinks.size());

				for (Entry<Path, Integer> entry2 : outboundLinks.entrySet()) {
					Path path = Paths.get(this.docsLocation + "/" + entry2.getKey().toString());
					int score = entry2.getValue();
					if (!pages.containsKey(path)) {
//						System.out.println(path + " not exist. continuing");
						continue;
					}
					Page p = pages.get(path);
					innerMap.put(p, score * 1.0);
				}

				double sum = 0.0d;
				for (Entry<Page, Double> entry3 : innerMap.entrySet()) {
					sum += entry3.getValue();
				}

				for (Entry<Page, Double> entry4 : innerMap.entrySet()) {
					entry4.setValue(entry4.getValue() / sum);
				}

			} else {
//				System.out.println(page.getPath() + "zero outbound");
				for (Entry<Path, Page> e1 : pages.entrySet()) {
					Page p = e1.getValue();
					innerMap.put(p, (1.0d / totalPages));
				}
			}
		}

		return weightMatrix;
	}

	public static void main(String[] args) {
		PageRank pr = new PageRank();
		new JCommander(pr, args);
		pr.rankPages();
	}
}