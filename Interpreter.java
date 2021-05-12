import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          An Atom is any discrete variable, literal or not
 *
 *          <p>
 *          It's worth noting that this is a significant reason why the
 *          interpreter is so slow; each Atom has a whole lot of space and time
 *          overhead because of how Java stores things. Because of this
 *          optimization is not a priority. To get around this, we could use a
 *          bytecode interpreter which would be orders of magnitude faster but a
 *          bit more complex.
 *          </p>
 */
abstract class Atom {
    public static class Val extends Atom {
        int val;

        public Val(int val) {
            this.val = val;
        }

        public String toString() {
            return String.valueOf(val);
        }
    }

    public static class Bool extends Atom {
        boolean val;

        public Bool(boolean val) {
            this.val = val;
        }

        public String toString() {
            return String.valueOf(val);
        }
    }
    
    public static class Char extends Atom {
    	char val;
    	
    	public Char(char val) {
            this.val = val;
        }

        public String toString() {
            return '\'' + String.valueOf(val) + '\'';
        }
    }

    public static class List extends Atom {
        ArrayList<Expr> list;

        public List(ArrayList<Expr> list) {
            this.list = list;
        }
        
        private boolean isCharArray() {
        	for(int i = 0; i < list.size(); i++) {
        		Expr e = list.get(i);
        		if (!(e instanceof Expr.AtomicExpr && ((Expr.AtomicExpr)e).val instanceof Atom.Char)) return false;
        	}
        	return true;
        }
        private String formatAsString() {
        	assert isCharArray();
        	String result = "\"";
        	for(int i = 0; i < list.size(); i++) {
        		result += ((Atom.Char)((Expr.AtomicExpr)list.get(i)).val).val;
        	}
        	return result + '"';
        }

        public String toString() {
        	if (isCharArray()) return formatAsString();
            return list.toString();
        }
    }
    
    public static class Str extends List {
    	private static ArrayList<Expr> split(String val) {
    		ArrayList<Expr> list = new ArrayList<Expr>();
    		for(int i = 0; i < val.length(); i++) {
    			list.add(new Expr.AtomicExpr(new Atom.Char(val.charAt(i))));
    		}
    		return list;
    	}
    	
    	public Str(String val) {
    		super(split(val));
        }
    }

    public static class Ident extends Atom {
        String name;

        public Ident(String name) {
            this.name = name;
        }

        public String toString() {
            return String.format("\"%s\"", name);
        }
    }

    public static class Lambda extends Atom {
        Expr expr;
        ArrayList<String> argNames;

        public Lambda(Expr expr, ArrayList<String> argNames) {
            this.expr = expr;
            this.argNames = argNames;
        }

        public String toString() {
            return String.format("Lambda {expr: %s, argNames: %s}", expr.toString(), argNames.toString());
        }
    }

    public static class Unit extends Atom {

        public Unit() {
        }

        public String toString() {
            return String.format("()");
        }
    }

    public Atom add(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val + ((Val) rhs).val);
        } else if ((this instanceof List) && (rhs instanceof List)) {
            List lArr = (List) this;
            List rArr = (List) rhs;

            List newList = new List(new ArrayList<Expr>());
            newList.list.addAll(lArr.list);
            newList.list.addAll(rArr.list);
            return (Atom) newList;
        } else {
            throw new Exception("Badd");
        }
    }

    public Atom sub(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val - ((Val) rhs).val);
        } else {
            throw new Exception("Bad Sub");
        }
    }

    public Atom mul(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val * ((Val) rhs).val);
        } else {
            throw new Exception("Bad Mul");
        }
    }

    public Atom div(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val / ((Val) rhs).val);
        } else {
            throw new Exception("Bad Div");
        }
    }

    public Atom mod(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val % ((Val) rhs).val);
        } else {
            throw new Exception("Bad Mod");
        }
    }

    public Atom lt(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Bool(((Val) this).val < ((Val) rhs).val);
        } else {
            throw new Exception("Bad Cmp");
        }
    }

    public Atom gt(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Bool(((Val) this).val > ((Val) rhs).val);
        } else {
            throw new Exception("Bad Cmp");
        }
    }

    public Atom eq(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Bool(((Val) this).val == ((Val) rhs).val);
        }
        else if (this instanceof Bool || rhs instanceof Bool) {
            return (Atom) new Bool(this.isTruthy() == rhs.isTruthy());
        }
        else {
            throw new Exception("Bad Cmp");
        }
    }

    public Atom negate() throws Exception {
    	if (this instanceof Val) {
            Val v = (Val) this;
            return (Atom) new Val(-v.val);
        }
    	else if (this instanceof Bool) {
            Bool b = (Bool) this;
            return (Atom) new Bool(!b.val);
        }
    	else {
            throw new Exception("Bad Negate");
        }
    }

    public Atom head(HashMap<String, Atom> variables) throws Exception {
        if (this instanceof List) {
            List ls = (List) this;
            return (Atom) ls.list.get(0).eval(variables);
        } else {
            throw new Exception("Bad Head");
        }
    }

    public Atom tail(HashMap<String, Atom> variables) throws Exception {
        if (this instanceof List) {
            List ls = (List) this;
            ArrayList<Expr> nls = new ArrayList<>(ls.list.subList(1, ls.list.size()));
            return (Atom) new List(nls);
        } else {
            throw new Exception("Bad Tail");
        }
    }

    public boolean isTruthy() throws Exception {
        if (this instanceof Bool) {
            Bool v = (Bool) this;
            return v.val;
        } else if (this instanceof List) {
            List ls = (List) this;
            return !ls.list.isEmpty();
        } else {
            throw new Exception(String.format("Can't coerce %s to a boolean", this.toString()));
        }
    }

    public Atom and(Atom rhs) throws Exception {
        if (this instanceof Bool && rhs instanceof Bool) {
            Bool lhs = (Bool) this;
            Bool other = (Bool) rhs;
            return new Bool(lhs.val && other.val);
        } else {
            throw new Exception(String.format("Can't coerce %s to a boolean", this.toString()));
        }
    }

    public Atom or(Atom rhs) throws Exception {
        if (this instanceof Bool && rhs instanceof Bool) {
            Bool lhs = (Bool) this;
            Bool other = (Bool) rhs;
            return new Bool(lhs.val || other.val);
        } else {
            throw new Exception(String.format("Can't coerce %s to a boolean", this.toString()));
        }
    }
}

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          An expression, the AST of this language.
 *
 *          <p>
 *          Because this is a functional expression based language, there are no
 *          statements, only expressions. In other words, everything returns
 *          something, even if it's just the unit type.
 *          </p>
 */
