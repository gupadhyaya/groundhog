package br.cin.ufpe.epona.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.cin.ufpe.epona.codehistory.CheckoutException;
import br.cin.ufpe.epona.codehistory.CodeHistory;
import br.cin.ufpe.epona.codehistory.GitCodeHistory;
import br.cin.ufpe.epona.codehistory.SFCodeHistory;
import br.cin.ufpe.epona.codehistory.SvnCodeHistory;
import br.cin.ufpe.epona.codehistory.UnsupportedSCMException;
import br.cin.ufpe.epona.config.ThreadsConfig;
import br.cin.ufpe.epona.crawler.CrawlGitHub;
import br.cin.ufpe.epona.crawler.CrawlGoogleCode;
import br.cin.ufpe.epona.crawler.CrawlSourceForge;
import br.cin.ufpe.epona.crawler.ForgeCrawler;
import br.cin.ufpe.epona.entity.ForgeProject;
import br.cin.ufpe.epona.entity.SCM;
import br.cin.ufpe.epona.http.Requests;
import br.cin.ufpe.epona.parser.JavaParser;
import br.cin.ufpe.epona.scmclient.EmptyProjectAtDateException;
import br.cin.ufpe.epona.scmclient.SVNClient;
import br.cin.ufpe.epona.search.ForgeSearch;
import br.cin.ufpe.epona.search.SearchException;
import br.cin.ufpe.epona.search.SearchGitHub;
import br.cin.ufpe.epona.search.SearchGoogleCode;
import br.cin.ufpe.epona.search.SearchSourceForge;

import com.google.common.base.Joiner;
import com.google.common.io.Files;

public class CmdMain {

	private static Logger logger = LoggerFactory.getLogger(CrawlGoogleCode.class);
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH_mm");
	
	private static String f(String s, Object... args) {
		return String.format(s, args);
	}
	
	public static ForgeSearch defineForgeSearch(SupportedForge f) {
		ForgeSearch search = null;
		switch (f) {
		case GitHub:
			search = SearchGitHub.getInstance();
			break;
		case SourceForge:
			search = SearchSourceForge.getInstance();
			break;
		case GoogleCode:
			search = SearchGoogleCode.getInstance();
			break;
		}
		return search;
	}
	
	public static ForgeCrawler defineForgeCrawler(SupportedForge f, File destinationFolder) {
		ForgeCrawler crawler = null;
		switch (f) {
		case GitHub:
			crawler = new CrawlGitHub(destinationFolder);
			break;
		case SourceForge:
			crawler = new CrawlSourceForge(destinationFolder); 
			break;
		case GoogleCode:
			crawler = new CrawlGoogleCode(destinationFolder);
			break;
		}
		return crawler;
	}
	
	public static CodeHistory defineCodeHistory(SCM scm) throws UnsupportedSCMException {
		CodeHistory codehistory = null;
		switch (scm) {
		case GIT:
			codehistory = GitCodeHistory.getInstance();
			break;
		case SOURCE_FORGE:
			codehistory = SFCodeHistory.getInstance(); 
			break;
		case SVN:
			codehistory = SvnCodeHistory.getInstance();
			break;
		default:
			throw new UnsupportedSCMException(scm);
		}
		return codehistory;
	}
	
	public static File downloadAndCheckoutProject(ForgeProject project, Date datetime, Future<File> repositoryFolderFuture)
			throws InterruptedException, ExecutionException, CheckoutException {
		// Wait for project download
		String name = project.getName();
		File repositoryFolder = repositoryFolderFuture.get();
		logger.info(f("Project %s was downloaded", name));
		
		// Checkout project to date
		String datetimeStr = dateFormat.format(datetime);
		logger.info(f("Checking out project %s to %s...", name, datetimeStr));
		CodeHistory codehistory = null;
		try {
			codehistory = defineCodeHistory(project.getSCM());
		} catch (UnsupportedSCMException e) {
			logger.warn(f("Project %s has an unsupported SCM: %s", name, e.getSCM()));
			return null;
		}
		File checkedOutRepository = null;
		try {
			if (project.getSCM() != SCM.SVN) {
				checkedOutRepository = codehistory.checkoutToDate(project.getName(), repositoryFolder, datetime);
			} else {
				checkedOutRepository = codehistory.checkoutToDate(project.getName(), project.getScmURL(), datetime);
			}
		} catch (EmptyProjectAtDateException e) {
			logger.warn(f("Project %s was empty at specified date: %s", name, datetimeStr));
			return null;
		}
		logger.info(f("Project %s successfully checked out to %s", name, datetimeStr));	
		
		return checkedOutRepository;
	}
	
