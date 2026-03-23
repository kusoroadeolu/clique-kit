package io.github.kusoroadeolu.veneer;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;
import io.github.kusoroadeolu.veneer.utils.Utils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import static io.github.kusoroadeolu.veneer.utils.Utils.*;

public class JavaScriptSyntaxHighlighter implements SyntaxHighlighter{
    private final boolean showLineNumbers;
    private final SyntaxTheme theme;

    public JavaScriptSyntaxHighlighter() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public JavaScriptSyntaxHighlighter(SyntaxTheme theme) {
        this(theme, true);
    }

    public JavaScriptSyntaxHighlighter(boolean showLineNumbers) {
        this(SyntaxThemes.DEFAULT, showLineNumbers);
    }

    public JavaScriptSyntaxHighlighter(SyntaxTheme theme, boolean showLineNumbers) {
        this.theme = theme;
        this.showLineNumbers = showLineNumbers;
    }

    @Override
    public String highlight(String s) {
        if (isNullOrBlank(s)) return "";

        StyleBuilder sb = Clique.styleBuilder();
        JavaScriptLexer lexer = new JavaScriptLexer(CharStreams.fromString(s));
        var tokenStream = toTokenStream(lexer);
        int[] lineNumber = new int[]{1};

        if (showLineNumbers) {
            sb.append(Utils.formatNoTo3dp(lineNumber[0]), theme.gutter());
        }

        for (Token token : tokenStream.getTokens()) {
            if (showLineNumbers && isMultiLineToken(token)) {
                styleMultiLineToken(token, lineNumber, sb, theme.gutter(), this::applyStyles);
            } else if (showLineNumbers && isLineTerminator(token)) {
                sb.append(token.getText());
                sb.append(Utils.formatNoTo3dp(++lineNumber[0]), theme.gutter());
            } else {
                applyStyles(token, sb);
            }
        }


        return sb.get();
    }



    void applyStyles(Token token, StyleBuilder sb){
        if (isKeyword(token)){
            sb.append(token.getText(), theme.keyword());
        }else if (isStringLiteral(token)){
            sb.append(token.getText(), theme.stringLiteral());
        } else if (isNumberLiteral(token)) {
            sb.append(token.getText(), theme.numberLiteral());
        }else if (isComment(token)) {
            sb.append(token.getText(), theme.comment());
        } else if (!isEOF(token)) {
            sb.append(token.getText());
        }
    }

    boolean isLineTerminator(Token token) {
        int t = token.getType();
        return t == JavaScriptLexer.LineTerminator
                || t == JavaScriptLexer.JsxOpeningElementLineTerminator
                || t == JavaScriptLexer.JsxClosingElementLineTerminator;
    }

    boolean isKeyword(Token token) {
        int t = token.getType();
        return t >= JavaScriptLexer.Break && t <= JavaScriptLexer.Yield;
    }

    boolean isNumberLiteral(Token token) {
        int t = token.getType();
        return t >= JavaScriptLexer.DecimalLiteral && t <= JavaScriptLexer.BigDecimalIntegerLiteral;
    }

    boolean isStringLiteral(Token token) {
        int t = token.getType();
        return t == JavaScriptLexer.StringLiteral
                || t == JavaScriptLexer.LinkLiteral
                || t == JavaScriptLexer.BackTick
                || t == JavaScriptLexer.TemplateStringAtom;
    }

    boolean isComment(Token token) {
        int t = token.getType();
        return t == JavaScriptLexer.SingleLineComment
                || t == JavaScriptLexer.MultiLineComment
                || t == JavaScriptLexer.JsxComment
                || t == JavaScriptLexer.HtmlComment
                || t == JavaScriptLexer.CDataComment;
    }


    boolean isMultiLineToken(Token token) {
        int t = token.getType();
        return t == JavaScriptLexer.MultiLineComment
                || t == JavaScriptLexer.JsxComment
                || t == JavaScriptLexer.CDataComment
                || t == JavaScriptLexer.TemplateStringAtom;
    }

    boolean isEOF(Token token){
        return token.getType() == Token.EOF;
    }
}