abstract class Expr {
    abstract Atom eval(HashMap<String, Atom> variables) throws Exception;

    public static class AtomicExpr extends Expr {
        Atom val;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            if (val instanceof Atom.Ident) {
                Atom.Ident v = (Atom.Ident) val;
                var res = variables.get(v.name);
                if (res == null) {
                    throw new Exception(String.format("Tried to access nonexistent variable %s", v.name));
                }
                return res;
            } else if (val instanceof Atom.List) {
                Atom.List ls = (Atom.List) val;
                ArrayList<Expr> nls = new ArrayList<>();
                for (Expr expr : ls.list) {
                    nls.add(new AtomicExpr(expr.eval(variables)));
                }
                return new Atom.List(nls);
            } else {
                return val;
            }
        }

        public AtomicExpr(Atom val) {
            this.val = val;
        }

        public String toString() {
            return String.valueOf(val);
        }
    }

    public static class PrefixExpr extends Expr {
        PrefixOp op;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            return switch (op) {
                case Negate -> rhs.eval(variables).negate();
                case Head -> rhs.eval(variables).head(variables);
                case Tail -> rhs.eval(variables).tail(variables);
            };
        }

        public PrefixExpr(PrefixOp op, Expr rhs) {
            this.op = op;
            this.rhs = rhs;
        }

        public String toString() {
            return String.format("%s (%s)", op.toString(), rhs.toString());
        }
    }

    public static class BinaryExpr extends Expr {
        BinOp op;
        Expr lhs;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            return switch (op) {
                case Add -> lhs.eval(variables).add(rhs.eval(variables));
                case Sub -> lhs.eval(variables).sub(rhs.eval(variables));
                case Mul -> lhs.eval(variables).mul(rhs.eval(variables));
                case Div -> lhs.eval(variables).div(rhs.eval(variables));
                case Mod -> lhs.eval(variables).mod(rhs.eval(variables));
                case LT -> lhs.eval(variables).lt(rhs.eval(variables));
                case GT -> lhs.eval(variables).gt(rhs.eval(variables));
                case EQ -> lhs.eval(variables).eq(rhs.eval(variables));
                case And -> lhs.eval(variables).and(rhs.eval(variables));
                case Or -> lhs.eval(variables).or(rhs.eval(variables));
            };
        }

        public BinaryExpr(BinOp op, Expr lhs, Expr rhs) {
            this.op = op;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public BinaryExpr(BinOp op, Atom lhs, Atom rhs) {
            this.op = op;
            this.lhs = new AtomicExpr(lhs);
            this.rhs = new AtomicExpr(rhs);
        }

        public String toString() {
            return String.format("%s, (%s, %s)", op.toString(), lhs.toString(), rhs.toString());
        }
    }

    public static class IfExpr extends Expr {
        Expr cond;
        Expr lhs;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            Atom condVal = cond.eval(variables);
            if (condVal.isTruthy()) {
                return lhs.eval(variables);
            } else {
                return rhs.eval(variables);
            }
        }

        public IfExpr(Expr cond, Expr lhs, Expr rhs) {
            this.cond = cond;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public String toString() {
            return String.format("if (%s) then (%s) else (%s)", cond.toString(), lhs.toString(), rhs.toString());
        }
    }

    public static class LambdaCall extends Expr {
        String name;
        ArrayList<Expr> variables;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            HashMap<String, Atom> evaledVariables = new HashMap<>();
            evaledVariables.putAll(variables);

            Atom.Lambda lambda = ((Atom.Lambda) evaledVariables.get(this.name));
            if (lambda == null) {
                throw new Exception(String.format("Undefined lambda '%s'", this.name));
            }

            ArrayList<String> argNames = lambda.argNames;

            if (this.variables.size() != lambda.argNames.size()) {
                throw new Exception(String.format("Expected %d arguments to call of lambda %s, got %d",
                        lambda.argNames.size(), name, this.variables.size()));
            }

            for (int i = 0; i < argNames.size(); i += 1) {
                evaledVariables.put(argNames.get(i), this.variables.get(i).eval(variables));
            }

            Expr expr = lambda.expr;
            return expr.eval(evaledVariables);
        }

        public LambdaCall(String name) {
            this.name = name;
        }

        public LambdaCall(String name, ArrayList<Expr> variables) {
            this.name = name;
            this.variables = variables;
        }

        public String toString() {
            return String.format("%s(%s)", name, variables.toString());
        }
    }

    public static class AssignExpr extends Expr {
        String lhs;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            variables.put(lhs, rhs.eval(variables));
            return new Atom.Unit();
        }

        public AssignExpr(String lhs, Expr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        public String toString() {
            return String.format("let %s = %s", lhs, rhs.toString());
        }
    }

    public static void testExpr() throws Exception {
        // all the eval methods are mutually recursive but since it's essentially a tree
        // instead of a potentially cyclic graph it *is* possible to test them all
        // individually

        HashMap<String, Atom> emptyScope = new HashMap<>();

        AtomicExpr e1 = new AtomicExpr(new Atom.Val(1));
        assert ((Atom.Val) e1.eval(emptyScope)).val == 1;

        HashMap<String, Atom> piScope = new HashMap<>();
        piScope.put("pi", new Atom.Val(3));
        AtomicExpr e2 = new AtomicExpr(new Atom.Ident("pi"));
        assert ((Atom.Val) e2.eval(piScope)).val == 3;

        PrefixExpr e3 = new PrefixExpr(PrefixOp.Negate, new AtomicExpr(new Atom.Val(3)));
        assert ((Atom.Val) e3.eval(emptyScope)).val == -3;

        BinaryExpr e4 = new BinaryExpr(BinOp.Add, new Atom.Val(10), new Atom.Val(20));
        assert ((Atom.Val) e4.eval(emptyScope)).val == 30;

        IfExpr e5 = new IfExpr(new AtomicExpr(new Atom.Bool(false)), new AtomicExpr(new Atom.Val(10)),
                new AtomicExpr(new Atom.Val(20)));
        assert ((Atom.Val) e5.eval(emptyScope)).val == 20;

        IfExpr e6 = new IfExpr(new AtomicExpr(new Atom.Bool(true)), new AtomicExpr(new Atom.Val(10)),
                new AtomicExpr(new Atom.Val(20)));
        assert ((Atom.Val) e6.eval(emptyScope)).val == 10;

        HashMap<String, Atom> lambdaScope = new HashMap<>();

        AtomicExpr fib = (AtomicExpr) Parser.parseExpr("fn (n) => if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))");
        AtomicExpr add = (AtomicExpr) Parser.parseExpr("fn (start, end) => start + end");

        lambdaScope.put("fib", fib.val);
        lambdaScope.put("add", add.val);

        LambdaCall e7 = (LambdaCall) Parser.parseExpr("fib(10)");
        LambdaCall e8 = (LambdaCall) Parser.parseExpr("add(5, 10)");
        assert ((Atom.Val) e7.eval(lambdaScope)).val == 89;
        assert ((Atom.Val) e8.eval(lambdaScope)).val == 15;

        HashMap<String, Atom> newScope = new HashMap<>();

        AssignExpr e9 = (AssignExpr) Parser.parseExpr("let x = 15");
        e9.eval(newScope);
        assert ((Atom.Val) newScope.get("x")).val == 15;
        AssignExpr e10 = (AssignExpr) Parser.parseExpr("let x = x * x");
        e10.eval(newScope);
        assert ((Atom.Val) newScope.get("x")).val == 15 * 15;
    }
}

