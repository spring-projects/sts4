import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ui.ProgressApi;
import ui.ProgressBar;


/**
 * SelfExtractor
 */
public class SelfExtractor {

	private static final String CONTENTS_ZIP_RSRC = "/contents.zip";
	private static final int BUF_SIZE = 1024*8;

	byte[] buffer = new byte[BUF_SIZE];

	public static void main(String[] args) throws Exception {
		try {
			new SelfExtractor().run(args);
			System.exit(0);
		} catch (Throwable e) {
			e.printStackTrace();
			String message = e.getMessage();
			if (message==null) {
				message = e.getClass().getName();
			}
			JOptionPane.showMessageDialog(new JFrame(), message, "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	private void run(String[] args) throws Exception {
		if (args.length==0) {
			unpack();
		} else if (args.length==1) {
			repack(args[0]);
		} else {
			throw new IllegalArgumentException("Expecting one argument pointing to zipfile to re-wrap");
		}
	}

	private void repack(String _contentsZip) throws Exception {
		File contentsZip = new File(_contentsZip);
		System.out.println("Repackaging "+contentsZip);
		if (!contentsZip.isFile()) {
			throw new IllegalArgumentException("File not found:" + contentsZip);
		}
		String repackagedPath = contentsZip.toString();
		if (repackagedPath.endsWith(".zip")) {
			repackagedPath = repackagedPath.substring(0, repackagedPath.length()-4);
		}
		repackagedPath = repackagedPath + ".self-extracting.jar";
		System.out.println("         as "+repackagedPath);

		File wrapperJar = getMyJar();
		System.out.println("wrapperJar = "+wrapperJar);
		try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(wrapperJar))) {
			try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(new File(repackagedPath)))) {
				ZipEntry entry;
				while ((entry = zipIn.getNextEntry())!=null) {
					//System.out.println("Adding: "+entry.getName());
					if (entry.getName().equals("contents.zip")) {
						//skip. We need to write the wrapped zip contents here.
					} else {
						zipOut.putNextEntry(new ZipEntry(entry));
						if (entry.getName().endsWith("/")) {
							//skip directory has no bytes as contents
						} else {
							copy(zipIn, zipOut);
						}
					}
				} //end while
				entry = new ZipEntry("contents.zip");
				zipOut.putNextEntry(entry);
				//System.out.println("Adding: "+entry.getName());
				try (FileInputStream contentsZipIn = new FileInputStream(contentsZip)) {
					copy(contentsZipIn, zipOut);
				}
			} //end: zipOut
		}//end: zipIn
	}

	private void copy(InputStream in, OutputStream out) throws IOException {
		int bytesRead;
		while ((bytesRead = in.read(buffer)) > 0) {
			out.write(buffer, 0, bytesRead);
		}
	}

	private File getMyJar() {
		Class<?> myklass = this.getClass();
		String resourceName = this.getClass().getName() + ".class";
		URL resource = myklass.getResource(resourceName);
		String myResourceUrl = resource.toString();
		//Example: jar:file:/home/.../self-extracing-jar-creator-0.0.1-SNAPSHOT.jar!/SelfExtractor.class 
		if (!myResourceUrl.startsWith("jar:file:")) {
			//For convenience in 'development mode' look for a local jar built by maven.
			File devJar = new File("target/self-extracting-jar-creator-0.0.1-SNAPSHOT.jar");
			if (devJar.isFile()) {
				return devJar;
			}
			throw new IllegalStateException("To create self extracting jar, you must run this code from a jar");
		}
		return new File(myResourceUrl.substring("jar:file:".length(), myResourceUrl.indexOf('!')));
	}

	private ZipInputStream openContentsZip() {
		InputStream zip = this.getClass().getResourceAsStream(CONTENTS_ZIP_RSRC);
		if (zip==null) {
			throw new IllegalArgumentException("Malformed archive: Couldn't find embedded '"+CONTENTS_ZIP_RSRC+"");
		}
		return new ZipInputStream(zip);
	}

	public void unpack() throws Exception {
		File destDir = new File(System.getProperty("user.dir"));
		int numEntries = getNumEntries();
		int processed = 0;
		ProgressApi progress = ProgressBar.create(numEntries);
		try (ZipInputStream zip = openContentsZip()) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry())!=null) {
				String name = entry.getName();
				System.out.println(name);
				File outfile = new File(destDir, name);
				if (outfile.exists()) {
					throw new IllegalArgumentException("File already exists: "+outfile);
				}
				if (name.endsWith("/")) {
					outfile.mkdirs();
				} else {
					//file
					outfile.getParentFile().mkdirs();
					try (FileOutputStream out = new FileOutputStream(outfile)) {
						copy(zip, out);
					}
				}
				processed++;
				System.out.println("Progress = "+processed +"/"+numEntries);
				progress.worked(1);
			}
		} finally {
			progress.done();
		}
	}

	private int getNumEntries() throws IOException {
		int count = 0;
		try (ZipInputStream zip = openContentsZip()) {
			while (zip.getNextEntry()!=null) {
				count++;
			}
		}
		return count;
	}
}