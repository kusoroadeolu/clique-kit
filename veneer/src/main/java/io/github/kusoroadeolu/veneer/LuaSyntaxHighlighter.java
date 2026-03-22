package io.github.kusoroadeolu.veneer;

import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.style.StyleBuilder;
import io.github.kusoroadeolu.veneer.theme.SyntaxTheme;
import io.github.kusoroadeolu.veneer.theme.SyntaxThemes;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.List;

import static io.github.kusoroadeolu.veneer.LuaLexer.*;
import io.github.kusoroadeolu.veneer.LuaLexer;

public class LuaSyntaxHighlighter implements SyntaxHighlighter{
    private final SyntaxTheme theme;
    private final boolean showLineNumbers;

    public LuaSyntaxHighlighter(SyntaxTheme theme, boolean showLineNumbers) {
        this.theme = theme;
        this.showLineNumbers = showLineNumbers;
    }

    public LuaSyntaxHighlighter(boolean showLineNumbers){
        this(SyntaxThemes.DEFAULT, showLineNumbers);
    }

    public LuaSyntaxHighlighter(SyntaxTheme theme){
        this(theme, true);
    }

    public LuaSyntaxHighlighter(){
        this(SyntaxThemes.DEFAULT, true);
    }

    @Override
    public String highlight(String s) {
        if (s == null || s.isBlank()) return "";
        StyleBuilder sb = Clique.styleBuilder();
        LuaLexer lexer = new LuaLexer(CharStreams.fromString(s));
        lexer.removeErrorListeners();
        BufferedTokenStream tokenStream = new BufferedTokenStream(lexer);
        tokenStream.fill();
        if (showLineNumbers) applyWithLines(sb, tokenStream);
        else applyWithoutLines(sb, tokenStream);
        return sb.get();
    }


    public void applyWithoutLines(StyleBuilder sb, BufferedTokenStream tokenStream){
        for (Token token : tokenStream.getTokens()){
            applyStyles(token, sb);
        }
    }

    public void applyWithLines(StyleBuilder sb, BufferedTokenStream tokenStream){
        int[] lineCount = new int[]{1};
        sb.append(Utils.formatNoTo3dp(lineCount[0]), theme.gutter());

        for (Token token : tokenStream.getTokens()){
            if (isMultiLineToken(token)){
                List<String> comments = token.getText().lines().toList();
                for (int i = 0; i < comments.size(); ++i) {
                    if (i > 0){
                        sb.append("\n");
                        sb.append(Utils.formatNoTo3dp(++lineCount[0]), theme.gutter());
                    }
                    var c = comments.get(i);
                    sb.append(c, theme.comment());
                }

                continue;
            }

            if (token.getType() == NL){
                applyStyles(token, sb);
                sb.append(Utils.formatNoTo3dp(++lineCount[0]), theme.gutter());
                continue;
            }

            applyStyles(token, sb);
        }
    }


    void applyStyles(Token token, StyleBuilder sb){
        if (isKeyWord(token)){
            sb.append(token.getText(), theme.keyword());
        }else if (isStringLiteral(token)){
            sb.append(token.getText(), theme.stringLiteral());
        } else if (isNumberLiteral(token)) {
            sb.append(token.getText(), theme.numberLiteral());
        }else if (isComment(token)) {
            sb.append(token.getText(), theme.comment());
        } else if (token.getType() == Token.EOF) {

        }else {
                sb.append(token.getText());
            }
    }



    boolean isKeyWord(Token token){
        int type = token.getType();
        return type >= BREAK && type <= FOR
                || type >= IN && type <= LOCAL
                || type == RETURN
                || (type >= NIL && type <= TRUE);
    }

    boolean isNumberLiteral(Token token){
        return token.getType() == INT || token.getType() == HEX || token.getType() == FLOAT || token.getType() == HEX_FLOAT;
    }

    boolean isStringLiteral(Token token){
        return token.getType() == NORMALSTRING || token.getType() == CHARSTRING || token.getType() == LONGSTRING;
    }

    boolean isComment(Token token){
        return token.getType() ==  COMMENT;
    }

    boolean isMultiLineToken(Token token){
        return isComment(token);
    }
}