enum BinOp {
    Add, Sub, Mul, Div,

    LT, GT, EQ,

    And, Or,

    Mod,
}

enum PrefixOp {
    Negate,

    Head, Tail,
}

enum TokenTy {
    LParen, RParen,

    LBracket, RBracket,

    Ident, Number, Character, String, True, False,

    Add, Sub, Mul, Div, Mod,

    EQ, LT, GT,

    And, Or,

    If, Then, Else,

    Let, Assign, Comma,

    Caret, Dollar,

    Fn, Arrow,

    For, In, DotDot,

    EOF,
}

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          A token
 *
 *          <p>
 *          A token represents a basic building block of the flat structure of
 *          the language. They're easier to work with than characters.
 *          </p>
 */
class Token {
    TokenTy ty;
    String lexeme;

    public Token(TokenTy ty, String lexeme) {
        this.ty = ty;
        this.lexeme = lexeme;
    }

    public static Token EOF() {
        return new Token(TokenTy.EOF, null);
    }

    public String toString() {
        return switch (ty) {
            case Ident -> String.format("Ident '%s'", lexeme);
            case Number -> String.format("Number %s", lexeme);
            default -> String.format("%s", ty.toString());
        };
    }
}

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          The Tokenizer takes a String and turns it into a flat list of
 *          Tokens.
 *
 */
class Tokenizer {
    private String input;
    private int position;
    private ArrayList<Token> output;

    private Tokenizer(String input) {
        this.input = input;
        this.position = 0;
        this.output = new ArrayList<Token>();
    }

    private boolean isFinished() {
        return this.position >= this.input.length();
    }

    private char peek() {
        return input.charAt(position);
    }

    private char eat() {
        char ret = input.charAt(position);
        position += 1;
        return ret;
    }

    private boolean expect(char expected) {
        char c = input.charAt(position);
        if (c == expected) {
            position += 1;
            return true;
        } else {
            return false;
        }
    }

    private void addToken(Token t) {
        this.output.add(t);
    }