	public static void analyzeProject(ForgeProject project, File projectFolder, Date datetime, File metricsFolder)
			throws IOException, JSONException {
		String name = project.getName();
		String datetimeStr = dateFormat.format(datetime);
		
		// Parse project
		logger.info(f("Parsing project %s...", name));
		JavaParser parser = new JavaParser(projectFolder);
		JSONObject metrics = null;
		metrics = parser.parseToJSON();
		
		if (metrics != null) {
			// Save metrics to file
			String metricsFilename = f("%s-%s.json", name, datetimeStr);
			logger.info(f("Project %s parsed, metrics extracted! Writing result to file %s...", name, metricsFilename));
			File metricsFile = new File(metricsFolder, metricsFilename);
			FileUtils.writeStringToFile(metricsFile, metrics.toString());
			logger.info(f("Metrics of project %s written to file %s", name, metricsFile.getAbsolutePath()));
		} else {
			logger.warn(f("Project %s has no Java source files! Metrics couldn't be extracted...", name));
		}
	}
	
	public static void freeResources(ForgeCrawler crawler, OutputStream errorStream) {
		crawler.shutdown();
		SVNClient.getInstance().close();
		Requests.getInstance().close();
		try {
			if (errorStream != null) {
				errorStream.close();
			}
		} catch (IOException e) {
			logger.trace("Unable to close error.log stream", e);
		}
	}
	
	public static void main(String[] args) {
		Options opt = new Options();
		/*CmdLineParser cmd = new CmdLineParser(opt);
		try {
			cmd.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmd.printUsage(System.err);
			return;
		}*/
		opt.setDatetime("2012-01-01_12_00");
		//opt.setDestinationFolder(new File("download"));
		opt.setForge(SupportedForge.GoogleCode);
		opt.setMetricsFolder(new File("metrics"));
		opt.setArguments(Arrays.asList("facebook"));
		
		List<String> terms = opt.getArguments();
		String term = Joiner.on(" ").join(terms);
		File destinationFolder = opt.getDestinationFolder();
		boolean isDestinationTemp = false;
		if (destinationFolder == null) {
			destinationFolder = Files.createTempDir();
			isDestinationTemp = true;
		}
		File metricsFolder = opt.getMetricsFolder();
		if (!metricsFolder.exists()) {
			metricsFolder.mkdirs();
		}
		String datetimeStr = opt.getDatetime();
		Date datetime = null;
		try {
			datetime = dateFormat.parse(datetimeStr);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		int nProjects = opt.getnProjects();
		
		// Redirect System.err to file
		PrintStream errorStream = null;
		try {
			errorStream = new PrintStream("error.log");
			System.setErr(errorStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Set ThreadsConfig.nThreads
		ThreadsConfig.nThreads = opt.getnThreads();
		
		// Search for projects
		logger.info("Searching for projects...");
		ForgeSearch search = defineForgeSearch(opt.getForge());
		ForgeCrawler crawler = defineForgeCrawler(opt.getForge(), destinationFolder);
		List<ForgeProject> allProjects = null;
		List<ForgeProject> projects = new ArrayList<ForgeProject>();
		try {
			allProjects = search.getProjects(term, 1);
		} catch (SearchException e) {
			e.printStackTrace();
			return;
		}
		for (int i = 0; i < nProjects; i++) {
			projects.add(allProjects.get(i));
		}
		
		// Download and analyze projects
		logger.info("Downloading and processing projects...");
		ExecutorService ex = Executors.newFixedThreadPool(ThreadsConfig.nThreads);
		List<Future<File>> downloadFutures = crawler.downloadProjects(projects);
		List<Future<?>> analysisFutures = new ArrayList<Future<?>>();
		for (int i = 0; i < downloadFutures.size(); i++) {
			final ForgeProject project = projects.get(i);
			final Date datetime_ = datetime;
			final Future<File> repositoryFolderFuture = downloadFutures.get(i);
			final File metricsFolder_ = metricsFolder;
			
			analysisFutures.add(ex.submit(new Runnable() {
				@Override
				public void run() { 
					File checkedOutRepository = null;
					try {
						checkedOutRepository = downloadAndCheckoutProject(project, datetime_, repositoryFolderFuture);
					} catch (Exception e) {
						logger.trace(f("Error while downloading project %s", project.getName()), e);
					}
					if (checkedOutRepository != null) {
						try {
							analyzeProject(project, checkedOutRepository, datetime_, metricsFolder_);
						} catch (Exception e) {
							logger.trace(f("Error while analyzing project %s", project.getName()), e);
						}
					} else {
						logger.warn(f("Project %s can't be analyzed", project.getName()));
					}
				}
			}));
		}
		ex.shutdown();
		for (int i = 0; i < analysisFutures.size(); i++) {
			try {
				analysisFutures.get(i).get();
			} catch (InterruptedException e) {
				logger.trace(f("Error while analyzing project %s", projects.get(i).getName()), e);
			} catch (ExecutionException e) {
				logger.trace(f("Error while analyzing project %s", projects.get(i).getName()), e);
			}
		}
		
		// Free resources and delete temp directory (if exists)
		logger.info("Disposing resources...");
		freeResources(crawler, errorStream);
		if (isDestinationTemp) {
			try {
				FileUtils.deleteDirectory(destinationFolder);
			} catch (IOException e) {
				logger.warn("Could not delete temp folders (but they will be eventually deleted)");
			}
		}
		logger.info("Done!");
	}

}