package modelValidation;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class TestHelper {

	// Hilfsmethode zur Ausgabe des Diagnostik-Baums
	public static void printDiagnostic(Diagnostic diagnostic, String indent) {
		System.out.println(indent + diagnostic.getMessage());
		for (Diagnostic child : diagnostic.getChildren()) {
			printDiagnostic(child, indent + "  ");
		}
	}

	/**
	 * Check that there is only one root content and that is is an EPackage.
	 * 
	 * @param metamodelResource
	 * @return
	 */
	private static EPackage checkResourceRootContent(Resource metamodelResource) {
		EList<EObject> metamodelContents = metamodelResource.getContents();
		if (metamodelContents.isEmpty()) {
			throw new IllegalArgumentException(
					"No content in metamodel resource: " + metamodelResource.getURI().toFileString());
		} else if (metamodelContents.size() > 1) {
			System.out.println("Warning: More than one root object in metamodel resource: "
					+ metamodelResource.getURI().toFileString());
		} else if (!(metamodelContents.get(0) instanceof EPackage)) {
			throw new IllegalArgumentException("Root object in metamodel resource is not an EPackage: "
					+ metamodelResource.getURI().toFileString());
		}
		return (EPackage) metamodelContents.get(0);
	}

	public static boolean checkInstance(URI modelInstanceURI, EList<URI> metamodelURIs) {

		// Setup resource set and register the resource factories
		ResourceSet resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore",
				new EcoreResourceFactoryImpl());

		String extension = modelInstanceURI.fileExtension();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(extension,
				new XMIResourceFactoryImpl());

		EPackageRegistryImpl ePackageRegistry = new EPackageRegistryImpl();

		// Load metamodels and register their packages recursively
		for (URI metamodelURI : metamodelURIs) {
			Resource metamodelResource = resourceSet.getResource(metamodelURI, true);
			EPackage rootPackage = checkResourceRootContent(metamodelResource);
			ePackageRegistry.put(rootPackage.getNsURI(), rootPackage);
			recursivelyAddSubpackagesToRegistry(rootPackage, ePackageRegistry);
		}

		// Also register Ecore package to handle Ecore types in model
		resourceSet.setPackageRegistry(ePackageRegistry);
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		// print all registered packages
		System.out.println("Registered packages:");
		for (Object key : resourceSet.getPackageRegistry().keySet()) {
			System.out.println(" - " + key);
		}

		Resource modelResource = resourceSet.getResource(modelInstanceURI, true);
		TreeIterator<EObject> allContents = modelResource.getAllContents();
		StringBuilder sb = new StringBuilder();
		sb.append("Model instance check result:\n");

		boolean noDynamicInstances = true;
		Set<EClass> usedTypes = new HashSet<>();

		// ---- First loop: gather all used types and check for dynamic objects ----
		while (allContents.hasNext()) {
			EObject eObject = allContents.next();
			if (eObject.getClass() == DynamicEObjectImpl.class) {
				noDynamicInstances = false;
				sb.append(" - Found dynamic instance: ").append(eObject).append("\n");
			}
			usedTypes.add(eObject.eClass());
		}

		// ---- Second loop: verify that all used types are known in registered
		// metamodels ----
		boolean allTypesResolved = true;
		for (EClass eClass : usedTypes) {
			boolean found = false;
			for (Object value : resourceSet.getPackageRegistry().values()) {
				if (value instanceof EPackage) {
					EPackage ePackage = (EPackage) value;
					if (ePackage.getEClassifiers().contains(eClass)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				allTypesResolved = false;
				sb.append(" - Unresolved type: ").append(eClass.getName()).append(" (package: ")
						.append(eClass.getEPackage().getName()).append(")\n");
			}
		}

		sb.append("Summary:\n");
		sb.append(" - No dynamic instances: ").append(noDynamicInstances).append("\n");
		sb.append(" - All types resolved: ").append(allTypesResolved).append("\n");

		System.out.println(sb.toString());

		return noDynamicInstances && allTypesResolved;
	}

	private static void recursivelyAddSubpackagesToRegistry(EPackage targetPackage,
			EPackageRegistryImpl ePackageRegistry) {
		for (EPackage subPackage : targetPackage.getESubpackages()) {
			ePackageRegistry.put(subPackage.getNsURI(), subPackage);
			recursivelyAddSubpackagesToRegistry(subPackage, ePackageRegistry);
		}
	}

}