    private void addToken(TokenTy ty, char lexeme) {
        addToken(new Token(ty, String.valueOf(lexeme)));
    }

    private void addToken(TokenTy ty, String lexeme) {
        addToken(new Token(ty, lexeme));
    }

    private void scanIdent() {
        int start = position;
        while (!isFinished() && (Character.isAlphabetic(peek()) || Character.isDigit(peek()) || peek() == '_')) {
            eat();
        }

        String lexeme = input.substring(start, position);

        switch (lexeme) {
            case "if" -> addToken(new Token(TokenTy.If, lexeme));
            case "then" -> addToken(new Token(TokenTy.Then, lexeme));
            case "else" -> addToken(new Token(TokenTy.Else, lexeme));
            case "let" -> addToken(new Token(TokenTy.Let, lexeme));
            case "fn" -> addToken(new Token(TokenTy.Fn, lexeme));
            case "for" -> addToken(new Token(TokenTy.For, lexeme));
            case "in" -> addToken(new Token(TokenTy.In, lexeme));
            case "true" -> addToken(new Token(TokenTy.True, lexeme));
            case "false" -> addToken(new Token(TokenTy.False, lexeme));
            default -> addToken(new Token(TokenTy.Ident, lexeme));
        }
    }

    private void scanNumber() {
        int start = position;
        while (!isFinished() && Character.isDigit(peek())) {
            eat();
        }

        String lexeme = input.substring(start, position);
        addToken(new Token(TokenTy.Number, lexeme));
    }
    
    private static boolean isHex(char c) {
		return Character.isDigit(c) || (c >= 'A' && c <= 'F');
	}
    private static boolean isHex(String s) {
    	for(int i = 0; i < s.length(); i++) {
    		if (!isHex(s.charAt(i))) return false;
    	}
    	return true;
    }
    
    private String unescaper(String sequence) {
    	// TODO: Implement unescaper for:		https://en.wikipedia.org/wiki/Escape_character
    	// 			* Octal (\1 to \377)
    	// 			* Unicode: 					https://en.wikipedia.org/wiki/List_of_Unicode_characters	https://www.rapidtables.com/code/text/unicode-characters.html
    	// 			* Control characters: 		https://en.wikipedia.org/wiki/Control_character
    	// 			* Whitespace characters: 	https://en.wikipedia.org/wiki/Whitespace_character
    	String result = "";
    	for(int p = 0; p < sequence.length(); p++) {
    		char c = sequence.charAt(p);
    		if (c == '\\') {
    			p++;
	    		String remaining = sequence.substring(p);
	    		if (remaining.length() >= 5 && remaining.charAt(0) == 'u' && isHex(remaining.substring(1, 5))) {
	    			char unicodeChar = (char)Integer.parseInt(remaining.substring(1, 5), 16);
	    			result += unicodeChar;
	    			p += 4; // p will increment before next iteration
	    		}
	    		else {
	    			result += remaining.charAt(0);
	    		}
    		}
    		else result += c;
    	}
    	return result;
    }
    
    private void scanCharacter() throws Exception {
    	expect('\''); // Will always match
    	int start = position;
    	boolean escaped;
    	while (!isFinished() && peek() != '\'') {
    		escaped = peek() == '\\';
            eat();
            if (escaped && (peek() == '\'' || peek() == '\\')) eat(); // eat escaped apostrophes or backslashes
        }
    	if (start == position) throw new Exception("Missing character, '' is not valid.");
        String lexeme = input.substring(start, position);
    	if (!isFinished() && peek() == '\'') eat();
    	else throw new Exception("Found character with a missing closing apostrophe, did you mean '" + lexeme + "'?");
    	
    	String characters = unescaper(lexeme);
    	if (characters.length() > 1) throw new Exception("Found invalid character, did you mean \"" + characters + "\"?");
    	char character = characters.charAt(0);

    	addToken(new Token(TokenTy.Character, "" + character));
    }
    
    private void scanString() throws Exception {
    	expect('"'); // Will always match
    	int start = position;
    	boolean escaped;
    	while (!isFinished() && peek() != '"') {
    		escaped = peek() == '\\';
            eat();
            if (escaped && (peek() == '\"' || peek() == '\\')) eat(); // eat escaped apostrophes or backslashes
        }
        String lexeme = input.substring(start, position);
    	if (!isFinished() && peek() == '"') eat();
    	else throw new Exception("Found string with a missing closing quotation mark, did you mean \"" + lexeme + "\"?");
    	
    	String string = unescaper(lexeme);

    	addToken(new Token(TokenTy.String, string));
    }

