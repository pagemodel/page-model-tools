package org.pagemodel.core.utils.json;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StacktraceFilter {
	private PackageHighlight packageRoot = new PackageHighlight();
	public static final StacktraceFilter highlights = new StacktraceFilter();
	public static final StacktraceFilter systemIgnoreHighlights = new StacktraceFilter();

	static {
		ignoreSystemPackages();
	}

	private static void ignoreSystemPackages(){
		highlights.addPackageHighlight("org.pagemodel").setHighlight("pmt").setSticky(true);

		highlights.addPackageHighlight("com.sun").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("java").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("sun").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("org.junit").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("org.gradle").setHighlight("system").setStop(true);

		highlights.addPackageHighlight("org.openqa").setHighlight("system").setStop(true);

		highlights.addPackageHighlight("javax").setHighlight("system").setStop(true);

		highlights.addPackageHighlight("net.schmizz.sshj").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("net.sf.expectit").setHighlight("system").setStop(true);

		highlights.addPackageHighlight("io.github.bonigarcia").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("org.seleniumhq").setHighlight("system").setStop(true);
		highlights.addPackageHighlight("com.deque.axe").setHighlight("system").setStop(true);
	}

	public PackageHighlight addPackageHighlight(String packageName){
		String[] parts = packageName.split("\\.");
		PackageHighlight cur = packageRoot;
		for(String p : parts){
			PackageHighlight n = cur.childPackages.get(p);
			if(n == null){
				n = new PackageHighlight(cur, p);
				cur.childPackages.put(p, n);
			}
			cur = n;
		}
		return cur;
	}

	public ClassHighlight addClassHighlight(Class<?> cls){
		return addClassHighlight(cls.getPackage().getName(), cls.getSimpleName());
	}

	public ClassHighlight addClassHighlight(String packageName, String className){
		PackageHighlight parent = addPackageHighlight(packageName);
		String[] parts = className.split("\\$");
		ClassHighlight cur = parent.classRoot;
		for(String p : parts){
			ClassHighlight n = cur.childClasses.get(p);
			if(n == null){
				n = new ClassHighlight(cur, p);
				cur.childClasses.put(p, n);
			}
			cur = n;
		}
		return cur;
	}

	public MethodHighlight addMethodHighlight(Method mthd){
		return addMethodHighlight(mthd.getDeclaringClass().getPackage().getName(), mthd.getDeclaringClass().getSimpleName(), mthd.getName());
	}

	public MethodHighlight addMethodHighlight(String packageName, String className, String methodName){
		ClassHighlight parentClass = addClassHighlight(packageName, className);
		MethodHighlight methodHighlight = parentClass.childMethods.get(methodName);
		if(methodHighlight == null){
			methodHighlight = new MethodHighlight(parentClass, methodName);
			parentClass.childMethods.put(methodName, methodHighlight);
		}
		return methodHighlight;
	}

	public MethodMatch matchPackage(String packageName){
		String[] parts = packageName.split("\\.");
		MethodMatch matchResult = new MethodMatch();
		PackageHighlight cur = packageRoot;
		for(String p : parts){
			if(cur.stop){
				matchResult.packageMatch = true;
				return matchResult;
			}
			PackageHighlight next = cur.childPackages.get(p);
			if(next == null){
				matchResult.packageMatchDiff = parts.length - matchResult.packageMatchDepth;
				return matchResult;
			}
			matchResult.packageMatchDepth++;
			cur = next;
			matchResult.setPackageHighlight(cur);
		}
		matchResult.packageMatch = true;
		return matchResult;
	}

	public MethodMatch matchClass(Class<?> cls){
		return matchClass(cls.getPackage().getName(), cls.getSimpleName());
	}

	public MethodMatch matchClass(String packageName, String className){
		MethodMatch matchResult = matchPackage(packageName);
		if(!matchResult.packageMatch){
			return matchResult;
		}
		String[] parts = className.split("\\$");
		ClassHighlight cur = matchResult.packageHighlight.classRoot;
		for(String p : parts){
			ClassHighlight next = cur.childClasses.get(p);
			if(next == null){
				matchResult.classMatchDiff = parts.length - matchResult.classMatchDepth;
				return matchResult;
			}
			matchResult.classMatchDepth++;
			cur = next;
			matchResult.setClassHighlight(cur);
			if(cur.stop){
				matchResult.classMatch = true;
				return matchResult;
			}
		}
		matchResult.classMatch = true;
		return matchResult;
	}

	public MethodMatch matchMethod(Method mthd){
		return matchMethod(mthd.getDeclaringClass().getPackage().getName(), mthd.getDeclaringClass().getSimpleName(), mthd.getName());
	}

	public MethodMatch matchMethod(String fqcn, String methodName){
		int i = fqcn.lastIndexOf('.');
		return matchMethod(fqcn.substring(0, i), fqcn.substring(i+1), methodName);
	}

	public MethodMatch matchMethod(String packageName, String className, String methodName){
		MethodMatch matchResult = matchClass(packageName, className);
		if(!matchResult.classMatch){
			return matchResult;
		}
		MethodHighlight methodHighlight = matchResult.classHighlight.childMethods.get(methodName);
		if(methodHighlight == null){
			return matchResult;
		}
		matchResult.methodMatch = true;
		matchResult.setMethodHighlight(methodHighlight);
		return matchResult;
	}

	public static class PackageHighlight extends HighlightValue {
		String partName;
		String packageName;
		PackageHighlight parentPackage;
		Map<String,PackageHighlight> childPackages = new HashMap<>();
		ClassHighlight classRoot = new ClassHighlight(this);

		private PackageHighlight(){
			this.partName = "";
			this.packageName = "";
			this.parentPackage = null;
		}
		private PackageHighlight(PackageHighlight parentPackage, String partName){
			this.parentPackage = parentPackage;
			this.partName = partName;
			this.packageName = parentPackage.packageName.isEmpty() ? partName : parentPackage.packageName + "." + partName;
		}
	}

	public static class ClassHighlight extends HighlightValue {
		// Name of the class
		String simpleName;
		// For inner classes includes parent
		String className;
		String fullyQualifiedClassName;
		ClassHighlight parentClass;
		PackageHighlight parentPackage;
		Map<String,ClassHighlight> childClasses = new HashMap<>();
		Map<String,MethodHighlight> childMethods = new HashMap<>();

		private ClassHighlight(PackageHighlight parentPackage){
			this.parentPackage = parentPackage;
			this.parentClass = this;
			this.simpleName = "";
			this.className = "";
			String fullyQualifiedClassName;
		}
		private ClassHighlight(ClassHighlight parentClass, String simpleName){
			this.parentClass = parentClass;
			this.parentPackage = parentClass.parentPackage;
			this.simpleName = simpleName;
			this.className = parentClass.className.isEmpty() ? simpleName : parentClass.className + "$" + simpleName;
			this.fullyQualifiedClassName = parentPackage.packageName + "." + className;
		}
	}

	public static class MethodHighlight extends HighlightValue {
		String methodName;
		ClassHighlight parentClass;
		PackageHighlight parentPackage;

		private MethodHighlight(ClassHighlight parentClass, String methodName){
			this.methodName = methodName;
			this.parentClass = parentClass;
			this.parentPackage = parentClass.parentPackage;
		}
	}

	public static class HighlightValue {
		boolean stop = false;
		boolean sticky = false;
		String highlight = "";

		public boolean isStop() {
			return stop;
		}

		public HighlightValue setStop(boolean stop) {
			this.stop = stop;
			return this;
		}

		public boolean isSticky() {
			return sticky;
		}

		public HighlightValue setSticky(boolean sticky) {
			this.sticky = sticky;
			return this;
		}

		public String getHighlight() {
			return highlight;
		}

		public HighlightValue setHighlight(String highlight) {
			this.highlight = highlight;
			return this;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof HighlightValue))
				return false;
			HighlightValue highlight1 = (HighlightValue) o;
			return stop == highlight1.stop && sticky == highlight1.sticky && highlight.equals(highlight1.highlight);
		}

		@Override
		public int hashCode() {
			return Objects.hash(stop, sticky, highlight);
		}
	}

	public static class MethodMatch {
		public boolean methodMatch = false;
		public MethodHighlight methodHighlight = null;

		public int classMatchDepth = 0;
		public int classMatchDiff = -1;
		public boolean classMatch = false;
		public ClassHighlight classHighlight = null;

		public int packageMatchDepth = 0;
		public int packageMatchDiff = -1;
		public boolean packageMatch = false;
		public PackageHighlight packageHighlight = null;

		void setMethodHighlight(MethodHighlight methodHighlight) {
			this.methodHighlight = methodHighlight;
		}

		void setClassHighlight(ClassHighlight classHighlight) {
			this.classHighlight = classHighlight;
		}

		void setPackageHighlight(PackageHighlight packageHighlight) {
			this.packageHighlight = packageHighlight;
		}
	}
}
