package io.github.kusoroadeolu.cliquekit.parser;

import io.github.kusoroadeolu.clique.ansi.StyleCode;
import io.github.kusoroadeolu.cliquekit.parser.theme.SyntaxThemes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSyntaxParserTest {

    private JavaSyntaxParser parser;

    @BeforeEach
    void setUp() {
        parser = new JavaSyntaxParser(false); //No line counting
    }

    @Test
    //Here i'm using the default style so i'll be testing against that
    public void parse_onValidSyntax_shouldReturnStyledString(){
        String codeSnippet = """
            @SuppressWarnings("unchecked")
            public class Example {
            }
            """;
        String styled = parser.parse(codeSnippet);
        List<String> list = styled.lines().toList();
        assertTrue(list.getFirst().contains(SyntaxThemes.DEFAULT.annotation().toString()));
        assertTrue(list.getFirst().contains(SyntaxThemes.DEFAULT.string().toString()));
        assertTrue(list.get(1).contains(SyntaxThemes.DEFAULT.keyword().toString()));
    }

    @Test
    //Here i'm using the default style so i'll be testing against that
    public void parse_onInvalidSyntax_shouldReturnEqualString(){
        String codeSnippet = """
            Some garbage
            """;
        String styled = parser.parse(codeSnippet);
        styled = styled.replace(StyleCode.RESET.toString(), ""); //Replace all the resets, since resets will be initially applied here
        assertEquals(codeSnippet, styled);
    }

    @Test
    public void parseCompleteSyntax_onIncompleteSyntax_shouldReturnStyledString(){
        String codeSnippet = """
            void main(){
                int a = 1;
            }
            """;

        String styled = parser.parse(codeSnippet);
        List<String> list = styled.lines().toList();
        //This wont render the method name correctly
        assertTrue(list.getFirst().contains(SyntaxThemes.DEFAULT.keyword().toString()));
        assertTrue(list.get(1).contains(SyntaxThemes.DEFAULT.keyword().toString()));
    }

    @Test
    public void parseIncompleteSyntax_withLinesEnabled_shouldCorrectlyFormatLines(){
        String codeSnippet = """
            @SuppressWarnings("unchecked")
            public class Example {
            }
            """;
        var parser = new JavaSyntaxParser();
        String styled = parser.parse(codeSnippet);
        List<String> list = styled.lines().toList();
        assertTrue(list.getFirst().contains("1"));
        assertTrue(list.getFirst().contains("2"));
        assertTrue(list.get(1).contains("3"));
    }

    @Test
    public void parse_withLinesEnabled_shouldCorrectlyFormatLines(){
        String codeSnippet = """
            void main(){
                int a = 1;
            }
            """;
        var parser = new JavaSyntaxParser();
        String styled = parser.parse(codeSnippet);
        IO.println(styled);
        List<String> list = styled.lines().toList();
        assertTrue(list.getFirst().contains("1"));
        assertTrue(list.getFirst().contains("2"));
        assertTrue(list.get(1).contains("3"));
    }


}