    private void addNextToken() throws Exception {
        char c = eat();
        switch (c) {
            case ' ' -> addNextToken();
            case '(' -> addToken(TokenTy.LParen, c);
            case ')' -> addToken(TokenTy.RParen, c);
            case '[' -> addToken(TokenTy.LBracket, c);
            case ']' -> addToken(TokenTy.RBracket, c);
            case '+' -> addToken(TokenTy.Add, c);
            case '-' -> addToken(TokenTy.Sub, c);
            case '*' -> addToken(TokenTy.Mul, c);
            case '%' -> addToken(TokenTy.Mod, c);
            case '/' -> addToken(TokenTy.Div, c);
            case '<' -> addToken(TokenTy.LT, c);
            case '>' -> addToken(TokenTy.GT, c);
            case ',' -> addToken(TokenTy.Comma, c);
            case '^' -> addToken(TokenTy.Caret, c);
            case '$' -> addToken(TokenTy.Dollar, c);
            case '|' -> {
                if (expect('|')) {
                    addToken(TokenTy.Or, "||");
                } else {
                    throw new Exception("Found a single '|', did you mean '||'?");
                }
            }
            case '&' -> {
                if (expect('&')) {
                    addToken(TokenTy.And, "&&");
                } else {
                    throw new Exception("Found a single '&', did you mean '&&'?");
                }
            }
            case '.' -> {
                if (expect('.')) {
                    addToken(TokenTy.DotDot, "..");
                } else {
                    throw new Exception("Found a single '.', did you mean '..'?");
                }
            }
            case '=' -> {
                if (expect('=')) {
                    addToken(TokenTy.EQ, "==");
                } else if (expect('>')) {
                    addToken(TokenTy.Arrow, "=>");
                } else {
                    addToken(TokenTy.Assign, "=");
                }
            }
            default -> {
                if (Character.isAlphabetic(c)) {
                    position -= 1;
                    scanIdent();
                } else if (Character.isDigit(c)) {
                    position -= 1;
                    scanNumber();
                } else if (c == '\'') {
                    position -= 1;
                    scanCharacter();
                }else if (c == '"') {
                    position -= 1;
                    scanString();
                } else {
                    throw new Exception(String.format("Unexpected character: %c", c));
                }
            }
        };
    }

    public static ArrayList<Token> tokenize(String input) throws Exception {
        Tokenizer t = new Tokenizer(input);

        while (!t.isFinished()) {
            t.addNextToken();
        }

        return t.output;
    }

    public static void testTokenizer() throws Exception {
        {
            // tests constructor and isFinished
            var t = new Tokenizer("");
            assert t.isFinished();
        }

        {
            // tests scanIdent

            // a non-keyword identifier
            var t0 = new Tokenizer("valid_identifier");
            t0.scanIdent();
            assert t0.output.get(0).ty == TokenTy.Ident;
            assert t0.output.get(0).lexeme.equals("valid_identifier");

            // keywords
            String keywords = "if then else let fn for in";
            String[] keywordLS = keywords.split(" ");
            TokenTy[] kwTokens = { TokenTy.If, TokenTy.Then, TokenTy.Else, TokenTy.Let, TokenTy.Fn, TokenTy.For,
                    TokenTy.In };

            for (int i = 0; i < keywordLS.length; i += 1) {
                var t1 = new Tokenizer(keywordLS[i]);
                t1.scanIdent();

                Token t = t1.output.get(0);

                assert t.ty == kwTokens[i];
                assert t.lexeme.equals(keywordLS[i]);
            }
        }

        {
            // tests scanNumber
            String numbers = "5 10 20 30";
            String[] numberLS = numbers.split(" ");

            for (int i = 0; i < numberLS.length; i += 1) {
                var t1 = new Tokenizer(numberLS[i]);
                t1.scanNumber();

                Token t = t1.output.get(0);

                assert t.ty == TokenTy.Number;
                assert t.lexeme.equals(numberLS[i]);
            }
        }

        {
            // tests addNextToken
            String input = "for x in [10..12] 15 let";

            Tokenizer t = new Tokenizer(input);

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.For;
            assert t.output.get(t.output.size() - 1).lexeme.equals("for");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.Ident;
            assert t.output.get(t.output.size() - 1).lexeme.equals("x");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.In;
            assert t.output.get(t.output.size() - 1).lexeme.equals("in");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.LBracket;
            assert t.output.get(t.output.size() - 1).lexeme.equals("[");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.Number;
            assert t.output.get(t.output.size() - 1).lexeme.equals("10");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.DotDot;
            assert t.output.get(t.output.size() - 1).lexeme.equals("..");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.Number;
            assert t.output.get(t.output.size() - 1).lexeme.equals("12");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.RBracket;
            assert t.output.get(t.output.size() - 1).lexeme.equals("]");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.Number;
            assert t.output.get(t.output.size() - 1).lexeme.equals("15");

            t.addNextToken();
            assert t.output.get(t.output.size() - 1).ty == TokenTy.Let;
            assert t.output.get(t.output.size() - 1).lexeme.equals("let");
        }

        {
            // tests tokenize()
            ArrayList<Token> tokens = tokenize("for x in [0..15]");

            assert tokens.get(0).ty == TokenTy.For;
            assert tokens.get(0).lexeme.equals("for");

            assert tokens.get(1).ty == TokenTy.Ident;
            assert tokens.get(1).lexeme.equals("x");

            assert tokens.get(2).ty == TokenTy.In;
            assert tokens.get(2).lexeme.equals("in");

            assert tokens.get(3).ty == TokenTy.LBracket;
            assert tokens.get(3).lexeme.equals("[");

            assert tokens.get(4).ty == TokenTy.Number;
            assert tokens.get(4).lexeme.equals("0");

            assert tokens.get(5).ty == TokenTy.DotDot;
            assert tokens.get(5).lexeme.equals("..");

            assert tokens.get(6).ty == TokenTy.Number;
            assert tokens.get(6).lexeme.equals("15");

            assert tokens.get(7).ty == TokenTy.RBracket;
            assert tokens.get(7).lexeme.equals("]");
        }
    }
}

class BindingPower {
    public int left;
    public int right;

