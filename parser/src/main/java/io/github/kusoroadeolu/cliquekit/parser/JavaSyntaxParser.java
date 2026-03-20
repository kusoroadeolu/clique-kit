package io.github.kusoroadeolu.cliquekit.parser;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.ansi.ColorCode;
import io.github.kusoroadeolu.clique.core.utils.Constants;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
import io.github.kusoroadeolu.clique.spi.AnsiCode;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.javaparser.GeneratedJavaParserConstants.*;

public class JavaSyntaxParser implements AnsiStringParser {

    private static final AnsiCode KEYWORD        = Clique.rgb(204, 120, 50);
    private static final AnsiCode STRING         = Clique.rgb(106, 135, 89);
    private static final AnsiCode NUMBER_LITERAL = Clique.rgb(104, 151, 187);
    private static final AnsiCode COMMENT        = Clique.rgb(128, 128, 128);
    private static final AnsiCode ANNOTATION     = Clique.rgb(187, 181, 41);
    private static final AnsiCode METHOD         = Clique.rgb(255, 198, 109);
    private final JavaParser parser;

    public JavaSyntaxParser() {
        var config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
    }

    private static final String WRAPPER = """
            class __Snippet__{
                void __method__(){
                    %s
                }
            }
            """;

    private String wrapSnippet(String snippet) {
        return WRAPPER.formatted(snippet);
    }

    @Override
    public String parse(String s) {
        if (s == null || s.isBlank()) return "";
        ParseResult<CompilationUnit> result = parser.parse(s);
        if (!result.isSuccessful()) return s;
        Optional<CompilationUnit> opUnit = result.getResult();
        if (opUnit.isEmpty()) return s;
        CompilationUnit unit = opUnit.get();

        var opTokenRange = unit.getTokenRange();
        if (opTokenRange.isEmpty()) return s;

        var tokenRange = opTokenRange.get();
        Set<String> methodNames = findMethodAndConstructorIdentifiers(unit);


        boolean inAnnotation = false;
        var sb = Clique.styleBuilder();
        for (JavaToken token : tokenRange){
            String text = token.getText();
            if (isKeyword(token) || isUnicodeEscape(token)){
                sb.append(text, KEYWORD);
            }else if (isStringOrJavadoc(token)){
                sb.append(text, STRING);
            } else if (isLiteral(token)) {
                sb.append(text, NUMBER_LITERAL);
            }else if (isComment(token)){
                sb.append(text, COMMENT);
            }else if(isEOL(token)){
                sb.append(text);
                inAnnotation = false;
            } else if (isAnnotation(token) || inAnnotation) {
                sb.append(text, ANNOTATION);
                inAnnotation = true;
            }else if(isMethodOrConstructorIdentifier(token, methodNames)){
                sb.append(text, METHOD);
            }else sb.append(text);
        }
        return sb.get();
    }

    Set<String> findMethodAndConstructorIdentifiers(CompilationUnit unit){
        var methodNames = new HashSet<String>();
        unit.findAll(MethodDeclaration.class).forEach(m -> methodNames.add(m.getNameAsString()));
        unit.findAll(ConstructorDeclaration.class).forEach(c -> methodNames.add(c.getNameAsString()));
        return methodNames;
    }

    boolean isMethodOrConstructorIdentifier(JavaToken token, Set<String> methodNames){
        return token.getKind() == IDENTIFIER && methodNames.contains(token.getText());
    }

    boolean isKeyword(JavaToken token){
        return token.getKind() >= ABSTRACT && token.getKind() <= WHEN;
    }

    //Text blocks, string literals and java docs will have the same color
    boolean isStringOrJavadoc(JavaToken token){
        return token.getKind() >= STRING_LITERAL  && token.getKind() <= TEXT_BLOCK_LITERAL || token.getKind() == ENTER_JAVADOC_COMMENT || token.getKind() == JAVADOC_COMMENT;
    }

    //These will be blue colored
    boolean isLiteral(JavaToken token){
        return token.getKind() >= LONG_LITERAL && token.getKind() <= HEXADECIMAL_FLOATING_POINT_LITERAL;
    }

    boolean isUnicodeEscape(JavaToken token){
        return token.getKind() == UNICODE_ESCAPE;
    }

    boolean isComment(JavaToken token){
        return TokenTypes.isComment(token.getKind()) || token.getKind() == ENTER_MULTILINE_COMMENT || token.getKind() == MULTI_LINE_COMMENT;
    }

    boolean isEOL(JavaToken token){
        return TokenTypes.isEndOfLineToken(token.getKind());
    }

    boolean isAnnotation(JavaToken token){
        return token.getKind() == JavaToken.Kind.AT.getKind();
    }

    @Override
    public String parse(Object o) {
        return parse(o.toString());
    }

    @Override
    public String getOriginalString(String s) {
        return AnsiStringParser.DEFAULT.getOriginalString(s);
    }

    @Override
    public List<AnsiCode> ansiCodes(String s) {
        return List.of();
    }

    /**
     * Strips the wrapper class/method shell lines from the output,
     * leaving only the user's original snippet lines.
     */
    private String unwrapSnippet(String wrapped) {
        String[] lines = wrapped.split(Constants.NEWLINE, -1);
        // WRAPPER_START contributes 2 lines, WRAPPER_END contributes 2 lines
        if (lines.length <= 4) return wrapped;
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < lines.length - 2; i++) {
            if (i > 2) sb.append("\n");
            sb.append(lines[i]);
        }
        return sb.toString();
    }
}