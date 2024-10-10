// Generated from c:/entwicklung/workspaces/tma/cms/cms-server/cms-sandbox/tests/src/main/resources/com/condation/cms/content/shortcodes/ShortCodeLexer.g4 by ANTLR 4.13.1
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class ShortCodeLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		TAG_OPENING_BRACKET=1, EQUALS=2, SPACE=3, SINGLE_OPEN_BRAKET=4, DASH=5, 
		TAG_CLOSING_BRACKET=6, TAG_CLOSING_CLOSING_BRACKET=7, TAG_OPENING_CLOSING_BRACKET=8, 
		TAG_NAME=9, TAG_STRING=10, TAG_NUMBER=11, TAG_WS=12;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"TAG_OPENING_BRACKET", "EQUALS", "SPACE", "SINGLE_OPEN_BRAKET", "DASH", 
			"TAG_CLOSING_BRACKET", "TAG_CLOSING_CLOSING_BRACKET", "TAG_OPENING_CLOSING_BRACKET", 
			"TAG_NAME", "TAG_STRING", "TAG_NUMBER", "TAG_WS"
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


	public ShortCodeLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ShortCodeLexer.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\fI\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001"+
		"\b\u0005\b2\b\b\n\b\f\b5\t\b\u0001\t\u0001\t\u0005\t9\b\t\n\t\f\t<\t\t"+
		"\u0001\t\u0001\t\u0001\n\u0004\nA\b\n\u000b\n\f\nB\u0001\u000b\u0004\u000b"+
		"F\b\u000b\u000b\u000b\f\u000bG\u0000\u0000\f\u0001\u0001\u0003\u0002\u0005"+
		"\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013\n"+
		"\u0015\u000b\u0017\f\u0001\u0000\u0005\u0002\u0000AZaz\u0003\u000009A"+
		"Zaz\u0001\u0000\"\"\u0001\u000009\u0002\u0000\t\t  L\u0000\u0001\u0001"+
		"\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001"+
		"\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000"+
		"\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000"+
		"\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000"+
		"\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000"+
		"\u0000\u0000\u0017\u0001\u0000\u0000\u0000\u0001\u0019\u0001\u0000\u0000"+
		"\u0000\u0003\u001c\u0001\u0000\u0000\u0000\u0005\u001e\u0001\u0000\u0000"+
		"\u0000\u0007 \u0001\u0000\u0000\u0000\t\"\u0001\u0000\u0000\u0000\u000b"+
		"$\u0001\u0000\u0000\u0000\r\'\u0001\u0000\u0000\u0000\u000f+\u0001\u0000"+
		"\u0000\u0000\u0011/\u0001\u0000\u0000\u0000\u00136\u0001\u0000\u0000\u0000"+
		"\u0015@\u0001\u0000\u0000\u0000\u0017E\u0001\u0000\u0000\u0000\u0019\u001a"+
		"\u0005[\u0000\u0000\u001a\u001b\u0005[\u0000\u0000\u001b\u0002\u0001\u0000"+
		"\u0000\u0000\u001c\u001d\u0005=\u0000\u0000\u001d\u0004\u0001\u0000\u0000"+
		"\u0000\u001e\u001f\u0005 \u0000\u0000\u001f\u0006\u0001\u0000\u0000\u0000"+
		" !\u0005[\u0000\u0000!\b\u0001\u0000\u0000\u0000\"#\u0005-\u0000\u0000"+
		"#\n\u0001\u0000\u0000\u0000$%\u0005]\u0000\u0000%&\u0005]\u0000\u0000"+
		"&\f\u0001\u0000\u0000\u0000\'(\u0005/\u0000\u0000()\u0005]\u0000\u0000"+
		")*\u0005]\u0000\u0000*\u000e\u0001\u0000\u0000\u0000+,\u0005[\u0000\u0000"+
		",-\u0005[\u0000\u0000-.\u0005/\u0000\u0000.\u0010\u0001\u0000\u0000\u0000"+
		"/3\u0007\u0000\u0000\u000002\u0007\u0001\u0000\u000010\u0001\u0000\u0000"+
		"\u000025\u0001\u0000\u0000\u000031\u0001\u0000\u0000\u000034\u0001\u0000"+
		"\u0000\u00004\u0012\u0001\u0000\u0000\u000053\u0001\u0000\u0000\u0000"+
		"6:\u0005\"\u0000\u000079\b\u0002\u0000\u000087\u0001\u0000\u0000\u0000"+
		"9<\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000"+
		"\u0000;=\u0001\u0000\u0000\u0000<:\u0001\u0000\u0000\u0000=>\u0005\"\u0000"+
		"\u0000>\u0014\u0001\u0000\u0000\u0000?A\u0007\u0003\u0000\u0000@?\u0001"+
		"\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000B@\u0001\u0000\u0000\u0000"+
		"BC\u0001\u0000\u0000\u0000C\u0016\u0001\u0000\u0000\u0000DF\u0007\u0004"+
		"\u0000\u0000ED\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000GE\u0001"+
		"\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000H\u0018\u0001\u0000\u0000"+
		"\u0000\u0005\u00003:BG\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}