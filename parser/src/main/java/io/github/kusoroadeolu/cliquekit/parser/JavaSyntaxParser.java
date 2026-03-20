package io.github.kusoroadeolu.cliquekit.parser;

import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import io.github.kusoroadeolu.clique.Clique;
import io.github.kusoroadeolu.clique.ansi.StyleCode;
import io.github.kusoroadeolu.clique.core.utils.Constants;
import io.github.kusoroadeolu.clique.parser.AnsiStringParser;
import io.github.kusoroadeolu.clique.spi.AnsiCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.github.kusoroadeolu.clique.parser.AnsiStringParser.DEFAULT;

/**
 * A syntax highlighting parser for Java source code backed by JavaParser.
 *
 * Uses JavaParser's token stream rather than the AST visitor — this gives us
 * the raw token kinds and their exact text positions, which maps directly to
 * what we need for highlighting without needing to resolve symbols.
 *
 * Dependency (add to your pom.xml / build.gradle):
 *
 *   Maven:
 *     <dependency>
 *       <groupId>com.github.javaparser</groupId>
 *       <artifactId>javaparser-core</artifactId>
 *       <version>3.28.0</version>
 *     </dependency>
 *
 *   Gradle:
 *     implementation 'com.github.javaparser:javaparser-core:3.28.0'
 *
 * Usage:
 *
 *   AnsiStringParser parser = new JavaSyntaxParser();
 *
 *   // Standalone
 *   parser.print("public class Main { }");
 *
 *   // Plugged into a Frame
 *   Component highlighted = () -> parser.parse(code);
 *   Clique.frame()
 *       .title("[bold]Source[/]")
 *       .nest(highlighted)
 *       .render();
 */
public class JavaSyntaxParser implements AnsiStringParser {

    // -------------------------------------------------------------------------
    // Hardcoded truecolor ANSI codes
    // -------------------------------------------------------------------------

    private static final String KEYWORD    = Clique.rgb(189, 147, 249).toString(); // soft purple
    private static final String STRING     = "\u001B[38;2;80;200;120m";  // soft green
    private static final String COMMENT    = "\u001B[38;2;98;114;164m";  // muted grey
    private static final String NUMBER     = "\u001B[38;2;139;233;253m"; // cyan
    private static final String ANNOTATION = "\u001B[38;2;241;250;140m"; // yellow
    private static final String TYPE       = "\u001B[38;2;255;184;108m"; // orange
    private static final String RESET      = StyleCode.RESET.toString();

    // -------------------------------------------------------------------------
    // JavaParser token kind groups
    // -------------------------------------------------------------------------

