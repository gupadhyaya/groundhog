package br.ufpe.cin.groundhog.search;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufpe.cin.groundhog.Commit;
import br.ufpe.cin.groundhog.Contributor;
import br.ufpe.cin.groundhog.Language;
import br.ufpe.cin.groundhog.Project;
import br.ufpe.cin.groundhog.Release;
import br.ufpe.cin.groundhog.SCM;
import br.ufpe.cin.groundhog.User;
import br.ufpe.cin.groundhog.http.HttpModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SearchGitHubTest {
	
	private SearchGitHub searchGitHub;
	private Project fakeProject;

	@Before
	public void setup() {
		Injector injector = Guice.createInjector(new SearchModule(), new HttpModule());
		searchGitHub = injector.getInstance(SearchGitHub.class);
		User user = new User("elixir-lang");
		fakeProject = new Project("elixir", "", SCM.GIT, "git@github.com:elixir-lang/elixir.git");
		fakeProject.setUser(user);
	}
	
	@Test
	public void testSearchByProjectName() {
		try {			
			List<Project> projects = searchGitHub.getProjects("groundhog", 1, SearchGitHub.INFINITY);
			searchGitHub.getProjectLanguages(projects.get(0));
			Assert.assertNotNull(projects);
			
			/*Search a inexisting project, we expect no project*/
			projects = searchGitHub.getProjects("12k4o12samsarmorm1om1o2m21m921", 1, SearchGitHub.INFINITY);
			Assert.assertEquals(0, projects.size());
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testFetchByProjectLanguages() {
		try {
			List<Language> langs = searchGitHub.getProjectLanguages(fakeProject);
			Assert.assertNotNull(langs);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjects() {
		try {
			List<Project> projects = searchGitHub.getAllProjects(0, 5);
			Assert.assertNotNull(projects);
			
			for (Project project : projects){
				Assert.assertNotNull(project.getName());
				Assert.assertNotNull(project.getDescription());
				Assert.assertNotNull(project.getLanguage());
				Assert.assertNotNull(project.getLanguages());
				Assert.assertNotNull(project.getIssues());
				Assert.assertNotNull(project.getMilestones());
				Assert.assertNotNull(project.getCommits());
				Assert.assertNotNull(project.getContributors());
				Assert.assertNotNull(project.getUser());
				Assert.assertNotNull(project.getSCM());
				Assert.assertNotNull(project.getScmURL());
				Assert.assertNotNull(project.getSourceCodeURL());
				Assert.assertNotNull(project.getCreatedAt());
				Assert.assertNotNull(project.getLastPushedAt());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetProjectsByLanguage() {
		try {
			List<Project> projects = searchGitHub.getAllProjectsByLanguage("java");
			Assert.assertNotNull(projects);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjectCommits() {
		try {
			Project project = new Project("github","android");
			List<Commit> commits = searchGitHub.getAllProjectCommits(project);
			Assert.assertNotNull(commits);	
			
			/*Assert that no object attribute is null*/
			for(Commit commit : commits){
				Assert.assertNotNull(commit.getSha());
				Assert.assertNotNull(commit.getCommiter());
				Assert.assertNotNull(commit.getMessage());
				Assert.assertNotNull(commit.getProject());
				Assert.assertNotNull(commit.getCommitDate());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * This class contains a group of tests about {@link SearchGitHub#getAllProjectCommits(Project) getAllProjectCommits} method
	 * */
	@Test
	public void testGetAllProjectContributors() {
		try {
			
			/*Sanity test, if the list of contributors is null, something is wrong*/
			Project project = new Project("twitter", "ambrose");
			List<Contributor> contributors = searchGitHub.getAllProjectContributors(project);
			Assert.assertNotNull(contributors);
			
			/*Assert that no object attribute is null*/
			for(Contributor contributor : contributors){
				Assert.assertNotNull(contributor.getGravatar_id());
				Assert.assertNotNull(contributor.getHtml_url());
				Assert.assertNotNull(contributor.getFollowers_url());
				Assert.assertNotNull(contributor.getFollowing_url());
				Assert.assertNotNull(contributor.getGists_url());
				Assert.assertNotNull(contributor.getStarred_url());
				Assert.assertNotNull(contributor.getSubscriptions_url());
				Assert.assertNotNull(contributor.getAvatar_url());
				Assert.assertNotNull(contributor.getRepos_url());
				Assert.assertNotNull(contributor.getEvents_url());
				Assert.assertNotNull(contributor.getReceived_events_url());
				Assert.assertNotNull(contributor.getType());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testGetAllProjectReleases() {
		try {
			User u = new User("twbs");
			Project project = new Project(u, "bootstrap");
			
			List<Release> releases = searchGitHub.getAllProjectReleases(project);
						
			Assert.assertNotNull(releases);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
