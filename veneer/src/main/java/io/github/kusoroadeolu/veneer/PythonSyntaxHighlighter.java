package io.github.kusoroadeolu.veneer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import io.github.kusoroadeolu.veneer.PythonLexer;

public class PythonSyntaxHighlighter implements SyntaxHighlighter {

    private final SyntaxTheme theme;
    private final boolean allowLineCount;

    public PythonSyntaxHighlighter() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public PythonSyntaxHighlighter(SyntaxTheme theme) {
        this(theme, true);
    }

    public PythonSyntaxHighlighter(boolean allowLineCount) {
        this(SyntaxThemes.DEFAULT, allowLineCount);
    }


    public PythonSyntaxHighlighter(SyntaxTheme theme, boolean allowLineCount) {
        this.theme = theme;
        this.allowLineCount = allowLineCount;
    }

    @Override
    public String highlight(String s) {
        if (s == null || s.isBlank()) return "";
        StyleBuilder sb = Clique.styleBuilder();
        PythonLexer lexer = new PythonLexer(CharStreams.fromString(s));
        BufferedTokenStream tokenStream = new BufferedTokenStream(lexer);
        tokenStream.fill();
        int lineNumber = 0;
        for (Token token : tokenStream.getTokens()) {
            if (token.getType() == PythonLexer.NEWLINE && allowLineCount) {
                sb.append("\n");
                lineNumber++;
                sb.append(Utils.formatNoTo3dp(lineNumber) , theme.gutter());
            } else {
                applyStyle(token, sb);
            }
        }
        return sb.get();
    }

    void applyStyle(Token token, StyleBuilder sb) {
        if (token.getType() == Token.EOF) return;
        if (token.getType() == PythonLexer.INDENT || token.getType() == PythonLexer.DEDENT) return;
        if (token.getChannel() == Token.HIDDEN_CHANNEL && token.getType() != PythonLexer.WS) return;

        String text = token.getText();

        if (isWhitespace(token)) {
            sb.append(text);
        } else if (isComment(token)) {
            sb.append(text, theme.comment());
        } else if (isString(token)) {
            sb.append(text, theme.stringLiteral());
        } else if (isNumber(token)) {
            sb.append(text, theme.numberLiteral());
        } else if (isAnnotation(token)) {
            sb.append(text, theme.annotation());
        } else if (isKeyword(token)) {
            sb.append(text, theme.keyword());
        } else {
            sb.append(text);
        }
    }

    boolean isWhitespace(Token token) {
        int t = token.getType();
        return t == PythonLexer.WS
                || t == PythonLexer.NEWLINE
                || t == PythonLexer.INDENT
                || t == PythonLexer.DEDENT
                || t == PythonLexer.EXPLICIT_LINE_JOINING;
    }

    boolean isKeyword(Token token) {
        int t = token.getType();
        return t >= PythonLexer.FALSE && t <= PythonLexer.YIELD
                || t == PythonLexer.NAME_OR_TYPE
                || t == PythonLexer.NAME_OR_MATCH
                || t == PythonLexer.NAME_OR_CASE
                || t == PythonLexer.NAME_OR_WILDCARD;
    }

    boolean isString(Token token) {
        int t = token.getType();
        return t == PythonLexer.STRING
                || t == PythonLexer.FSTRING_START
                || t == PythonLexer.FSTRING_MIDDLE
                || t == PythonLexer.FSTRING_END;
    }

    boolean isNumber(Token token) {
        return token.getType() == PythonLexer.NUMBER;
    }

    boolean isComment(Token token) {
        return token.getType() == PythonLexer.COMMENT;
    }

    boolean isAnnotation(Token token) {
        return token.getType() == PythonLexer.AT;
    }
}