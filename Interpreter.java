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

    public static class List extends Atom {
        ArrayList<Expr> list;

        public List(ArrayList<Expr> list) {
            this.list = list;
        }

        public String toString() {
            return list.toString();
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
        } else {
            throw new Exception("Bad Cmp");
        }
    }

    public Atom negate() throws Exception {
        if (this instanceof Val) {
            Val v = (Val) this;
            return (Atom) new Val(-v.val);
        } else {
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
                try {
                    return variables.get(v.name);
                } catch (Exception e) {
                    throw new Exception(String.format("Tried to access nonexistent variable %s", v.name));
                }
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

            ArrayList<String> argNames = lambda.argNames;

            if (argNames.size() != lambda.argNames.size()) {
                throw new Exception(String.format("Expected %d arguments to call of lambda %s, got %d",
                        lambda.argNames.size(), name, variables.size()));
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
            return String.format("%s = %s", lhs, rhs.toString());
        }
    }
}

enum BinOp {
    Add, Sub, Mul, Div,

    LT, GT, EQ,

    Mod,
}

enum PrefixOp {
    Negate,

    Head, Tail,
}

enum TokenTy {
    LParen, RParen,

    LBracket, RBracket,

    Ident, Number,

    Add, Sub, Mul, Div, Mod,

    EQ, LT, GT,

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
        position -= 1;
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
            default -> addToken(new Token(TokenTy.Ident, lexeme));
        }
    }

    private void scanNumber() {
        position -= 1;
        int start = position;
        while (!isFinished() && Character.isDigit(peek())) {
            eat();
        }

        String lexeme = input.substring(start, position);
        addToken(new Token(TokenTy.Number, lexeme));
    }

    private void addNextToken() throws Exception {
        char c = eat();
        switch (c) {
            case ' ' -> {
            }
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
                    scanIdent();
                } else if (Character.isDigit(c)) {
                    scanNumber();
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
                this.left = 2;
                this.right = 3;
            }
            case Mul, Div, Mod -> {
                this.left = 4;
                this.right = 5;
            }
            case LT, GT, EQ -> {
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
                Atom lambda = new Atom.Lambda(first, argNames);

                ArrayList<Expr> args = new ArrayList<>(2);
                args.add(new Expr.AtomicExpr(lambda));
                args.add(list);

                assertNext(TokenTy.RBracket);
                return new Expr.LambdaCall("fmap", args);
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
                    // do {
                    // out.add(exprBP(0));
                    // } while (expect(TokenTy.Comma));
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
            case Number -> new Expr.AtomicExpr(new Atom.Val(Integer.parseInt(nx.lexeme)));
            case Ident -> {
                if (peek().ty == TokenTy.LParen) {
                    ArrayList<Expr> vars = parseCallArgs();
                    yield new Expr.LambdaCall(nx.lexeme, vars);
                } else {
                    yield new Expr.AtomicExpr(new Atom.Ident(nx.lexeme));
                }
            }
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
        execute("let range_to = fn(a, b) => if (a == b - 1) then ([a]) else ([a] + range(a + 1, b))");
        execute("let fmap = fn(f, ls) => if (ls) then ([f(^ls)] + fmap(f, $ls)) else ([])");
        execute("let filter = fn(f, ls) => if (ls) then (if (f(^ls)) then ([^ls] + filter(f, $ls)) else (filter(f, $ls))) else ([])");
        execute("let fold = fn(f, acc, ls) => if (ls) then (fold(f, f(acc, ^ls), $ls)) else (acc)");
        execute("let sum = fn(ls) => fold(fn (a, b) => a + b, 0, ls)");
        execute("let product = fn(ls) => fold(fn (a, b) => a * b, 0, ls)");
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
        Interpreter i = new Interpreter();
        i.execute("5 + 12 * 3 - 2");
        i.execute("(5 + -12) * (3 - -2)");

        i.execute("let x = 5");
        i.execute("x");

        i.execute("let fib = fn (n) => if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))");
        i.execute("fib(10)");

        i.execute("range(0, 15)");
        i.execute("range(5, 25)");

        i.execute("let numbers = range(0, 20)");

        i.execute("fmap(fn (n) => n * 2, numbers))");
        i.execute("fmap(fn (n) => n % 2, numbers))");

        i.execute("filter(fn (n) => n % 2 == 0, numbers)");

        i.execute("fold(fn (acc, n) => acc + n, 0, [1..1000])");
        i.execute("sum(range(1, 1000))");
        i.execute("let fib_step = fn (ls) => [^$ls, ^ls + ^$ls]");

        i.execute("let efficient_fib = fn (n) => ^$fold(fib_step, [1, 1], range(0, n))");
        i.execute("efficient_fib(30)");

        i.execute("[n * 2 for n in range(0, 10)]");
        i.execute("[n * 2 for n in [0..10]]");
    }
}
