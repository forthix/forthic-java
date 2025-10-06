package com.forthix.forthic.tokenizer;

import com.forthix.forthic.errors.CodeLocation;
import com.forthix.forthic.errors.InvalidWordNameError;
import com.forthix.forthic.errors.UnterminatedStringError;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Tokenizer class
 */
public class TokenizerTest {

    @Test
    public void testKnowsTokenPositions() {
        String mainForthic = "\n"
            + "    : ADD-ONE   1 23 +;\n"
            + "    {module\n"
            + "        # 2 ADD-ONE\n"
            + "    }\n"
            + "    @: MY-MEMO   [ \"hello\" '''triple-single-quoted-string'''];\n"
            + "    ";

        CodeLocation referenceLocation = CodeLocation.builder()
            .screenName("main")
            .line(1)
            .column(1)
            .startPos(0)
            .build();

        Tokenizer tokenizer = new Tokenizer(mainForthic, referenceLocation);

        // TOK_START_DEF
        Token beginDef = tokenizer.nextToken();
        assertEquals(TokenType.START_DEF, beginDef.getType());
        assertEquals("ADD-ONE", beginDef.getString());
        CodeLocation expectedLoc = CodeLocation.builder()
            .screenName("main")
            .line(2)
            .column(7)
            .startPos(7)
            .endPos(14)
            .build();
        assertEquals(expectedLoc, beginDef.getLocation());

        // TOK_WORD: 1
        Token oneToken = tokenizer.nextToken();
        assertEquals(TokenType.WORD, oneToken.getType());
        assertEquals("1", oneToken.getString());
        expectedLoc = CodeLocation.builder()
            .screenName("main")
            .line(2)
            .column(17)
            .startPos(17)
            .endPos(18)
            .build();
        assertEquals(expectedLoc, oneToken.getLocation());

        // TOK_WORD: 23
        Token token23 = tokenizer.nextToken();
        assertEquals(TokenType.WORD, token23.getType());
        assertEquals("23", token23.getString());
        expectedLoc = CodeLocation.builder()
            .screenName("main")
            .line(2)
            .column(19)
            .startPos(19)
            .endPos(21)
            .build();
        assertEquals(expectedLoc, token23.getLocation());
    }

