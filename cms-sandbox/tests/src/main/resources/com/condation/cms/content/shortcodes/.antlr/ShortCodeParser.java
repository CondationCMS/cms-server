// Generated from c:/entwicklung/workspaces/tma/cms/cms-server/cms-sandbox/tests/src/main/resources/com/condation/cms/content/shortcodes/ShortCodeParser.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ShortCodeParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TAG_OPENING_BRACKET=1, EQUALS=2, SPACE=3, SINGLE_OPEN_BRAKET=4, DASH=5, 
		TAG_CLOSING_BRACKET=6, TAG_CLOSING_CLOSING_BRACKET=7, TAG_OPENING_CLOSING_BRACKET=8, 
		TAG_NAME=9, TAG_STRING=10, TAG_NUMBER=11, TAG_WS=12;
	public static final int
		RULE_shortcodes = 0, RULE_shortcode = 1, RULE_openingTag = 2, RULE_closingTag = 3, 
		RULE_selfClosingTag = 4, RULE_params = 5, RULE_param = 6, RULE_value = 7, 
		RULE_content = 8, RULE_text = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"shortcodes", "shortcode", "openingTag", "closingTag", "selfClosingTag", 
			"params", "param", "value", "content", "text"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'[['", "'='", "' '", "'['", "'-'", "']]'", "'/]]'", "'[[/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "TAG_OPENING_BRACKET", "EQUALS", "SPACE", "SINGLE_OPEN_BRAKET", 
			"DASH", "TAG_CLOSING_BRACKET", "TAG_CLOSING_CLOSING_BRACKET", "TAG_OPENING_CLOSING_BRACKET", 
			"TAG_NAME", "TAG_STRING", "TAG_NUMBER", "TAG_WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ShortCodeParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ShortCodeParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ShortcodesContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ShortCodeParser.EOF, 0); }
		public List<ShortcodeContext> shortcode() {
			return getRuleContexts(ShortcodeContext.class);
		}
		public ShortcodeContext shortcode(int i) {
			return getRuleContext(ShortcodeContext.class,i);
		}
		public List<TextContext> text() {
			return getRuleContexts(TextContext.class);
		}
		public TextContext text(int i) {
			return getRuleContext(TextContext.class,i);
		}
		public ShortcodesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shortcodes; }
	}

	public final ShortcodesContext shortcodes() throws RecognitionException {
		ShortcodesContext _localctx = new ShortcodesContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_shortcodes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(22); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(22);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(20);
					shortcode();
					}
					break;
				case 2:
					{
					setState(21);
					text();
					}
					break;
				}
				}
				setState(24); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 8174L) != 0) );
			setState(26);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ShortcodeContext extends ParserRuleContext {
		public ShortcodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shortcode; }
	 
		public ShortcodeContext() { }
		public void copyFrom(ShortcodeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ShortcodeWithContentContext extends ShortcodeContext {
		public OpeningTagContext openingTag() {
			return getRuleContext(OpeningTagContext.class,0);
		}
		public ClosingTagContext closingTag() {
			return getRuleContext(ClosingTagContext.class,0);
		}
		public ContentContext content() {
			return getRuleContext(ContentContext.class,0);
		}
		public ShortcodeWithContentContext(ShortcodeContext ctx) { copyFrom(ctx); }
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SelfClosingShortcodeContext extends ShortcodeContext {
		public SelfClosingTagContext selfClosingTag() {
			return getRuleContext(SelfClosingTagContext.class,0);
		}
		public SelfClosingShortcodeContext(ShortcodeContext ctx) { copyFrom(ctx); }
	}

	public final ShortcodeContext shortcode() throws RecognitionException {
		ShortcodeContext _localctx = new ShortcodeContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_shortcode);
		try {
			setState(35);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new ShortcodeWithContentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				openingTag();
				setState(30);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
				case 1:
					{
					setState(29);
					content();
					}
					break;
				}
				setState(32);
				closingTag();
				}
				break;
			case 2:
				_localctx = new SelfClosingShortcodeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(34);
				selfClosingTag();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OpeningTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPENING_BRACKET() { return getToken(ShortCodeParser.TAG_OPENING_BRACKET, 0); }
		public TerminalNode TAG_NAME() { return getToken(ShortCodeParser.TAG_NAME, 0); }
		public TerminalNode TAG_CLOSING_BRACKET() { return getToken(ShortCodeParser.TAG_CLOSING_BRACKET, 0); }
		public TerminalNode TAG_WS() { return getToken(ShortCodeParser.TAG_WS, 0); }
		public ParamsContext params() {
			return getRuleContext(ParamsContext.class,0);
		}
		public OpeningTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_openingTag; }
	}

	public final OpeningTagContext openingTag() throws RecognitionException {
		OpeningTagContext _localctx = new OpeningTagContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_openingTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37);
			match(TAG_OPENING_BRACKET);
			setState(38);
			match(TAG_NAME);
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_WS) {
				{
				setState(39);
				match(TAG_WS);
				}
			}

			setState(43);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_NAME) {
				{
				setState(42);
				params();
				}
			}

			setState(45);
			match(TAG_CLOSING_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ClosingTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPENING_CLOSING_BRACKET() { return getToken(ShortCodeParser.TAG_OPENING_CLOSING_BRACKET, 0); }
		public TerminalNode TAG_NAME() { return getToken(ShortCodeParser.TAG_NAME, 0); }
		public TerminalNode TAG_CLOSING_BRACKET() { return getToken(ShortCodeParser.TAG_CLOSING_BRACKET, 0); }
		public ClosingTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closingTag; }
	}

	public final ClosingTagContext closingTag() throws RecognitionException {
		ClosingTagContext _localctx = new ClosingTagContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_closingTag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			match(TAG_OPENING_CLOSING_BRACKET);
			setState(48);
			match(TAG_NAME);
			setState(49);
			match(TAG_CLOSING_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelfClosingTagContext extends ParserRuleContext {
		public TerminalNode TAG_OPENING_BRACKET() { return getToken(ShortCodeParser.TAG_OPENING_BRACKET, 0); }
		public TerminalNode TAG_NAME() { return getToken(ShortCodeParser.TAG_NAME, 0); }
		public TerminalNode TAG_CLOSING_CLOSING_BRACKET() { return getToken(ShortCodeParser.TAG_CLOSING_CLOSING_BRACKET, 0); }
		public List<TerminalNode> TAG_WS() { return getTokens(ShortCodeParser.TAG_WS); }
		public TerminalNode TAG_WS(int i) {
			return getToken(ShortCodeParser.TAG_WS, i);
		}
		public ParamsContext params() {
			return getRuleContext(ParamsContext.class,0);
		}
		public SelfClosingTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selfClosingTag; }
	}

	public final SelfClosingTagContext selfClosingTag() throws RecognitionException {
		SelfClosingTagContext _localctx = new SelfClosingTagContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_selfClosingTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			match(TAG_OPENING_BRACKET);
			setState(52);
			match(TAG_NAME);
			setState(54);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(53);
				match(TAG_WS);
				}
				break;
			}
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_NAME) {
				{
				setState(56);
				params();
				}
			}

			setState(60);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TAG_WS) {
				{
				setState(59);
				match(TAG_WS);
				}
			}

			setState(62);
			match(TAG_CLOSING_CLOSING_BRACKET);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamsContext extends ParserRuleContext {
		public List<ParamContext> param() {
			return getRuleContexts(ParamContext.class);
		}
		public ParamContext param(int i) {
			return getRuleContext(ParamContext.class,i);
		}
		public List<TerminalNode> TAG_WS() { return getTokens(ShortCodeParser.TAG_WS); }
		public TerminalNode TAG_WS(int i) {
			return getToken(ShortCodeParser.TAG_WS, i);
		}
		public ParamsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_params; }
	}

	public final ParamsContext params() throws RecognitionException {
		ParamsContext _localctx = new ParamsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_params);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			param();
			setState(73);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(66); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(65);
						match(TAG_WS);
						}
						}
						setState(68); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==TAG_WS );
					setState(70);
					param();
					}
					} 
				}
				setState(75);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParamContext extends ParserRuleContext {
		public TerminalNode TAG_NAME() { return getToken(ShortCodeParser.TAG_NAME, 0); }
		public TerminalNode EQUALS() { return getToken(ShortCodeParser.EQUALS, 0); }
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public ParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_param; }
	}

	public final ParamContext param() throws RecognitionException {
		ParamContext _localctx = new ParamContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_param);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(TAG_NAME);
			setState(77);
			match(EQUALS);
			setState(78);
			value();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValueContext extends ParserRuleContext {
		public TerminalNode TAG_STRING() { return getToken(ShortCodeParser.TAG_STRING, 0); }
		public TerminalNode TAG_NUMBER() { return getToken(ShortCodeParser.TAG_NUMBER, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			_la = _input.LA(1);
			if ( !(_la==TAG_STRING || _la==TAG_NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ContentContext extends ParserRuleContext {
		public List<TerminalNode> SPACE() { return getTokens(ShortCodeParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(ShortCodeParser.SPACE, i);
		}
		public List<TerminalNode> DASH() { return getTokens(ShortCodeParser.DASH); }
		public TerminalNode DASH(int i) {
			return getToken(ShortCodeParser.DASH, i);
		}
		public List<TerminalNode> SINGLE_OPEN_BRAKET() { return getTokens(ShortCodeParser.SINGLE_OPEN_BRAKET); }
		public TerminalNode SINGLE_OPEN_BRAKET(int i) {
			return getToken(ShortCodeParser.SINGLE_OPEN_BRAKET, i);
		}
		public ContentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_content; }
	}

	public final ContentContext content() throws RecognitionException {
		ContentContext _localctx = new ContentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_content);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(85); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(85);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
					case 1:
						{
						setState(82);
						_la = _input.LA(1);
						if ( _la <= 0 || (_la==SINGLE_OPEN_BRAKET) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 2:
						{
						setState(83);
						match(SPACE);
						}
						break;
					case 3:
						{
						setState(84);
						match(DASH);
						}
						break;
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(87); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TextContext extends ParserRuleContext {
		public List<TerminalNode> SPACE() { return getTokens(ShortCodeParser.SPACE); }
		public TerminalNode SPACE(int i) {
			return getToken(ShortCodeParser.SPACE, i);
		}
		public List<TerminalNode> SINGLE_OPEN_BRAKET() { return getTokens(ShortCodeParser.SINGLE_OPEN_BRAKET); }
		public TerminalNode SINGLE_OPEN_BRAKET(int i) {
			return getToken(ShortCodeParser.SINGLE_OPEN_BRAKET, i);
		}
		public TextContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_text; }
	}

	public final TextContext text() throws RecognitionException {
		TextContext _localctx = new TextContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_text);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(91); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					setState(91);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
					case 1:
						{
						setState(89);
						_la = _input.LA(1);
						if ( _la <= 0 || (_la==SINGLE_OPEN_BRAKET) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 2:
						{
						setState(90);
						match(SPACE);
						}
						break;
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(93); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\f`\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0001\u0000\u0001\u0000\u0004\u0000\u0017\b"+
		"\u0000\u000b\u0000\f\u0000\u0018\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0003\u0001\u001f\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003"+
		"\u0001$\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002)\b\u0002"+
		"\u0001\u0002\u0003\u0002,\b\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0003\u00047\b\u0004\u0001\u0004\u0003\u0004:\b\u0004\u0001\u0004\u0003"+
		"\u0004=\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0004"+
		"\u0005C\b\u0005\u000b\u0005\f\u0005D\u0001\u0005\u0005\u0005H\b\u0005"+
		"\n\u0005\f\u0005K\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0004\bV\b\b\u000b\b"+
		"\f\bW\u0001\t\u0001\t\u0004\t\\\b\t\u000b\t\f\t]\u0001\t\u0000\u0000\n"+
		"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0000\u0002\u0001\u0000"+
		"\n\u000b\u0001\u0000\u0004\u0004e\u0000\u0016\u0001\u0000\u0000\u0000"+
		"\u0002#\u0001\u0000\u0000\u0000\u0004%\u0001\u0000\u0000\u0000\u0006/"+
		"\u0001\u0000\u0000\u0000\b3\u0001\u0000\u0000\u0000\n@\u0001\u0000\u0000"+
		"\u0000\fL\u0001\u0000\u0000\u0000\u000eP\u0001\u0000\u0000\u0000\u0010"+
		"U\u0001\u0000\u0000\u0000\u0012[\u0001\u0000\u0000\u0000\u0014\u0017\u0003"+
		"\u0002\u0001\u0000\u0015\u0017\u0003\u0012\t\u0000\u0016\u0014\u0001\u0000"+
		"\u0000\u0000\u0016\u0015\u0001\u0000\u0000\u0000\u0017\u0018\u0001\u0000"+
		"\u0000\u0000\u0018\u0016\u0001\u0000\u0000\u0000\u0018\u0019\u0001\u0000"+
		"\u0000\u0000\u0019\u001a\u0001\u0000\u0000\u0000\u001a\u001b\u0005\u0000"+
		"\u0000\u0001\u001b\u0001\u0001\u0000\u0000\u0000\u001c\u001e\u0003\u0004"+
		"\u0002\u0000\u001d\u001f\u0003\u0010\b\u0000\u001e\u001d\u0001\u0000\u0000"+
		"\u0000\u001e\u001f\u0001\u0000\u0000\u0000\u001f \u0001\u0000\u0000\u0000"+
		" !\u0003\u0006\u0003\u0000!$\u0001\u0000\u0000\u0000\"$\u0003\b\u0004"+
		"\u0000#\u001c\u0001\u0000\u0000\u0000#\"\u0001\u0000\u0000\u0000$\u0003"+
		"\u0001\u0000\u0000\u0000%&\u0005\u0001\u0000\u0000&(\u0005\t\u0000\u0000"+
		"\')\u0005\f\u0000\u0000(\'\u0001\u0000\u0000\u0000()\u0001\u0000\u0000"+
		"\u0000)+\u0001\u0000\u0000\u0000*,\u0003\n\u0005\u0000+*\u0001\u0000\u0000"+
		"\u0000+,\u0001\u0000\u0000\u0000,-\u0001\u0000\u0000\u0000-.\u0005\u0006"+
		"\u0000\u0000.\u0005\u0001\u0000\u0000\u0000/0\u0005\b\u0000\u000001\u0005"+
		"\t\u0000\u000012\u0005\u0006\u0000\u00002\u0007\u0001\u0000\u0000\u0000"+
		"34\u0005\u0001\u0000\u000046\u0005\t\u0000\u000057\u0005\f\u0000\u0000"+
		"65\u0001\u0000\u0000\u000067\u0001\u0000\u0000\u000079\u0001\u0000\u0000"+
		"\u00008:\u0003\n\u0005\u000098\u0001\u0000\u0000\u00009:\u0001\u0000\u0000"+
		"\u0000:<\u0001\u0000\u0000\u0000;=\u0005\f\u0000\u0000<;\u0001\u0000\u0000"+
		"\u0000<=\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000\u0000>?\u0005\u0007"+
		"\u0000\u0000?\t\u0001\u0000\u0000\u0000@I\u0003\f\u0006\u0000AC\u0005"+
		"\f\u0000\u0000BA\u0001\u0000\u0000\u0000CD\u0001\u0000\u0000\u0000DB\u0001"+
		"\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000"+
		"FH\u0003\f\u0006\u0000GB\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000\u0000"+
		"IG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000J\u000b\u0001\u0000"+
		"\u0000\u0000KI\u0001\u0000\u0000\u0000LM\u0005\t\u0000\u0000MN\u0005\u0002"+
		"\u0000\u0000NO\u0003\u000e\u0007\u0000O\r\u0001\u0000\u0000\u0000PQ\u0007"+
		"\u0000\u0000\u0000Q\u000f\u0001\u0000\u0000\u0000RV\b\u0001\u0000\u0000"+
		"SV\u0005\u0003\u0000\u0000TV\u0005\u0005\u0000\u0000UR\u0001\u0000\u0000"+
		"\u0000US\u0001\u0000\u0000\u0000UT\u0001\u0000\u0000\u0000VW\u0001\u0000"+
		"\u0000\u0000WU\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000X\u0011"+
		"\u0001\u0000\u0000\u0000Y\\\b\u0001\u0000\u0000Z\\\u0005\u0003\u0000\u0000"+
		"[Y\u0001\u0000\u0000\u0000[Z\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000"+
		"\u0000][\u0001\u0000\u0000\u0000]^\u0001\u0000\u0000\u0000^\u0013\u0001"+
		"\u0000\u0000\u0000\u000f\u0016\u0018\u001e#(+69<DIUW[]";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}