    private static final Set<Integer> KEYWORD_KINDS = Set.of(
            JavaToken.Kind.ABSTRACT.ordinal(),
            JavaToken.Kind.ASSERT.ordinal(),
            JavaToken.Kind.BOOLEAN.ordinal(),
            JavaToken.Kind.BREAK.ordinal(),
            JavaToken.Kind.BYTE.ordinal(),
            JavaToken.Kind.CASE.ordinal(),
            JavaToken.Kind.CATCH.ordinal(),
            JavaToken.Kind.CHAR.ordinal(),
            JavaToken.Kind.CLASS.ordinal(),
            JavaToken.Kind.CONST.ordinal(),
            JavaToken.Kind.CONTINUE.ordinal(),
            JavaToken.Kind.DO.ordinal(),
            JavaToken.Kind.DOUBLE.ordinal(),
            JavaToken.Kind.ELSE.ordinal(),
            JavaToken.Kind.ENUM.ordinal(),
            JavaToken.Kind.EXTENDS.ordinal(),
            JavaToken.Kind.FALSE.ordinal(),
            JavaToken.Kind.FINAL.ordinal(),
            JavaToken.Kind.FINALLY.ordinal(),
            JavaToken.Kind.FLOAT.ordinal(),
            JavaToken.Kind.FOR.ordinal(),
            JavaToken.Kind.GOTO.ordinal(),
            JavaToken.Kind.IF.ordinal(),
            JavaToken.Kind.IMPLEMENTS.ordinal(),
            JavaToken.Kind.IMPORT.ordinal(),
            JavaToken.Kind.INSTANCEOF.ordinal(),
            JavaToken.Kind.INT.ordinal(),
            JavaToken.Kind.INTERFACE.ordinal(),
            JavaToken.Kind.LONG.ordinal(),
            JavaToken.Kind.NATIVE.ordinal(),
            JavaToken.Kind.NEW.ordinal(),
            JavaToken.Kind.NULL.ordinal(),
            JavaToken.Kind.PACKAGE.ordinal(),
            JavaToken.Kind.PRIVATE.ordinal(),
            JavaToken.Kind.PROTECTED.ordinal(),
            JavaToken.Kind.PUBLIC.ordinal(),
            JavaToken.Kind.RECORD.ordinal(),
            JavaToken.Kind.RETURN.ordinal(),
            JavaToken.Kind.SHORT.ordinal(),
            JavaToken.Kind.STATIC.ordinal(),
            JavaToken.Kind.STRICTFP.ordinal(),
            JavaToken.Kind.SUPER.ordinal(),
            JavaToken.Kind.SWITCH.ordinal(),
            JavaToken.Kind.SYNCHRONIZED.ordinal(),
            JavaToken.Kind.THIS.ordinal(),
            JavaToken.Kind.THROW.ordinal(),
            JavaToken.Kind.THROWS.ordinal(),
            JavaToken.Kind.TRANSIENT.ordinal(),
            JavaToken.Kind.TRUE.ordinal(),
            JavaToken.Kind.TRY.ordinal(),
            JavaToken.Kind.VOID.ordinal(),
            JavaToken.Kind.VOLATILE.ordinal(),
            JavaToken.Kind.WHILE.ordinal(),
            JavaToken.Kind.YIELD.ordinal(),
            JavaToken.Kind.SEALED.ordinal(),
            JavaToken.Kind.PERMITS.ordinal(),
            JavaToken.Kind.ABSTRACT.ordinal()
    );

    private static final Set<Integer> STRING_KINDS = Set.of(
            JavaToken.Kind.STRING_LITERAL.ordinal(),
            JavaToken.Kind.CHARACTER_LITERAL.ordinal(),
            JavaToken.Kind.TEXT_BLOCK_LITERAL.ordinal()
    );

    private static final Set<Integer> COMMENT_KINDS = Set.of(
            JavaToken.Kind.SINGLE_LINE_COMMENT.ordinal(),
            JavaToken.Kind.MULTI_LINE_COMMENT.ordinal(),
            JavaToken.Kind.JAVADOC_COMMENT.ordinal()
    );

    private static final Set<Integer> NUMBER_KINDS = Set.of(
            JavaToken.Kind.INTEGER_LITERAL.ordinal(),
            JavaToken.Kind.LONG_LITERAL.ordinal(),
            JavaToken.Kind.FLOATING_POINT_LITERAL.ordinal(),
            JavaToken.Kind.HEX_LITERAL.ordinal(),
            JavaToken.Kind.OCTAL_LITERAL.ordinal(),
            JavaToken.Kind.BINARY_LITERAL.ordinal(),
           JavaToken.Kind.HEXADECIMAL_FLOATING_POINT_LITERAL.getKind()
    );

    // -------------------------------------------------------------------------
    // AnsiStringParser impl
    // -------------------------------------------------------------------------

