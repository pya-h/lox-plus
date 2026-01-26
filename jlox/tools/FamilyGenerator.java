package jlox.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FamilyGenerator {
    public static void prepareExpressionChildsParameters(Map<String, List<String>> childs, String baseClass) {
        childs.clear();
        childs.put("Binary", Arrays.asList(baseClass + " left", "Token operator", baseClass + " right"));
        childs.put("Unary", Arrays.asList("Token operator", baseClass + " right"));
        childs.put("Literal", Arrays.asList("Object value"));
        childs.put("Grouping", Arrays.asList(baseClass + " expression"));        
    }

    public static void prepareStatementChildsParameters(Map<String, List<String>> childs, String baseClass, String... childNames) {
        childs.clear();
        for(String child: childNames) {
            childs.put(child + baseClass, Arrays.asList("Expression expression"));
        }
    }

    public static void main(String[] args) throws IOException, Exception {
        if (args.length < 3) {
            final String name = new Object() {
            }.getClass().getEnclosingClass().getSimpleName();
            System.err.println("[Usage] jlox.tools." + name + " " + "<generation-mode> <output-directory> <base-class> [<child1> <child2> ...]");
            System.exit(64);
        }
        final String outDir = args[1];
        final Map<String, List<String>> childs = new HashMap<>();

        switch(args[0].toLowerCase()) {
            case "expr":
                prepareExpressionChildsParameters(childs, args[2]);
                generate(outDir, args[2], childs);
                break;
            case "stmt":
                prepareStatementChildsParameters(childs, args[2], Arrays.stream(args).skip(3).toArray(String[]::new));
                generate(outDir, args[2], childs);
                break;
            case "both":
                prepareExpressionChildsParameters(childs, args[2]);
                generate(outDir, args[2], childs);
                prepareStatementChildsParameters(childs, args[3], Arrays.stream(args).skip(4).toArray(String[]::new));
                generate(outDir, args[3], childs);
                break;
            default:
                System.err.println("Invalid mode! modes: expr, stmt, both");
                System.exit(64);
        }

    }

    public static void generate(final String outputDirectory, final String baseClass,
            final Map<String, List<String>> subClasses) throws IOException, Exception {
        PrintWriter writer = new PrintWriter(outputDirectory + "/" + baseClass + ".java");
        writer.println("package jlox;\n");
        writer.println("public abstract class " + baseClass + " {");
        final Set<String> childNames = subClasses.keySet();

        defineVisitor(writer, baseClass, childNames);

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
            writer.println("\n" + constructorDecleration + " {" + constructorBody + subClassTabs + "}\n");

            writer.println(tabs + "\t@Override<T>\n" + tabs + "\tT accept(Visitor<T> visitor) {");
            writer.println(tabs + "\t\treturn visitor.visit" + getVisitorName(className, baseClass) + "(this);\n" + tabs + "\t}");
            writer.println(tabs + "}\n");
        }
        writer.println("\tabstract <T> T accept(Visitor<T> visitor);\n}");
        writer.close();
    }

    public static String getVisitorName(final String className, final String baseClass) {
        return className.toLowerCase().contains(baseClass.toLowerCase()) ? className : className + baseClass; // prevent unnecessary repeats
    }

    public static void defineVisitor(PrintWriter writer, final String baseClass, final Set<String> subClasses) {
        writer.println("\tpublic interface Visitor<T> {");

        for(String cls: subClasses) {
            writer.println("\t\tT visit" + getVisitorName(cls, baseClass) + "(" + cls + " " + baseClass.toLowerCase() + ");");
        }
        writer.println("\t}\n");
    }
}
