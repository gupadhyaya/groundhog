package br.ufpe.cin.groundhog.main;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class OptionTest {

	@Test
	public void testJsonInputFile() {
		Options op = new Options();
		op.setInputFile(new File(
				"/home/ghlp/workspace/java/groundhog/grounghog.json"));
		Assert.assertNotNull(op.getInputFile());
	}
}