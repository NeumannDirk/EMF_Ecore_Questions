package main;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

public class MetamodelCreator {
	public static void main(String[] args) {

		// Creating the ResourceSet with factories and the Resource of the new metamodel

		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		EPackageRegistryImpl ePackageRegistry = new EPackageRegistryImpl();

		URI bookMetamodelURI = URI.createFileURI("../Book/model/book.ecore");
		
		Resource metamodelResource = resourceSet.getResource(bookMetamodelURI, true);
		EPackage bookPackage = (EPackage) metamodelResource.getContents().get(0);
		ePackageRegistry.put(bookPackage.getNsURI(), bookPackage);
		
		System.out.println(bookPackage.getNsURI());
		
		ePackageRegistry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		URI targetUri = URI.createFileURI("model/gen_libraryWithBooksAsLoadedResource.ecore");
		
		EPackage libraryPackage = EcoreFactory.eINSTANCE.createEPackage();
		libraryPackage.setName("libraryWithBooksAsLoadedResource");
		libraryPackage.setNsPrefix("libraryWithBooksAsLoadedResource");
		libraryPackage.setNsURI("http://www.kit.tva.metamodels/library");
		ePackageRegistry.put(libraryPackage.getNsURI(), libraryPackage);
		
		//Filling the new Metamodel
		fillThePackage(bookPackage, libraryPackage);		
		
		Resource targetResource = resourceSet.createResource(targetUri);
		targetResource.getContents().add(libraryPackage);

		try {
			targetResource.save(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("MetamodelCreator executed");
	}

	private static void fillThePackage(EPackage bookPackage, EPackage libraryPackage) {
		EClass libClass = EcoreFactory.eINSTANCE.createEClass();
		libClass.setName("Library");
		
		EAttribute nameAtt = EcoreFactory.eINSTANCE.createEAttribute();
		nameAtt.setName("name");
		nameAtt.setLowerBound(0);
		nameAtt.setUpperBound(1);
		nameAtt.setEType(EcorePackage.eINSTANCE.getEString());
		
		libClass.getEStructuralFeatures().add(nameAtt);
		
		EReference bookRef = EcoreFactory.eINSTANCE.createEReference();
		EClass bookClass = (EClass) bookPackage.getEClassifier("Book");
		bookRef.setName("books");
		bookRef.setContainment(true);
		bookRef.setLowerBound(0);
		bookRef.setUpperBound(-1);
		bookRef.setEType(bookClass);
		
		libClass.getEStructuralFeatures().add(bookRef);
		
		libraryPackage.getEClassifiers().add(libClass);
	}
}
