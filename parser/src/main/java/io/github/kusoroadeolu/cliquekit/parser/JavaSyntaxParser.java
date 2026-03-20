package io.github.kusoroadeolu.cliquekit.parser;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.core.utils.Constants;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
import io.github.kusoroadeolu.clique.spi.AnsiCode;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.cliquekit.parser.theme.SyntaxTheme;
import io.github.kusoroadeolu.cliquekit.parser.theme.SyntaxThemes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.javaparser.GeneratedJavaParserConstants.*;

public class JavaSyntaxParser implements AnsiStringParser {
    private final JavaParser parser;
    private final SyntaxTheme theme;

    public JavaSyntaxParser() {
        this(SyntaxThemes.DEFAULT);
    }

    public JavaSyntaxParser(SyntaxTheme theme) {
        var config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
        this.theme = theme;
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
        if (!result.isSuccessful()) {
            s = wrapSnippet(s);
        }

        Optional<CompilationUnit> opUnit = result.getResult();
        if (opUnit.isEmpty()) return s;

        CompilationUnit unit = opUnit.get();
        var opTokenRange = unit.getTokenRange();
        if (opTokenRange.isEmpty()) return s;

        var tokenRange = opTokenRange.get();
        Set<String> methodNames = findMethodAndConstructorIdentifiers(unit);
        var sb = Clique.styleBuilder();
        var lineNo = new LineNumber();

        for (JavaToken token : tokenRange){
            if (isMultiLineToken(token)){
                styleMultiLineContent(token, lineNo, sb, methodNames);
                continue;
            }

            //If we're the first token otherwise, if the prev token was an EOL
            if (token.getPreviousToken().isEmpty() || isEOL(token.getPreviousToken().get())){
                appendLineNo(lineNo.nextLine(), sb);
            }

            applyStyle(token, sb, methodNames);
        }

        if (!result.isSuccessful()){
           return unwrapSnippet(sb.get());
        }else return sb.get();
    }

    void applyStyle(JavaToken token, StyleBuilder sb, Set<String> methodNames){
        String text = token.getText();
        if (isKeyword(token) || isUnicodeEscape(token)){
            sb.append(text, theme.keyword());
        }else if (isStringOrJavadoc(token)){
            sb.append(text, theme.string());
        } else if (isLiteral(token)) {
            sb.append(text, theme.numberLiteral());
        }else if (isComment(token)){
            sb.append(text, theme.comment());
        }else if(isEOL(token)){
            sb.append(text);
        } else if (isAnnotation(token)) {
            sb.append(text, theme.annotation());
        }else if(isMethodOrConstructorIdentifier(token, methodNames)){
            sb.append(text, theme.method());
        }else sb.append(text);
    }

    void styleMultiLineContent(JavaToken token, LineNumber lineNo, StyleBuilder sb, Set<String> methodNames){
        List<CustomJavaToken> tokens = token.getText()
                .lines()
                .map(text -> new CustomJavaToken(token.getKind(), text))
                .toList();

        int size = tokens.size();

        for (int i = 0; i < size; ++i){
            var custom = tokens.get(i);
            if (i > 0) {
                sb.append("\n");
                appendLineNo(lineNo.nextLine(), sb);
            }

            applyStyle(custom, sb, methodNames);
        }
    }

    Set<String> findMethodAndConstructorIdentifiers(CompilationUnit unit){
        var methodNames = new HashSet<String>();
        unit.findAll(MethodDeclaration.class).forEach(m -> methodNames.add(m.getNameAsString()));
        unit.findAll(ConstructorDeclaration.class).forEach(c -> methodNames.add(c.getNameAsString()));
        return methodNames;
    }

    void appendLineNo(int lineNo, StyleBuilder sb){
        sb.append(lineNo + ". ", theme.gutter());
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

    boolean isMultiLineToken(JavaToken token){
        return token.getKind() == MULTI_LINE_COMMENT || token.getKind() == JAVADOC_COMMENT || token.getKind() == TEXT_BLOCK_LITERAL || token.getKind() == TEXT_BLOCK_CONTENT;
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

    class LineNumber{
        private int lineNo;

        int nextLine(){
            return ++lineNo;
        }
    }
}