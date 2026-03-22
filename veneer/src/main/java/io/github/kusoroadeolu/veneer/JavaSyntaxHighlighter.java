package io.github.kusoroadeolu.veneer;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.javaparser.GeneratedJavaParserConstants.*;
import static io.github.kusoroadeolu.veneer.Utils.formatNoTo3dp;

public class JavaSyntaxHighlighter implements SyntaxHighlighter{
    private final JavaParser parser;
    private final SyntaxTheme theme;
    private final boolean showLineNumbers;
    private static final String VAR = "var";

    public JavaSyntaxHighlighter() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public JavaSyntaxHighlighter(SyntaxTheme theme)  {
        this(theme, true);
    }

    public JavaSyntaxHighlighter(boolean showLineNumbers)  {
        this(SyntaxThemes.DEFAULT, showLineNumbers);
    }

    public JavaSyntaxHighlighter(SyntaxTheme theme, boolean showLineNumbers) {
        var config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
        this.theme = theme;
        this.showLineNumbers = showLineNumbers;
    }

    @Override
    public String highlight(String s) {
        if (s == null || s.isBlank()) return "";
        ParseResult<CompilationUnit> result = parser.parse(s);
        Optional<CompilationUnit> opUnit = result.getResult();
        if (opUnit.isEmpty()) return s;

        CompilationUnit unit = opUnit.get();
        Optional<TokenRange> opTokenRange = unit.getTokenRange();
        if (opTokenRange.isEmpty()) return s;

        TokenRange tokenRange = opTokenRange.get();
        return tryStyleString(unit,tokenRange);
    }

    String tryStyleString(CompilationUnit unit, TokenRange tokenRange){
        Set<JavaToken> methodNames = findMethodAndConstructorIdentifiers(unit);
        Set<JavaToken> typeTokens = findTypeDefinitions(unit);
        Set<JavaToken> constants = findConstants(unit);
        var bundle = new AstBundle(methodNames, typeTokens, constants);
        StyleBuilder sb = Clique.styleBuilder();

        if (showLineNumbers) styleWithLines(sb, tokenRange, bundle);
        else styleWithoutLines(sb, tokenRange, bundle);

        return sb.get();
    }


    void styleWithLines(StyleBuilder sb, TokenRange tokenRange, AstBundle astBundle){
        var lineNo = new int[1];
        for (JavaToken token : tokenRange){
            if (isMultiLineToken(token)){
                styleMultiLineContent(token, lineNo, sb, astBundle);
                continue;
            }

            //If we're the first token otherwise, if the prev token was an EOL
            if (token.getPreviousToken().isEmpty() || isEOL(token.getPreviousToken().get())){
                appendLineNo(++lineNo[0], sb);
            }

            applyStyle(token, sb, astBundle);
        }
    }

    void styleWithoutLines(StyleBuilder sb, TokenRange tokenRange, AstBundle astBundle){
        for (JavaToken token : tokenRange){
            applyStyle(token, sb, astBundle);
        }
    }

    void applyStyle(JavaToken token, StyleBuilder sb, AstBundle astBundle){
        if (token.getCategory().isWhitespaceButNotEndOfLine()) {
            sb.append(token.getText());
            return;
        }

        String text = token.getText();
        if(isConstant(token, astBundle.constants())){
            sb.append(text, theme.constants());
        }else if (isStringOrJavadoc(token)){
            sb.append(text, theme.stringLiteral());
        } else if (isNumberLiteral(token)) {
            sb.append(text, theme.numberLiteral());
        }else if (isComment(token)){
            sb.append(text, theme.comment());
        }else if(isEOL(token)){
            sb.append(text);
        } else if (isAnnotation(token)) {
            sb.append(text, theme.annotation());
        }else if(isTypeToken(token, astBundle.typeDef())) {
                sb.append(text, theme.types());
        }else if(isMethodOrConstructorIdentifier(token, astBundle.mcIdentifiers())) {
            sb.append(text, theme.method());
        }else if (isKeyword(token) || isUnicodeEscape(token)) { //Moved this to the bottom to prevent "var" from clashing with valid identifiers
                sb.append(text, theme.keyword());
        }else sb.append(text);
    }

    void styleMultiLineContent(JavaToken token, int[] lineNo, StyleBuilder sb, AstBundle astBundle){
        List<CustomJavaToken> tokens = token.getText()
                .lines()
                .map(text -> new CustomJavaToken(token.getKind(), text))
                .toList();

        int size = tokens.size();

        boolean startsOnNewLine = token.getPreviousToken().isEmpty() || isEOL(token.getPreviousToken().get());

        for (int i = 0; i < size; ++i) {
            var custom = tokens.get(i);
            if (i == 0 && startsOnNewLine) {
                appendLineNo(++lineNo[0], sb);
            } else if (i > 0) {
                sb.append("\n");
                appendLineNo(++lineNo[0], sb);
            }

            applyStyle(custom, sb, astBundle);
        }
    }


