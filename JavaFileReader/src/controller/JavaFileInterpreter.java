package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileWriter;

public class JavaFileInterpreter {

	//private static String path = "C:\\Users\\Bruger\\git\\P2BevarUkraine\\P2.BevarUkraine\\src\\model\\ModelSupplier.java";
	//private static String path1 = "C:\\Users\\Bruger\\git\\P2BevarUkraine\\P2.BevarUkraine\\src\\dataaccess\\DAO.java";
	//private static String path2 = "C:\\Users\\Bruger\\git\\P2BevarUkraine\\P2.BevarUkraine\\src\\dataaccess\\DAOFactorySupplier.java";
	//private static String path3 = "C:\\Users\\Bruger\\git\\P2BevarUkraine\\P2.BevarUkraine\\src\\control\\standard\\BetterPickupRouteController.java";
	//private static String path4 = "C:\\Users\\Bruger\\git\\P2BevarUkraine\\P2.BevarUkraine\\src\\gui\\windows\\AddressSelectorWindow.java";
	private boolean firstMethod;
	private List<String> resultList;
	private File file;
	
/*
	public static void main(String[] args) {
		
		JavaFileInterpreter reader = new JavaFileInterpreter();
		//reader.readJavaFile(path3);
		reader.extractJavaFile(path3);
		
		for (String string : reader.getResultList()) {
			System.out.println(string);
		}
		reader.saveResultFile(System.getProperty("user.home") + "\\Java2UMLet\\");

	}
	*/
	

	public JavaFileInterpreter() {
		resultList = new ArrayList<>();
	}
	
	public List<String> getResultList(){
		return resultList;
	}
	
	public File getFoundFile() {
		return file;
	}
	

