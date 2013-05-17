package br.ufpe.cin.groundhog.main;

import java.io.File;
import java.util.Date;
import java.util.List;

import br.ufpe.cin.groundhog.codehistory.UnsupportedForgeException;
import br.ufpe.cin.groundhog.parser.formater.Formater;
import br.ufpe.cin.groundhog.parser.formater.FormaterFactory;
import br.ufpe.cin.groundhog.util.Dates;

import com.google.common.base.Objects;

public final class JsonInput {
	private String forge;
	private String dest;
	private String out;
	private String datetime;
	private String nprojects;
	private String outputformat;
	private Search search;

	//TODO: this should be discovered dinamically
	public SupportedForge getForge() {
		if (forge.toLowerCase().equals("github")) {
			return SupportedForge.GITHUB;
		} else if (forge.equals("googlecode")) {
			return SupportedForge.GOOGLECODE;
		} else if (forge.equals("googlecode")) {
			return SupportedForge.SOURCEFORGE;			
		}
		throw new UnsupportedForgeException("Sorry, currently groundhog only supports Github, Sourceforge and GoogleCode. We do not support: " + forge);
	}

	public File getDest() {
		return new File(dest);
	}

	public File getOut() {
		return new File(out);
	}

	public Date getDatetime() {
		return new Dates("yyyy-MM-dd HH:mm").format(datetime);
	}

	public int getNprojects() {
		return Integer.parseInt(nprojects);
	}

	public Formater getOutputformat() {
		return FormaterFactory.get(outputformat.toLowerCase());
	}

	public static int getMaxThreads() {
		return 4;
	}
	
	public Search getSearch() {
		return search;
	}

	public String toString() {
		return Objects.toStringHelper(this)
				.add("forge", forge)
				.add("dest", dest)
				.add("out", out)
				.add("datetime", datetime)
				.add("nproject", nprojects)
				.add("outputformat", outputformat)
				.add("search", search)
				.toString();
	}
}

final class Search {
	private List<String> projects;
	private String username;

	public String getUsername() {
		return username;
	}

	public List<String> getProjects() {
		return projects;
	}

	public String toString() {
		return Objects.toStringHelper(this)
				.add("projects", projects)
				.add("username", username)
				.toString();
	}
}