    public BindingPower(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public BindingPower(BinOp op) {
        switch (op) {
            case Add, Sub -> {
                this.left = 4;
                this.right = 5;
            }
            case Mul, Div, Mod -> {
                this.left = 6;
                this.right = 7;
            }
            case LT, GT, EQ -> {
                this.left = 2;
                this.right = 3;
            }
            case And, Or -> {
                this.left = 0;
                this.right = 1;
            }
        }
    }
}

class PrefixBindingPower {
    public int right;

    public PrefixBindingPower(int right) {
        this.right = right;
    }

    public PrefixBindingPower(PrefixOp op) throws Exception {
        switch (op) {
            case Negate, Head, Tail -> this.right = 10;
        };
    }
}

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          A Parser takes a String, tokenizes it using a Tokenizer, and then
 *          converts the flat list of tokens into a meaningful Expr AST which
 *          can be evaluated.
 *
 *          <p>
 *          https://matklad.github.io/2020/04/13/simple-but-powerful-pratt-parsing.html
 *          <br>
 *          I read this article every time I write a parser.
 *          </p>
 *
 */
class Parser {
    int position;
    ArrayList<Token> tokens;

    private Parser(ArrayList<Token> tokens) {
        this.position = 0;
        this.tokens = tokens;
    }

    private boolean isFinished() {
        return this.position >= this.tokens.size();
    }

    private Token peek() {
        return isFinished() ? Token.EOF() : tokens.get(position);
    }

    private Token eat() {
        if (!isFinished()) {
            Token ret = tokens.get(position);
            position += 1;
            return ret;
        } else {
            return Token.EOF();
        }
    }

    private boolean expect(TokenTy expected) {
        if (isFinished())
            return false;

        Token c = tokens.get(position);
        if (c.ty == expected) {
            position += 1;
            return true;
        } else {
            return false;
        }
    }

    private void assertNext(TokenTy expected) throws Exception {
        var nx = eat();
        if (nx.ty != expected) {
            throw new Exception(String.format("Expected %s, got %s", expected.toString(), nx.toString()));
        }
    }

    private Expr parseIfExpr() throws Exception {
        Expr cond = exprBP(0);
        assertNext(TokenTy.Then);
        Expr lhs = exprBP(0);
        assertNext(TokenTy.Else);
        Expr rhs = exprBP(0);
        return new Expr.IfExpr(cond, lhs, rhs);
    }

    private Expr parseList() throws Exception {
        if (peek().ty != TokenTy.RBracket) {
            Expr first = exprBP(0);

            if (peek().ty == TokenTy.For) {
                // list comprehension
                assertNext(TokenTy.For);

                Token ident = eat();
                if (ident.ty != TokenTy.Ident) {
                    throw new Exception("Invalid list comp, expected an identifier after 'for'.");
                }

                String name = ident.lexeme;

                assertNext(TokenTy.In);

                Expr list = exprBP(0);

                ArrayList<String> argNames = new ArrayList<>();
                argNames.add(name);
                Atom mapLambda = new Atom.Lambda(first, argNames);

                ArrayList<Expr> args = new ArrayList<>(2);
                args.add(new Expr.AtomicExpr(mapLambda));
                args.add(list);

                Expr fmap = new Expr.LambdaCall("fmap", args);

                if (expect(TokenTy.If)) {
                    Expr cond = exprBP(0);
                    Atom filterLambda = new Atom.Lambda(cond, argNames);

                    ArrayList<Expr> filterArgs = new ArrayList<>(2);
                    filterArgs.add(new Expr.AtomicExpr(filterLambda));
                    filterArgs.add(fmap);

                    assertNext(TokenTy.RBracket);
                    return new Expr.LambdaCall("filter", filterArgs);
                } else {
                    assertNext(TokenTy.RBracket);
                    return fmap;
                }
            } else if (peek().ty == TokenTy.DotDot) {
                // range literal
                assertNext(TokenTy.DotDot);

                Expr end = exprBP(0);

                ArrayList<Expr> args = new ArrayList<>(2);
                args.add(first);
                args.add(end);

                assertNext(TokenTy.RBracket);
                return new Expr.LambdaCall("range", args);
            } else {
                // array literal
                ArrayList<Expr> out = new ArrayList<>();
                out.add(first);

                if (peek().ty != TokenTy.RBracket) {
                    while (expect(TokenTy.Comma)) {
                        out.add(exprBP(0));
                    }
                }

                assertNext(TokenTy.RBracket);
                return new Expr.AtomicExpr(new Atom.List(out));
            }
        } else {
            assertNext(TokenTy.RBracket);
            return new Expr.AtomicExpr(new Atom.List(new ArrayList<>()));
        }
    }

    private ArrayList<Expr> parseCallArgs() throws Exception {
        assertNext(TokenTy.LParen);
        ArrayList<Expr> out = new ArrayList<>();

        if (peek().ty != TokenTy.RParen) {
            do {
                out.add(exprBP(0));
            } while (expect(TokenTy.Comma));
        }

        assertNext(TokenTy.RParen);
        return out;
    }

    private Expr parseLetExpr() throws Exception {
        Token ident = eat();
        if (ident.ty != TokenTy.Ident) {
            throw new Exception("Invalid let expression");
        }

        assertNext(TokenTy.Assign);

        Expr rhs = exprBP(0);

        return new Expr.AssignExpr(ident.lexeme, rhs);
    }