    @Override
    public String parse(String input) {
        if (input == null || input.isBlank()) return input == null ? "" : input;

        // Wrap in a class shell if the input looks like a snippet rather than
        // a full compilation unit — JavaParser needs valid Java to tokenise
        String source = isFullCompilationUnit(input) ? input : wrapSnippet(input);

        try {
            ParserConfiguration configuration = new ParserConfiguration();
            configuration.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
            JavaParser parser = new JavaParser();
            var cu = parser.parse(source).getResult();
            if (cu.isEmpty()) return input;
            Optional<TokenRange> tokenRange = cu.get().getTokenRange();
            if (tokenRange.isEmpty()) return input;

            StringBuilder sb = new StringBuilder();
            for (JavaToken token : tokenRange.get()) {
                sb.append(styleToken(token));
            }

            String result = sb.toString().stripTrailing();
            return isFullCompilationUnit(input) ? result : unwrapSnippet(result);
        } catch (Exception e) {
            return input;
        }
    }

    @Override
    public String parse(Object object) {
        return parse(object.toString());
    }

    @Override
    public String getOriginalString(String string) {
        return DEFAULT.getOriginalString(string);
    }

    @Override
    public List<AnsiCode> ansiCodes(String string) {
        return List.of();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Maps a JavaParser token to its ANSI-styled string.
     * Whitespace and punctuation pass through unstyled.
     */
    private String styleToken(JavaToken token) {
        String text = token.getText();
        int kind = token.getKind();

        if (KEYWORD_KINDS.contains(kind))               return KEYWORD    + text + RESET;
        if (STRING_KINDS.contains(kind))                return STRING     + text + RESET;
        if (COMMENT_KINDS.contains(kind))               return COMMENT    + text + RESET;
        if (NUMBER_KINDS.contains(kind))                return NUMBER     + text + RESET;
        if (kind == JavaToken.Kind.AT.ordinal())        return ANNOTATION + text + RESET;
        if (isTypeName(token))                          return TYPE       + text + RESET;

        return text; // punctuation, operators, whitespace — pass through
    }

    /**
     * Heuristic to detect type names — identifiers starting with uppercase
     * that follow class/interface/enum/record/new/extends/implements/@.
     */
    private boolean isTypeName(JavaToken token) {
        if (token.getKind() != JavaToken.Kind.IDENTIFIER.ordinal()) return false;
        String text = token.getText();
        if (text.isEmpty() || !Character.isUpperCase(text.charAt(0))) return false;

        Optional<JavaToken> prev = previousMeaningfulToken(token);
        if (prev.isEmpty()) return false;

        int prevKind = prev.get().getKind();
        return prevKind == JavaToken.Kind.CLASS.ordinal()
                || prevKind == JavaToken.Kind.INTERFACE.ordinal()
                || prevKind == JavaToken.Kind.ENUM.ordinal()
                || prevKind == JavaToken.Kind.RECORD.ordinal()
                || prevKind == JavaToken.Kind.NEW.ordinal()
                || prevKind == JavaToken.Kind.EXTENDS.ordinal()
                || prevKind == JavaToken.Kind.IMPLEMENTS.ordinal()
                || prevKind == JavaToken.Kind.AT.ordinal();
    }

    /** Walks back through the token stream skipping whitespace tokens. */
    private Optional<JavaToken> previousMeaningfulToken(JavaToken token) {
        Optional<JavaToken> prev = token.getPreviousToken();
        while (prev.isPresent()) {
            JavaToken t = prev.get();
            int kind = t.getKind();
            if (kind != JavaToken.Kind.SPACE.ordinal()
                    && kind != JavaToken.Kind.WINDOWS_EOL.ordinal()
                    && kind != JavaToken.Kind.OLD_MAC_EOL.ordinal()) {
                return prev;
            }
            prev = t.getPreviousToken();
        }
        return Optional.empty();
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

    /**
     * Quick heuristic — if the input contains a top-level declaration
     * we treat it as a full compilation unit and skip the wrapper.
     */
    private boolean isFullCompilationUnit(String input) {
        String trimmed = input.stripLeading();
        return trimmed.startsWith("package ")
                || trimmed.startsWith("import ")
                || trimmed.contains("class ")
                || trimmed.contains("interface ")
                || trimmed.contains("enum ")
                || trimmed.contains("record ");
    }
}