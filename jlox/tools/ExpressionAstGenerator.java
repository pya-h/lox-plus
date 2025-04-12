package jlox.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExpressionAstGenerator {
    public static void main(String[] args) throws IOException, Exception {
        if (args.length != 2) {
            final String name = new Object() {
            }.getClass().getEnclosingClass().getSimpleName();
            System.out.println("[Usage] jlox.tools." + name + " " + "<output-directory> <base-class>");
            System.exit(64);
        }
        final String outDir = args[0], baseClass = args[1];
        final Map<String, List<String>> childs = new HashMap<>();
        childs.put("Binary", Arrays.asList(baseClass + " left", "Token operator", baseClass + " right"));
        childs.put("Unary", Arrays.asList(baseClass + " right"));
        childs.put("Literal", Arrays.asList("Object value"));
        childs.put("Grouping", Arrays.asList(baseClass + " expression"));
        generate(outDir, baseClass, childs);
    }

    public static void generate(final String outputDirectory, final String baseClass,
            final Map<String, List<String>> subClasses) throws IOException, Exception {
        PrintWriter writer = new PrintWriter(outputDirectory + "/" + baseClass + ".java");
        writer.println("package jlox;\n");
        writer.println("public abstract class " + baseClass + " {");
        final Set<String> childNames = subClasses.keySet();
        for (String className : childNames) {
            String tabs = "\t";
            writer.println(tabs + "public static class " + className + " extends " + baseClass + " {");

            final List<String> fields = subClasses.get(className);
            String subClassTabs = tabs + "\t", constructorBody = "",
                    constructorDecleration = subClassTabs + "public " + className + "(";
            int i = fields.size();
            for (String fieldData : fields) {
                constructorDecleration += fieldData;
                final String[] data = fieldData.split("\\s+");
                if(data.length < 2) {
                    writer.close();
                    throw new Exception("Insufficient field data at: " + fieldData);
                }
                constructorBody += "\n" + subClassTabs + "\tthis." + data[data.length - 1] + " = " + data[data.length - 1] + ";";
                writer.println(subClassTabs + (data[0] != "final" ? "final" : "") + " " + fieldData + ";");
                if (--i > 0) {
                    constructorDecleration += ", ";
                } else {
                    constructorDecleration += ")";
                    constructorBody += "\n";
                }
            }
            writer.println("\n" + constructorDecleration + " {" + constructorBody + subClassTabs + "}\n" + tabs + "}\n");
        }
        writer.println("}");
        writer.close();
    }
}
