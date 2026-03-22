package io.github.kusoroadeolu.veneer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;
import org.antlr.v4.runtime.*;

import java.util.List;
import io.github.kusoroadeolu.veneer.GoLexer;

public class GoSyntaxHighlighter implements SyntaxHighlighter {

    private final SyntaxTheme theme;
    private final boolean showLineNumbers;

    private static final int KEYWORD_START = 1;
    private static final int KEYWORD_END   = 26;

    private static final int NUM_START = GoLexer.DECIMAL_LIT;   // 65
    private static final int NUM_END   = GoLexer.IMAGINARY_LIT; // 72

    public GoSyntaxHighlighter() {
        this(SyntaxThemes.DEFAULT, true);
    }

    public GoSyntaxHighlighter(SyntaxTheme theme) {
        this(theme, true);
    }

    public GoSyntaxHighlighter(boolean showLineNumbers) {
        this(SyntaxThemes.DEFAULT, showLineNumbers);
    }

    public GoSyntaxHighlighter(SyntaxTheme theme, boolean showLineNumbers) {
        this.theme = theme;
        this.showLineNumbers = showLineNumbers;
    }

    @Override
    public String highlight(String s) {
        if (s == null || s.isBlank()) return "";

        StyleBuilder sb = Clique.styleBuilder();
        GoLexer lexer = new GoLexer(CharStreams.fromString(s));
        lexer.removeErrorListeners();
        BufferedTokenStream tokenStream = new BufferedTokenStream(lexer);
        tokenStream.fill();

        List<Token> tokens = tokenStream.getTokens();
        int size = tokens.size();
        int[] lineNumber = new int[1];

        if (showLineNumbers) {
            sb.append(Utils.formatNoTo3dp(++lineNumber[0]), theme.gutter());
        }

        for (int i = 0; i < size; i++) {
            Token token = tokens.get(i);

            if (token.getType() == Token.EOF) {
                break;
            }

            Token next = lookahead(tokens, i + 1);

            if (showLineNumbers && isMultiLineToken(token)) {
                styleMultiLineToken(token, next, lineNumber, sb);
            }else if (showLineNumbers && isLineEnding(token)) {
                String text = token.getText();
                long newlineCount = text.chars().filter(c -> c == '\n').count();
                for (int j = 0; j < newlineCount; j++) {
                    sb.append("\n");
                    sb.append(Utils.formatNoTo3dp(++lineNumber[0]), theme.gutter());
                }
            } else {
                applyStyle(token, sb);
            }
        }

        return sb.get();
    }

    void applyStyle(Token token, StyleBuilder sb) {
        int type = token.getType();
        String text = token.getText();

        if (token.getChannel() == Token.HIDDEN_CHANNEL
                && type != GoLexer.WS
                && type != GoLexer.WS_NLSEMI
                && !isComment(type)) {
            return;
        }

        if (isWhitespace(type)) {
            sb.append(text);
        } else if (isComment(type)) {
            sb.append(text, theme.comment());
        } else if (isString(type)) {
            sb.append(text, theme.stringLiteral());
        } else if (isNumber(type)) {
            sb.append(text, theme.numberLiteral());
        } else if (isKeyword(type)) {
            sb.append(text, theme.keyword());
        } else {
            sb.append(text);
        }
    }

    // Splits a multiline token on \n, styles each line, and inserts gutter numbers between them.
    void styleMultiLineToken(Token token, Token next, int[] lineNumber, StyleBuilder sb) {
        String[] lines = token.getText().split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                sb.append("\n");
                sb.append(Utils.formatNoTo3dp(++lineNumber[0]), theme.gutter());
            }
            // Wrap the line fragment in a synthetic token so applyStyle can color it correctly
            applyStyle(new FragmentToken(token, lines[i]), sb);
        }
    }

    // Returns the next non-whitespace token from the given index onwards.
    private Token lookahead(List<Token> tokens, int from) {
        for (int i = from; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (!isWhitespace(t.getType())) return t;
        }
        return null;
    }

    // Only raw string literals can span multiple lines in Go.
    boolean isMultiLineToken(Token token) {
        return token.getType() == GoLexer.RAW_STRING_LIT && token.getText().contains("\n");
    }

    // A line ending is any non-string token whose text contains a newline.
    boolean isLineEnding(Token token) {
        return token.getText().contains("\n");
    }

    boolean isKeyword(int type) {
        return type >= KEYWORD_START && type <= KEYWORD_END;
    }

    boolean isString(int type) {
        return type == GoLexer.RAW_STRING_LIT
                || type == GoLexer.INTERPRETED_STRING_LIT
                || type == GoLexer.RUNE_LIT;
    }

    boolean isNumber(int type) {
        return type >= NUM_START && type <= NUM_END;
    }

    boolean isComment(int type) {
        return type == GoLexer.COMMENT
                || type == GoLexer.LINE_COMMENT
                || type == GoLexer.COMMENT_NLSEMI
                || type == GoLexer.LINE_COMMENT_NLSEMI;
    }

    boolean isWhitespace(int type) {
        return type == GoLexer.WS || type == GoLexer.WS_NLSEMI;
    }


        private record FragmentToken(Token origin, String text) implements Token {

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getType() {
            return origin.getType();
        }

        @Override
        public int getChannel() {
            return origin.getChannel();
        }

        @Override
        public int getLine() {
            return origin.getLine();
        }

        @Override
        public int getCharPositionInLine() {
            return origin.getCharPositionInLine();
        }

        @Override
        public int getTokenIndex() {
            return origin.getTokenIndex();
        }

        @Override
        public int getStartIndex() {
            return origin.getStartIndex();
        }

        @Override
        public int getStopIndex() {
            return origin.getStopIndex();
        }

        @Override
        public TokenSource getTokenSource() {
            return origin.getTokenSource();
        }

        @Override
        public CharStream getInputStream() {
            return origin.getInputStream();
        }
        }
}