package net.twisterrob.gradle.graph.graphstream

import org.graphstream.graph.*

// TODO consider @groovy.lang.Category
class GraphExtensions {
	static void addClass(final Iterable<? extends Element> self, String clazz) {
		for (Element e : self) {
			addClass(e, clazz)
		}
	}
	static void removeClass(final Iterable<? extends Element> self, String clazz) {
		for (Element e : self) {
			removeClass(e, clazz)
		}
	}

	static String[] addClass(final Node self, String clazz) {
		return addClass((Element)self, clazz)
	}
	static String[] addClass(final Element self, String clazz) {
		ArrayList<String> classes = getClasses(self)
		classes.add(clazz)
		self.setAttribute("ui.class", classes.join(","))
		return classes;
	}
	static void setLabel(final Element self, String label) {
		self.setAttribute("ui.label", label)
	}
	static String getLabel(final Element self) {
		return self.getAttribute("ui.label")
	}
	static String[] removeClass(final Node self, String clazz) {
		return removeClass((Element)self, clazz)
	}
	static String[] removeClass(final Element self, String clazz) {
		ArrayList<String> classes = getClasses(self)
		classes.remove(clazz)
		if (classes.empty) {
			self.removeAttribute("ui.class")
		} else {
			self.setAttribute("ui.class", classes.join(","))
		}
		return classes;
	}
	static ArrayList<String> getClasses(final Element self) {
		String classesString = self.getAttribute("ui.class")
		if (classesString == null || classesString.empty) {
			return new ArrayList()
		}
		new ArrayList(Arrays.asList(classesString.split(",")))
	}
}
