package io.github.kusoroadeolu.cliquekit.parser;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
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
    private final boolean allowLineCount;

    public JavaSyntaxParser() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public JavaSyntaxParser(SyntaxTheme theme)  {
        this(theme, true);
    }

    public JavaSyntaxParser(boolean allowLineCount)  {
        this(SyntaxThemes.DEFAULT, allowLineCount);
    }

    public JavaSyntaxParser(SyntaxTheme theme, boolean allowLineCount) {
        var config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.parser = new JavaParser(config);
        this.theme = theme;
        this.allowLineCount = allowLineCount;
    }



    @Override
    public String parse(String s) {
        if (s == null || s.isBlank()) return "";
        ParseResult<CompilationUnit> result = parser.parse(s);
        return tryStyleString(s, result);
    }

    @Override
    public String parse(Object o) {
        return parse(o.toString());
    }

    @Override
    public String getOriginalString(String s) {
        return AnsiStringParser.DEFAULT.getOriginalString(s);
    }

    String tryStyleString(String s, ParseResult<CompilationUnit> result){
        Optional<CompilationUnit> opUnit = result.getResult();
        if (opUnit.isEmpty()) return s;

        CompilationUnit unit = opUnit.get();
        var opTokenRange = unit.getTokenRange();
        if (opTokenRange.isEmpty()) return s;

        var tokenRange = opTokenRange.get();
        Set<String> methodNames = findMethodAndConstructorIdentifiers(unit);
        Set<JavaToken> typeTokens = findTypeDefinitions(unit);
        Set<JavaToken> constants = findFieldConstants(unit);
        var special = new Special(methodNames, typeTokens, constants);
        var sb = Clique.styleBuilder();

        if (allowLineCount) styleWithLines(sb, tokenRange, special);
        else styleWithoutLines(sb, tokenRange, special);

        return sb.get();
    }


    void styleWithLines(StyleBuilder sb, TokenRange tokenRange, Special special){
        var lineNo = new int[1];
        for (JavaToken token : tokenRange){
            if (isMultiLineToken(token)){
                styleMultiLineContent(token, lineNo, sb, special);
                continue;
            }

            //If we're the first token otherwise, if the prev token was an EOL
            if (token.getPreviousToken().isEmpty() || isEOL(token.getPreviousToken().get())){
                appendLineNo(++lineNo[0], sb);
            }

            applyStyle(token, sb, special);
        }
    }

    void styleWithoutLines(StyleBuilder sb, TokenRange tokenRange, Special special){
        for (JavaToken token : tokenRange){
            applyStyle(token, sb, special);
        }
    }

    void applyStyle(JavaToken token, StyleBuilder sb, Special special){
        String text = token.getText();
        if (isKeyword(token) || isUnicodeEscape(token)) {
            sb.append(text, theme.keyword());
        }else if(isConstant(token, special.constants())){
            sb.append(text, theme.constants());
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
        }else if(isMethodOrConstructorIdentifier(token, special.mcIdentifiers())) {
            sb.append(text, theme.method());
        }else if(isTypeToken(token, special.typeDef())){
            sb.append(text, theme.types());
        }else sb.append(text);
    }

    void styleMultiLineContent(JavaToken token, int[] lineNo, StyleBuilder sb, Special special){
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

            applyStyle(custom, sb, special);
        }
    }


    Set<JavaToken> findTypeDefinitions(CompilationUnit unit){
        var typeTokens = new HashSet<JavaToken>();
        unit.findAll(ClassOrInterfaceType.class).forEach(t -> {
            t.getTokenRange().ifPresent(r -> {
                for (JavaToken token : r) {
                    if (token.getKind() == IDENTIFIER)
                        typeTokens.add(token);
                }
            });
        });
        return typeTokens;
    }

    Set<JavaToken> findFieldConstants(CompilationUnit unit){
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
        return constantTokens;
    }

    //Find valid identifiers
    Set<String> findMethodAndConstructorIdentifiers(CompilationUnit unit){
        var typeNames = new HashSet<String>();
        unit.findAll(MethodDeclaration.class).forEach(m -> typeNames.add(m.getNameAsString()));
        unit.findAll(ConstructorDeclaration.class).forEach(c -> typeNames.add(c.getNameAsString()));
        return typeNames;
    }

    boolean isConstant(JavaToken token, Set<JavaToken> constants){
        return constants.contains(token);
    }

    boolean isTypeToken(JavaToken token, Set<JavaToken> typeTokens) {
        return typeTokens.contains(token);
    }

    void appendLineNo(int lineNo, StyleBuilder sb){
        sb.append(lineNo + " ", theme.gutter());
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
        return token.getKind() == ENTER_MULTILINE_COMMENT || token.getKind() == ENTER_JAVADOC_COMMENT ||  token.getKind() == MULTI_LINE_COMMENT || token.getKind() == JAVADOC_COMMENT || token.getKind() == TEXT_BLOCK_LITERAL || token.getKind() == TEXT_BLOCK_CONTENT;
    }

    boolean isEOL(JavaToken token){
        return TokenTypes.isEndOfLineToken(token.getKind());
    }

    boolean isAnnotation(JavaToken token){
        return token.getKind() == JavaToken.Kind.AT.getKind();
    }

    private String unwrapSnippet(String wrapped) {
        var ls = wrapped.strip().lines().toList();
        return String.join("\n", ls.subList(1, ls.size() - 2)).strip();

    }

    //Since idk what to name this tbh, this is just a wrapper class to hold tokens/strings gotten from walking the ast, that we couldnt have normally styled from the token range
    record Special(Set<String> mcIdentifiers, Set<JavaToken> typeDef, Set<JavaToken> constants){

    }

}