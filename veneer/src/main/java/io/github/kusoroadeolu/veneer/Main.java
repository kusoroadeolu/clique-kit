package io.github.kusoroadeolu.veneer;

public class Main {
    public static void main(String[] args) {
        String code = """
                public class QuirkTest {
                
                    // Quirk 1: 'var' as variable name (legal but will style as keyword)
                    public void testVarAsIdentifier() {
                        int var = 42;  // 'var' used as variable name
                        System.out.println(var);
                    }
                
                    // Quirk 2: Method name matches type name
                    public void List() {  // method named 'List'
                        System.out.println("Method called List");
                    }
                
                    public void testListMethod() {
                        List();  // calling the method
                        java.util.List<String> list = new ArrayList<>();  // actual List type
                    }
                
                    // Quirk 3: Local variable matches method name
                    public void calculate() {
                        return 100;
                    }
                
                    public void testVariableMatchesMethod() {
                        int calculate = 50;  // variable named same as method
                        System.out.println(calculate);
                        System.out.println(calculate());  // actual method call
                    }
                
                    // Bonus: 'var' as actual keyword (should always work)
                    public void testVarKeyword() {
                        var message = "Hello";
                        var number = 42;
                        var list = new ArrayList<String>();
                    }
                }
                """;

        new JavaSyntaxHighlighter().print(code);

    }
}