    Set<JavaToken> findTypeDefinitions(CompilationUnit unit){
        var typeTokens = new HashSet<JavaToken>();
        unit.findAll(ClassOrInterfaceType.class).forEach(t ->
                t.getTokenRange().ifPresent(r -> {
            for (JavaToken token : r) {
                if (token.getKind() == IDENTIFIER)
                    typeTokens.add(token);
            }
        }));
        return typeTokens;
    }


    Set<JavaToken> findConstants(CompilationUnit unit){
        var constantTokens = new HashSet<JavaToken>();
        unit.findAll(FieldDeclaration.class).stream()
                .filter(f -> f.isStatic() && f.isFinal())
                .forEach(f -> f.getVariables().forEach(v ->
                        v.getName().getTokenRange()
                                .ifPresent(r -> {
                    for (JavaToken token : r) {
                        if (token.getKind() == IDENTIFIER)
                            constantTokens.add(token);
                    }
                })));

        unit.findAll(com.github.javaparser.ast.body.EnumConstantDeclaration.class).forEach(e ->
                e.getName().getTokenRange().ifPresent(r -> {
                    for (JavaToken token : r)
                        if (token.getKind() == IDENTIFIER)
                            constantTokens.add(token);
                }));
        return constantTokens;
    }

    //Find valid method identifiers
    Set<JavaToken> findMethodAndConstructorIdentifiers(CompilationUnit unit){
        var methodTokens = new HashSet<JavaToken>();
        unit.findAll(MethodDeclaration.class).forEach(m ->
                m.getName().getTokenRange().ifPresent(r -> {
                    for (JavaToken token : r) {
                        if (token.getKind() == IDENTIFIER)
                            methodTokens.add(token);
                    }
                }));

        unit.findAll(ConstructorDeclaration.class).forEach(c ->
                c.getName().getTokenRange().ifPresent(r -> {
                    for (JavaToken token : r) {
                        if (token.getKind() == IDENTIFIER)
                            methodTokens.add(token);
                    }
                }));

        return methodTokens;
    }

    boolean isConstant(JavaToken token, Set<JavaToken> constants){
        return constants.contains(token);
    }

    boolean isTypeToken(JavaToken token, Set<JavaToken> typeTokens) {
        return typeTokens.contains(token);
    }

    void appendLineNo(int lineNo, StyleBuilder sb){
        sb.append(formatNoTo3dp(lineNo), theme.gutter());
    }

    boolean isMethodOrConstructorIdentifier(JavaToken token, Set<JavaToken> methodTokens){
        return methodTokens.contains(token);
    }

    boolean isKeyword(JavaToken token){
        return token.getCategory().isKeyword()
                || (token.getCategory().isIdentifier() && token.getText().equals(VAR));
    }

    //Text blocks, string literals and java docs will have the same color
    boolean isStringOrJavadoc(JavaToken token){
        return token.getKind() >= STRING_LITERAL && token.getKind() <= TEXT_BLOCK_LITERAL || token.getKind() == ENTER_JAVADOC_COMMENT || token.getKind() == JAVADOC_COMMENT;
    }

    //These will probably have blue colors
    boolean isNumberLiteral(JavaToken token){
        return token.getKind() >= LONG_LITERAL && token.getKind() <= HEXADECIMAL_FLOATING_POINT_LITERAL;
    }

    //Same colors as keywords
    boolean isUnicodeEscape(JavaToken token){
        return token.getKind() == UNICODE_ESCAPE;
    }

    boolean isComment(JavaToken token){
        return token.getCategory().isComment() || token.getKind() == ENTER_MULTILINE_COMMENT || token.getKind() == MULTI_LINE_COMMENT;
    }

    //Helper for line counting
    boolean isMultiLineToken(JavaToken token){
        return token.getKind() == ENTER_MULTILINE_COMMENT || token.getKind() == ENTER_JAVADOC_COMMENT ||  token.getKind() == MULTI_LINE_COMMENT || token.getKind() == JAVADOC_COMMENT || token.getKind() == TEXT_BLOCK_LITERAL || token.getKind() == TEXT_BLOCK_CONTENT;
    }

    boolean isEOL(JavaToken token){
        return token.getCategory().isEndOfLine();
    }

    boolean isAnnotation(JavaToken token){
        return token.getKind() == JavaToken.Kind.AT.getKind();
    }

    //Since idk what to name this tbh, this is just a wrapper class to hold tokens/strings gotten from walking the ast, that we couldnt have normally styled from the token range
    private record AstBundle(Set<JavaToken> mcIdentifiers, Set<JavaToken> typeDef, Set<JavaToken> constants){

    }

}