    private Expr parseLambdaExpr() throws Exception {
        assertNext(TokenTy.LParen);

        ArrayList<String> argNames = new ArrayList<>();

        if (peek().ty != TokenTy.RParen) {
            do {
                Token nx = eat();
                if (nx.ty != TokenTy.Ident) {
                    throw new Exception(String.format("Unexpected %s", nx.toString()));
                }
                argNames.add(nx.lexeme);
            } while (expect(TokenTy.Comma));
        }
        assertNext(TokenTy.RParen);

        assertNext(TokenTy.Arrow);

        Expr expr = exprBP(0);

        return new Expr.AtomicExpr(new Atom.Lambda(expr, argNames));
    }

    private Expr exprBP(int minBP) throws Exception {
        Token nx = eat();
        Expr lhs = switch (nx.ty) {
            case True -> new Expr.AtomicExpr(new Atom.Bool(true));
            case False -> new Expr.AtomicExpr(new Atom.Bool(false));
            case Number -> new Expr.AtomicExpr(new Atom.Val(Integer.parseInt(nx.lexeme)));
            case Ident -> {
                if (peek().ty == TokenTy.LParen) {
                    ArrayList<Expr> vars = parseCallArgs();
                    yield new Expr.LambdaCall(nx.lexeme, vars);
                } else {
                    yield new Expr.AtomicExpr(new Atom.Ident(nx.lexeme));
                }
            }
            case Character -> new Expr.AtomicExpr(new Atom.Char(nx.lexeme.charAt(0)));
            case String -> new Expr.AtomicExpr(new Atom.Str(nx.lexeme));
            case Let -> parseLetExpr();
            case Fn -> parseLambdaExpr();
            case If -> parseIfExpr();
            case LBracket -> parseList();
            case LParen -> {
                Expr temp = exprBP(0);
                expect(TokenTy.RParen);
                yield temp;
            }
            case Sub, Caret, Dollar -> {
                PrefixOp op = switch (nx.ty) {
                    case Sub -> PrefixOp.Negate;
                    case Caret -> PrefixOp.Head;
                    case Dollar -> PrefixOp.Tail;
                    default -> throw new Exception("Unreachable hopefully");
                };

                PrefixBindingPower bp = new PrefixBindingPower(op);
                Expr rhs = exprBP(bp.right);
                yield new Expr.PrefixExpr(op, rhs);
            }
            default -> throw new Exception(String.format("Expected an expression, found: %s", nx.toString()));
        };

        for (;;) {
            Token opToken = peek();
            BinOp op = switch (opToken.ty) {
                case Add -> BinOp.Add;
                case Sub -> BinOp.Sub;
                case Mul -> BinOp.Mul;
                case Div -> BinOp.Div;
                case Mod -> BinOp.Mod;
                case LT -> BinOp.LT;
                case GT -> BinOp.GT;
                case EQ -> BinOp.EQ;
                case And -> BinOp.And;
                case Or -> BinOp.Or;
                default -> null;
            };

            if (op == null)
                break;

            //

            BindingPower bp = new BindingPower(op);
            if (bp.left < minBP) {
                break;
            }

            eat();

            Expr rhs = exprBP(bp.right);
            lhs = new Expr.BinaryExpr(op, lhs, rhs);
        }

        return lhs;
    }

    public static Expr parseExpr(String input) throws Exception {
        ArrayList<Token> tokens = Tokenizer.tokenize(input);
        Parser p = new Parser(tokens);

        return p.exprBP(0);
    }

    public static void testParser() throws Exception {
        // Testing parser methods individually isn't actually possible since they're
        // mutually recursive.
        //
        // the expression parsers individually expect to come after the first token has
        // been parsed, which is why they're each missing a token.

        {
            // tests parseIfExpr
            ArrayList<Token> tokens = Tokenizer.tokenize("(1) then (2) else (5)");
            Parser p = new Parser(tokens);
            Expr.IfExpr expr = (Expr.IfExpr) p.parseIfExpr();
            assert expr.toString().equals("if (1) then (2) else (5)");
        }

        {
            // tests parseList
            ArrayList<Token> tokens = Tokenizer.tokenize("1, 3, 2, 4]");
            Parser p = new Parser(tokens);
            Expr.AtomicExpr expr = (Expr.AtomicExpr) p.parseList();
            assert expr.toString().equals("[1, 3, 2, 4]");

            ArrayList<Token> tokensRange = Tokenizer.tokenize("0..10]");
            Parser parserRange = new Parser(tokensRange);
            Expr.LambdaCall exprRange = (Expr.LambdaCall) parserRange.parseList();
            assert exprRange.toString().equals("range([0, 10])");

            ArrayList<Token> tokensComp = Tokenizer.tokenize("x * 2 for x in [2..5]]");
            Parser parserComp = new Parser(tokensComp);
            Expr.LambdaCall exprComp = (Expr.LambdaCall) parserComp.parseList();
            assert exprComp.toString().equals("fmap([Lambda {expr: Mul, (\"x\", 2), argNames: [x]}, range([2, 5])])");
        }

        {
            // tests parseCallArgs
            ArrayList<Token> tokens = Tokenizer.tokenize("(2, 4, 6, 8, fib)");
            Parser p = new Parser(tokens);
            ArrayList<Expr> out = p.parseCallArgs();
            assert out.toString().equals("[2, 4, 6, 8, \"fib\"]");
        }

        {
            // tests parseLetExpr
            ArrayList<Token> tokens = Tokenizer.tokenize("x = 5");
            Parser p = new Parser(tokens);
            Expr.AssignExpr expr = (Expr.AssignExpr) p.parseLetExpr();
            assert expr.toString().equals("let x = 5");
        }

        {
            // tests arbitrary arithmetic expr with order of operations
            Expr expr = parseExpr("x + 3 * 5 - 2 / 4");
            assert expr.toString().equals("Sub, (Add, (\"x\", Mul, (3, 5)), Div, (2, 4))");
        }
    }
}

/**
 * @author Mikail Khan <mikail@mikail-khan.com>
 * @version 0.1.0
 * 
 *          Because this is an expression based language we don't need to deal
 *          with complicated scoping and whatnot. This keeps the interpreter
 *          very simple.
 *
 */
public class Interpreter {
    HashMap<String, Atom> globals;