    @Test
    public void testComment() {
        Tokenizer tokenizer = new Tokenizer("# This is a comment");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.COMMENT, token.getType());
        assertEquals(" This is a comment", token.getString());
    }

    @Test
    public void testSingleQuoteString() {
        Tokenizer tokenizer = new Tokenizer("'hello'");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("hello", token.getString());
    }

    @Test
    public void testDoubleQuoteString() {
        Tokenizer tokenizer = new Tokenizer("\"world\"");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("world", token.getString());
    }

    @Test
    public void testCaretQuoteString() {
        Tokenizer tokenizer = new Tokenizer("^test^");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("test", token.getString());
    }

    @Test
    public void testTripleQuoteString() {
        Tokenizer tokenizer = new Tokenizer("'''multi\nline\nstring'''");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("multi\nline\nstring", token.getString());
    }

    @Test
    public void testTripleQuoteGreedyMode() {
        // """"hello"""" should produce "hello"
        Tokenizer tokenizer = new Tokenizer("\"\"\"\"hello\"\"\"\"");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("\"hello\"", token.getString());
    }

    @Test
    public void testUnterminatedStringThrowsError() {
        Tokenizer tokenizer = new Tokenizer("'unterminated");
        assertThrows(UnterminatedStringError.class, () -> tokenizer.nextToken());
    }

    @Test
    public void testStartArray() {
        Tokenizer tokenizer = new Tokenizer("[");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.START_ARRAY, token.getType());
        assertEquals("[", token.getString());
    }

    @Test
    public void testEndArray() {
        Tokenizer tokenizer = new Tokenizer("]");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.END_ARRAY, token.getType());
        assertEquals("]", token.getString());
    }

    @Test
    public void testStartModule() {
        Tokenizer tokenizer = new Tokenizer("{module-name");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.START_MODULE, token.getType());
        assertEquals("module-name", token.getString());
    }

    @Test
    public void testEndModule() {
        Tokenizer tokenizer = new Tokenizer("}");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.END_MODULE, token.getType());
        assertEquals("}", token.getString());
    }

    @Test
    public void testStartDefinition() {
        Tokenizer tokenizer = new Tokenizer(": WORD-NAME");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.START_DEF, token.getType());
        assertEquals("WORD-NAME", token.getString());
    }

    @Test
    public void testEndDefinition() {
        Tokenizer tokenizer = new Tokenizer(";");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.END_DEF, token.getType());
        assertEquals(";", token.getString());
    }

    @Test
    public void testStartMemo() {
        Tokenizer tokenizer = new Tokenizer("@: MEMO-NAME");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.START_MEMO, token.getType());
        assertEquals("MEMO-NAME", token.getString());
    }

    @Test
    public void testWord() {
        Tokenizer tokenizer = new Tokenizer("some-word");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.WORD, token.getType());
        assertEquals("some-word", token.getString());
    }

    @Test
    public void testDotSymbol() {
        Tokenizer tokenizer = new Tokenizer(".symbol");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.DOT_SYMBOL, token.getType());
        assertEquals("symbol", token.getString());
    }

    @Test
    public void testDotSymbolTooShortBecomesWord() {
        // .x is only 2 chars total, should be a word
        Tokenizer tokenizer = new Tokenizer(".x");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.WORD, token.getType());
        assertEquals(".x", token.getString());
    }

    @Test
    public void testEndOfStream() {
        Tokenizer tokenizer = new Tokenizer("");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.EOS, token.getType());
    }

    @Test
    public void testWhitespaceHandling() {
        Tokenizer tokenizer = new Tokenizer("  word1   word2\n\tword3  ");
        assertEquals("word1", tokenizer.nextToken().getString());
        assertEquals("word2", tokenizer.nextToken().getString());
        assertEquals("word3", tokenizer.nextToken().getString());
        assertEquals(TokenType.EOS, tokenizer.nextToken().getType());
    }

    @Test
    public void testParensAsWhitespace() {
        Tokenizer tokenizer = new Tokenizer("(word1)(word2)");
        assertEquals("word1", tokenizer.nextToken().getString());
        assertEquals("word2", tokenizer.nextToken().getString());
    }

    @Test
    public void testCommasAsWhitespace() {
        Tokenizer tokenizer = new Tokenizer("word1,word2,word3");
        assertEquals("word1", tokenizer.nextToken().getString());
        assertEquals("word2", tokenizer.nextToken().getString());
        assertEquals("word3", tokenizer.nextToken().getString());
    }

    @Test
    public void testInvalidDefinitionNameWithQuotes() {
        Tokenizer tokenizer = new Tokenizer(": \"bad-name\"");
        assertThrows(InvalidWordNameError.class, () -> {
            tokenizer.nextToken();
        });
    }

    @Test
    public void testInvalidDefinitionNameWithBrackets() {
        Tokenizer tokenizer = new Tokenizer(": bad[name");
        assertThrows(InvalidWordNameError.class, () -> {
            tokenizer.nextToken(); // Error happens during token gathering
        });
    }

    @Test
    public void testHtmlEntityUnescaping() {
        Tokenizer tokenizer = new Tokenizer("'&lt;div&gt;'");
        Token token = tokenizer.nextToken();
        assertEquals(TokenType.STRING, token.getType());
        assertEquals("<div>", token.getString());
    }

    @Test
    public void testComplexTokenSequence() {
        String forthic = ": DOUBLE DUP +; [1 2 3] DOUBLE";
        Tokenizer tokenizer = new Tokenizer(forthic);

        Token defToken = tokenizer.nextToken();
        assertEquals(TokenType.START_DEF, defToken.getType());
        assertEquals("DOUBLE", defToken.getString());
        assertEquals("DUP", tokenizer.nextToken().getString());
        assertEquals("+", tokenizer.nextToken().getString());
        assertEquals(TokenType.END_DEF, tokenizer.nextToken().getType());
        assertEquals(TokenType.START_ARRAY, tokenizer.nextToken().getType());
        assertEquals("1", tokenizer.nextToken().getString());
        assertEquals("2", tokenizer.nextToken().getString());
        assertEquals("3", tokenizer.nextToken().getString());
        assertEquals(TokenType.END_ARRAY, tokenizer.nextToken().getType());
        assertEquals("DOUBLE", tokenizer.nextToken().getString());
        assertEquals(TokenType.EOS, tokenizer.nextToken().getType());
    }

    @Test
    public void testMultilineCode() {
        String forthic = ": ADD-TWO\n"
            + "    2 +\n"
            + ";\n"
            + "\n"
            + "5 ADD-TWO\n";
        Tokenizer tokenizer = new Tokenizer(forthic);

        Token defToken = tokenizer.nextToken();
        assertEquals(TokenType.START_DEF, defToken.getType());
        assertEquals("ADD-TWO", defToken.getString());
        assertEquals("2", tokenizer.nextToken().getString());
        assertEquals("+", tokenizer.nextToken().getString());
        assertEquals(TokenType.END_DEF, tokenizer.nextToken().getType());
        assertEquals("5", tokenizer.nextToken().getString());
        assertEquals("ADD-TWO", tokenizer.nextToken().getString());
    }

    @Test
    public void testEmptyModule() {
        Tokenizer tokenizer = new Tokenizer("{}");
        Token moduleToken = tokenizer.nextToken();
        assertEquals(TokenType.START_MODULE, moduleToken.getType());
        assertEquals("", moduleToken.getString()); // empty module name
        assertEquals(TokenType.END_MODULE, tokenizer.nextToken().getType());
    }

    @Test
    public void testNestedStructures() {
        String forthic = "{module [ 'string' ] }";
        Tokenizer tokenizer = new Tokenizer(forthic);

        Token moduleToken = tokenizer.nextToken();
        assertEquals(TokenType.START_MODULE, moduleToken.getType());
        assertEquals("module", moduleToken.getString());
        assertEquals(TokenType.START_ARRAY, tokenizer.nextToken().getType());
        Token stringToken = tokenizer.nextToken();
        assertEquals(TokenType.STRING, stringToken.getType());
        assertEquals("string", stringToken.getString());
        assertEquals(TokenType.END_ARRAY, tokenizer.nextToken().getType());
        assertEquals(TokenType.END_MODULE, tokenizer.nextToken().getType());
    }
}
