package com.forthix.forthic.tokenizer;

/**
 * Types of tokens recognized by the Forthic tokenizer
 */
public enum TokenType {
    STRING,         // "hello" or 'hello' or ^hello^ or '''hello'''
    COMMENT,        // # comment text
    START_ARRAY,    // [
    END_ARRAY,      // ]
    START_MODULE,   // {module-name
    END_MODULE,     // }
    START_DEF,      // : word-name
    END_DEF,        // ;
    START_MEMO,     // @: memo-name
    WORD,           // any other token
    DOT_SYMBOL,     // .symbol (3+ chars)
    EOS             // End of stream
}
