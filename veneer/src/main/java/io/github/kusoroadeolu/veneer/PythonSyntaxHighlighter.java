package io.github.kusoroadeolu.veneer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.core.utils.Constants;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;
import io.github.kusoroadeolu.veneer.utils.Utils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;


import static io.github.kusoroadeolu.veneer.utils.Utils.*;

public class PythonSyntaxHighlighter implements SyntaxHighlighter {

    private final SyntaxTheme theme;
    private final boolean showLineNumbers;

    public PythonSyntaxHighlighter() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public PythonSyntaxHighlighter(SyntaxTheme theme) {
        this(theme, true);
    }

    public PythonSyntaxHighlighter(boolean showLineNumbers) {
        this(SyntaxThemes.DEFAULT, showLineNumbers);
    }


    public PythonSyntaxHighlighter(SyntaxTheme theme, boolean showLineNumbers) {
        this.theme = theme;
        this.showLineNumbers = showLineNumbers;
    }

    @Override
    public String highlight(String s) {
        if (isNullOrBlank(s)) return "";

        StyleBuilder sb = Clique.styleBuilder();
        PythonLexer lexer = new PythonLexer(CharStreams.fromString(s));
        var tokenStream = Utils.toTokenStream(lexer);
        int[] lineNumber = new int[1];
        sb.append(formatNoTo3dp(++lineNumber[0]) , theme.gutter());

        for (Token token : tokenStream.getTokens()) {
            if (token.getType() == PythonLexer.NEWLINE && showLineNumbers) {
                sb.append(Constants.NEWLINE);
                sb.append(formatNoTo3dp(++lineNumber[0]), theme.gutter());
            } else if(isMultiLineToken(token)){
                styleMultiLineToken(token, lineNumber, sb, theme.gutter(), this::applyStyles);
            } else {
                applyStyles(token, sb);
            }
        }
        return sb.get();
    }

    void applyStyles(Token token, StyleBuilder sb) {
        if (token.getType() == Token.EOF) return;
        else if (token.getType() == PythonLexer.INDENT || token.getType() == PythonLexer.DEDENT) return;
        else if (token.getChannel() == Token.HIDDEN_CHANNEL && token.getType() != PythonLexer.WS) return;

        String text = token.getText();
        if (isComment(token)) {
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

    boolean isMultiLineToken(Token token){
        int t = token.getType();
        return t == PythonLexer.FSTRING_MIDDLE || isComment(token) || t == PythonLexer.STRING;
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