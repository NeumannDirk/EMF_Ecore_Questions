package modelValidation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstanceConfromanceTest {

	static String modelInstance = "../LibraryWithReplicatedBooks/instances/My.librarywithreplicatedbooks";
	
	static String libraryWithBooksMetamodel = "../LibraryWithReplicatedBooks/model/libraryWithReplicatedBooks.ecore";
	static String libraryOnlyMetamodel = "../LibraryWithBooksAsLoadedResource/model/libraryWithBooksAsLoadedResource.ecore";
	static String bookOnlyMetamodel = "../Book/model/book.ecore";
	
	@BeforeEach
	public void CheckFileExistence() {
		EList<String> pathStrings = new BasicEList<String>();
		pathStrings.add(libraryWithBooksMetamodel);
		pathStrings.add(modelInstance);
		pathStrings.add(libraryOnlyMetamodel);
		pathStrings.add(bookOnlyMetamodel);
		
		for (String pathString : pathStrings) {
			Path path = Paths.get(pathString);
			assertTrue(Files.isRegularFile(path));
			
			URI uri = URI.createFileURI(pathString);
			assertTrue(uri.isFile());
		}
	}
	
	@Test
	public void testInstanceOfCombinedMetamodel() {
		EList<URI> metamodelUris = new BasicEList<URI>();
		metamodelUris.add(URI.createFileURI(libraryWithBooksMetamodel));	
		
		TestHelper.checkInstance(URI.createFileURI(modelInstance), metamodelUris);
	}
	
	@Test
	public void testInstanceOfSeparateMetamodels() {
		EList<URI> metamodelUris = new BasicEList<URI>();
		metamodelUris.add(URI.createFileURI(libraryOnlyMetamodel));
		metamodelUris.add(URI.createFileURI(bookOnlyMetamodel));
		
		TestHelper.checkInstance(URI.createFileURI(modelInstance), metamodelUris);
	}
}