    public Interpreter() throws Exception {
        globals = new HashMap<>();

        // small standard library
        execute("let range = fn(a, b) => if (a == b - 1) then ([a]) else ([a] + range(a + 1, b))");
        execute("let fmap = fn(f, ls) => if (ls) then ([f(^ls)] + fmap(f, $ls)) else ([])");
        execute("let filter = fn(f, ls) => if (ls) then (if (f(^ls)) then ([^ls] + filter(f, $ls)) else (filter(f, $ls))) else ([])");
        execute("let fold = fn(f, acc, ls) => if (ls) then (fold(f, f(acc, ^ls), $ls)) else (acc)");
        execute("let sum = fn(ls) => fold(fn (a, b) => a + b, 0, ls)");
        execute("let product = fn(ls) => fold(fn (a, b) => a * b, 1, ls)");
        execute("let reverse = fn(ls) => fold(fn (rs, el) => [el] + rs, [], ls)");
    }

    public Atom eval(String expr) throws Exception {
        return Parser.parseExpr(expr).eval(globals);
    }

    public void execute(String expr) throws Exception {
        Atom res = eval(expr);
        if (!(res instanceof Atom.Unit)) {
            System.out.println(res.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        Tokenizer.testTokenizer();
        Parser.testParser();
        Expr.testExpr();

        // Some full stack tests
        //
        // I use toString as a hash function to test for equality

        Interpreter i = new Interpreter();

        Atom val1 = i.eval("5 + 12 * 3 - 2");
        assert val1 instanceof Atom.Val;
        assert ((Atom.Val) val1).val == 39;

        Atom val2 = i.eval("(5 + -12) * (3 - -2)");
        assert val2 instanceof Atom.Val;
        assert ((Atom.Val) val2).val == -35;

        Atom val3 = i.eval("let x = 5");
        assert val3 instanceof Atom.Unit;

        Atom val4 = i.eval("x");
        assert val4 instanceof Atom.Val;
        assert ((Atom.Val) val4).val == 5;

        i.eval("let fib = fn (n) => if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))");
        Atom val5 = i.eval("fib(10)");
        assert val5 instanceof Atom.Val;
        assert ((Atom.Val) val5).val == 89;

        Atom val6 = i.eval("range(5, 10)");
        assert val6 instanceof Atom.List;
        ArrayList<Expr> list1 = new ArrayList<>();
        list1.add(new Expr.AtomicExpr(new Atom.Val(5)));
        list1.add(new Expr.AtomicExpr(new Atom.Val(6)));
        list1.add(new Expr.AtomicExpr(new Atom.Val(7)));
        list1.add(new Expr.AtomicExpr(new Atom.Val(8)));
        list1.add(new Expr.AtomicExpr(new Atom.Val(9)));
        assert ((Atom.List) val6).list.toString().equals(list1.toString());

        Atom val7 = i.eval("range(0, 20)");
        Atom val8 = i.eval("[0..20]");
        assert ((Atom.List) val7).list.toString().equals(((Atom.List) val8).list.toString());

        Atom val9 = i.eval("fmap(fn (n) => n * 2, [0..3]))");
        ArrayList<Expr> list2 = new ArrayList<>();
        list2.add(new Expr.AtomicExpr(new Atom.Val(0)));
        list2.add(new Expr.AtomicExpr(new Atom.Val(2)));
        list2.add(new Expr.AtomicExpr(new Atom.Val(4)));
        assert ((Atom.List) val9).list.toString().equals(list2.toString());

        Atom val10 = i.eval("filter(fn (n) => n % 3 == 0, [0..10])");
        ArrayList<Expr> list3 = new ArrayList<>();
        list3.add(new Expr.AtomicExpr(new Atom.Val(0)));
        list3.add(new Expr.AtomicExpr(new Atom.Val(3)));
        list3.add(new Expr.AtomicExpr(new Atom.Val(6)));
        list3.add(new Expr.AtomicExpr(new Atom.Val(9)));
        assert ((Atom.List) val10).list.toString().equals(list3.toString());

        Atom val11 = i.eval("fold(fn (acc, n) => acc + n, 0, [1..1000])");
        Atom val12 = i.eval("sum(range(1, 1000))");

        assert ((Atom.Val) val11).val == 499500;
        assert ((Atom.Val) val11).val == ((Atom.Val) val12).val;

        i.eval("let fib_step = fn (ls, i) => [^$ls, ^ls + ^$ls]");
        i.eval("let efficient_fib = fn (n) => ^$fold(fib_step, [1, 1], [0..n])");
        Atom val13 = i.eval("efficient_fib(30)");

        assert ((Atom.Val) val13).val == 2178309;

        System.out.println("All tests passed!");
    }
}