	public void readJavaFile(String path) {
        try {
            BufferedReader input = new BufferedReader(
                    new FileReader(path));
            String line;
            while ((line = input.readLine()) != null)
                System.out.println(line);
            input.close();
        } catch (IOException ex) {
            System.err.println("Error occured");
        }
    }
	
	
	public String saveResultFile(String startSavePath) {
		
		
		String savePath = startSavePath;
		
		String fileName = file.getName();
		fileName = fileName.replaceAll("\\..*", "") + ".txt";
		System.out.println(savePath+"\\"+fileName);
		
		try {
			Files.createDirectories(Paths.get(savePath));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		FileWriter writer;

		try {
			writer = new FileWriter(savePath+"\\"+fileName);

			for (String str : resultList) {
				writer.write(str + System.lineSeparator());
			}
			writer.close();
			
			return savePath;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Error saving file");
			e.printStackTrace();
		}
		return null;
	}

	public void extractJavaFile(String path) {
		file = new File(path);
		extractJavaFile(file);
	}

	public void extractJavaFile(File file) {
		
		this.file = file;
		
		firstMethod = true;
		try {
			
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line;
			Iterator<String> it = input.lines().iterator();
			
			while (it.hasNext()) {

				line = it.next();
				if(line.contains("@Override")) {
					line = it.next();
					line = it.next();
				}
				
				String filteredLine = filterLines(line);
				if (!filteredLine.isBlank()) {
					
					resultList.add(filteredLine);
				}
			}
			input.close();
		} catch (IOException ex) {
			System.err.println("Extraction Error occured");
			ex.printStackTrace();

		}
	}

	private String filterLines(String line) {
		line = line.trim();
		
		String out = "";
		
		String flag = "";
		
		List<String> method = new ArrayList<>();
		
		List<String> field = new ArrayList<>();

		// match method by public, private or protected
		// followed by anything with border ) {
		Pattern reMethod = Pattern.compile("(public|private|protected)(.*\\)).*(\\{|;)");
		Matcher matchMethod = reMethod.matcher(line);

		Pattern refield = Pattern.compile("^(public|private|protected)([^\\(\\)]*)(=|;)");
		Matcher matchField = refield.matcher(line);

		while (matchMethod.find()) {
			String matchedGroup = matchMethod.group();
			// System.out.println("Group: " + matchedGroup);
			method.add(matchedGroup);
		}

		while (matchField.find()) {
			String matchedGroup = matchField.group();
			// System.out.println("Group: " + matchedGroup);
			field.add(matchedGroup);
		}

		if (line.contains("class")) {
			flag = "class";
		}

		if (line.contains("interface")) {
			flag = "interface";
		}

		if (method.size() > 0) {
			flag = "method";
		}

		if (field.size() > 0) {
			flag = "field";
		}

		switch (flag) {

		case "class":
			out = formatClass(line);
			break;

		case "interface":
			out = formatInterface(line);
			break;

		case "field":
			//System.out.println("field");
			out = formatField(line);

			break;

		case "method":
			//System.out.println("method");
			out = formatMethod(line);
			break;

		default:
			out = "";
			break;
		}

		return out;
	}

	private String formatField(String line) {

		String formattedLine = "";

		boolean isStatic = line.contains("static");

		String visibility = getVisibility(line);

		String fieldName = getFieldName(line);

		String fieldType = getFieldType(line);

		formattedLine = visibility + fieldName + ": " + fieldType;

		if (isStatic) {
			formattedLine = "_" + formattedLine + "_";
		}

		return formattedLine;
		// return "";
	}

	private String getFieldName(String line) {

		line = line.replaceAll("(;|=).*", "");
		line = line.replaceAll("(public|private|protected|public|final)", "");
		line = line.trim();
		
		line = line.replaceAll("(.)* ", "");

		//System.out.println("FieldName: " + line);
		return line;
	}

	private String getFieldType(String line) {

		line = line.replaceAll("(;|=).*", "");
		line = line.replaceAll("(public|private|protected|static|final)", "");
		line = line.trim();
		line = line.replaceAll("(\\w)*$", "");
		line = line.trim();

		//System.out.println("Fieldtype: " + line);
		line = sanitizeType(line);
		return line;
	}

	private String formatClass(String line) {
		
		boolean isAbstract = line.contains("abstract");
			
		
		
		line = line.replaceAll("\\{", "");
		line = line.replaceAll("(?:public|private|protected|abstract)", "");
		line = line.replaceAll("(?:final)", "");
		line = line.replaceAll("(?:class)", "");
		line = line.trim();
		line = line.replaceAll("[^\\w*].*", "");
		line = line.trim();
		
		if(isAbstract) {
			line = "/" + line + "/";
		}
		
		line += "\n-";
		line = line.trim();
		return line;
	}

	private String formatInterface(String line) {
		line = line.replaceAll("\\{", "");
		line = line.replaceAll("(?:public|private|protected)", "");
		line = line.replaceAll("(?:interface)", "");
		line = "<<Interface>>\n" + line.trim();
		line += "\n-";
		return line;
	}

	private String formatMethod(String line) {

		String formattedLine = "";

		boolean isStatic = line.contains("static");

		String visibility = getVisibility(line);
		// System.out.println(visibility);

		String methodName = getMethodName(line);
		// System.out.println(methodName);

		String returnType = getReturnType(line);
		// System.out.println(returnType);
		
		boolean isConstructor = methodName.equals(returnType);

		List<List<String>> parametersTypeName = getParameterList(line);


		if(isConstructor) {
			formattedLine = visibility +"<<constructor>>(";
		}else {
			formattedLine = visibility + methodName + "(";
		}
		
		

		for (int i = 0; i < parametersTypeName.size(); i++) {

			List<String> parameter = parametersTypeName.get(i);

			if (parameter.size() > 1) {
				formattedLine += parameter.get(1) + ": " + parameter.get(0);
			}
			if (i + 1 < parametersTypeName.size()) {
				formattedLine += ", ";
			}
		}

		formattedLine += "): " + returnType;

		if (isStatic || isConstructor) {
			formattedLine = "_" + formattedLine + "_";
		}

		if (firstMethod) {
			firstMethod = false;
			// System.out.println("firstMethod");
			formattedLine = "-\n" + formattedLine;
			// System.out.println(formattedLine);
		}

		return formattedLine;
	}

	private String getMethodName(String line) {

		line = line.replaceAll("\\{", "");
		line = line.replaceAll("\\(.*\\)", "");
		line = line.replaceAll("(?:public|private|protected|public)*", "");
		line = line.replaceAll(";", "");
		line = line.trim();
		line = line.replaceAll("(^(.*) )", "");

		// System.out.println("Method name: " + line);

		return line;
	}

	private List<List<String>> getParameterList(String line) {

		// match method by public, private or protected
		// followed by anything with border ) {
		Pattern reMethod = Pattern.compile("\\(.*\\)");
		Matcher matchMethod = reMethod.matcher(line);

		while (matchMethod.find()) {
			String matchedGroup = matchMethod.group();
			// System.out.println("parameters: " + matchedGroup);
			line = matchedGroup;
		}
		line = line.replaceAll("(\\(|\\))", "");
		// System.out.println(line);

		List<String> parameters = new ArrayList<>();

		String[] splitParametersList = line.split("\\, (?![^<]*>)");
		// System.out.println(splitParametersList.length);
		parameters.addAll(Arrays.asList(splitParametersList));

		// parameters.forEach(s->System.out.println(s));

		List<List<String>> parametersTypeName = new ArrayList<>();

		for (int index = 0; index < parameters.size(); index++) {
			List<String> splitParameters = new ArrayList<>(Arrays.asList(parameters.get(index).split(" (?<!, )")));

			splitParameters.set(0, sanitizeType(splitParameters.get(0)));

			parametersTypeName.add(splitParameters);
		}

		// parametersTypeName.forEach(p->p.forEach(s->System.out.println(s)));
		// System.out.println(Arrays.deepToString(parametersTypeName.toArray()));

		return parametersTypeName;
	}

	private String getReturnType(String line) {

		/*
		 * Pattern reMethod =
		 * Pattern.compile("^(?:public|private|protected|public)* .*? "); Matcher
		 * matchMethod = reMethod.matcher(line);
		 * 
		 * while (matchMethod.find()) { String matchedGroup = matchMethod.group();
		 * System.out.println("return type: " + matchedGroup); line = matchedGroup; }
		 */

		line = line.replaceAll("\\{", "");
		line = line.replaceAll("\\(.*\\)", "");
		line = line.replaceAll("(?:public|private|protected|static)*", "");
		line = line.replaceAll(";", "");
		line = line.trim();
		line = line.replaceAll("( (.*))$", "");

		// System.out.println("Return Type: " + line);

		line = sanitizeType(line);

		return line;
	}

	private String getVisibility(String line) {
		String visibility = "";

		if (line.matches(".*(public).*")) {
			visibility = "+";
		}

		if (line.matches(".*(private).*")) {
			visibility = "-";
		}

		if (line.matches(".*(protected).*")) {
			visibility = "~";
		}

		return visibility;
	}

	private String sanitizeType(String type) {
		String sanitizedType = type;

		if (type.matches("^Predicate.*")) {

			String subType = type.replaceAll("^\\w*", "");
			subType = subType.substring(1, subType.length() - 1);
			subType = sanitizeType(subType);

			sanitizedType = "Function<" + subType + ", boolean>";

			return sanitizedType;
		}

		if (type.matches("^List.*")) {

			String subType = type.replaceAll("^\\w*", "");
			subType = subType.substring(1, subType.length() - 1);
			subType = sanitizeType(subType);

			sanitizedType = subType + "[]";

			return sanitizedType;
		}

		if (type.matches("^Supplier.*")) {

			String subType = type.replaceAll("^\\w*", "");
			subType = subType.substring(1, subType.length() - 1);
			subType = sanitizeType(subType);

			sanitizedType = "Function<void, " + subType + ">";

			return sanitizedType;
		}

		if (type.matches("^Integer")) {

			sanitizedType = "int";

			return sanitizedType;
		}
		
		if (type.matches("^Double")) {
			
			sanitizedType = "double";
			
			return sanitizedType;
		}

		return sanitizedType;
	}
}
