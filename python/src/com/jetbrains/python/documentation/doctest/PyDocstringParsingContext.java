package com.jetbrains.python.documentation.doctest;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharArrayUtil;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.parsing.ExpressionParsing;
import com.jetbrains.python.parsing.ParsingContext;
import com.jetbrains.python.parsing.ParsingScope;
import com.jetbrains.python.parsing.StatementParsing;
import com.jetbrains.python.psi.LanguageLevel;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 */
public class PyDocstringParsingContext extends ParsingContext {
  private final StatementParsing stmtParser;
  private final ExpressionParsing exprParser;

  public PyDocstringParsingContext(final PsiBuilder builder,
                                   LanguageLevel languageLevel,
                                   StatementParsing.FUTURE futureFlag) {
    super(builder, languageLevel, futureFlag);
    stmtParser = new PyDocstringStatementParsing(this, futureFlag);
    exprParser = new PyDocstringExpressionParsing(this);
  }

  @Override
  public ExpressionParsing getExpressionParser() {
    return exprParser;
  }

  @Override
  public StatementParsing getStatementParser() {
    return stmtParser;
  }

  private static class PyDocstringExpressionParsing extends ExpressionParsing {
    public PyDocstringExpressionParsing(ParsingContext context) {
      super(context);
    }

    @Override
    protected IElementType getReferenceType() {
      return PyDocstringTokenTypes.DOC_REFERENCE;
    }
  }

  private static class PyDocstringStatementParsing extends StatementParsing {

    protected PyDocstringStatementParsing(ParsingContext context,
                                          @Nullable FUTURE futureFlag) {
      super(context, futureFlag);
    }
    @Override
    public void parseStatement(ParsingScope scope) {
      IElementType type = myBuilder.getTokenType();
      if (type == PyTokenTypes.INDENT) {
        myBuilder.advanceLexer();
          type = myBuilder.getTokenType();
      }
      if (type == PyDocstringTokenTypes.WELCOME) { // >>> case
        myBuilder.advanceLexer();
      }
      super.parseStatement(scope);
    }

    @Override
    public IElementType filter(IElementType source, int start, int end, CharSequence text) {
      if (source == PyTokenTypes.DOT && CharArrayUtil.regionMatches(text, start, end, "..."))
        return PyDocstringTokenTypes.DOTS;
      if (source == PyTokenTypes.GTGT && CharArrayUtil.regionMatches(text, start, end, ">>>"))
        return PyDocstringTokenTypes.WELCOME;
      return super.filter(source, start, end, text);
    }
  }
}
