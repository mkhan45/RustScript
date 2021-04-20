import java.util.ArrayList;
import java.util.HashMap;

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
        ArrayList<Integer> list;

        public List(ArrayList<Integer> list) {
            this.list = list;
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

    public Atom add(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val + ((Val) rhs).val);
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
            throw new Exception("Badd");
        }
    }

    public Atom div(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Val(((Val) this).val / ((Val) rhs).val);
        } else {
            throw new Exception("Badd");
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
}

abstract class Expr {
    abstract Atom eval(HashMap<String, Atom> variables) throws Exception;

    public static class AtomicExpr extends Expr {
        Atom val;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            if (val instanceof Atom.Ident) {
                Atom.Ident v = (Atom.Ident) val;
                return variables.get(v.name);
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
            if (!(condVal instanceof Atom.Bool)) {
                throw new Exception("Attempted if statement on non-boolean value");
            }
            if (((Atom.Bool) condVal).val) {
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

        // void resolve(HashMap<String, Atom> variables) throws Exception {
        // HashMap<String, Expr> newVariables = new HashMap<>(variables.size());
        // for (String key : variables.keySet()) {
        // newVariables.put(key, new AtomicExpr(variables.get(key)));
        // }
        // newVariables.putAll(this.variables);
        // this.variables = newVariables;
        // }

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
}

enum BinOp {
    Add, Sub, Mul, Div,

    LT, GT, EQ,
}

enum TokenTy {
    LParen, RParen,

    Ident, Number,

    Add, Sub, Mul, Div,

    EQ, LT, GT,

    If, Then, Else,

    Assign, Comma,

    EOF,
}

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
        while (!isFinished() && Character.isAlphabetic(peek())) {
            eat();
        }

        String lexeme = input.substring(start, position);

        switch (lexeme) {
            case "if" -> addToken(new Token(TokenTy.If, lexeme));
            case "then" -> addToken(new Token(TokenTy.Then, lexeme));
            case "else" -> addToken(new Token(TokenTy.Else, lexeme));
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
            case '(' -> addToken(TokenTy.LParen, c);
            case ')' -> addToken(TokenTy.RParen, c);
            case '+' -> addToken(TokenTy.Add, c);
            case '-' -> addToken(TokenTy.Sub, c);
            case '*' -> addToken(TokenTy.Mul, c);
            case '/' -> addToken(TokenTy.Div, c);
            case '<' -> addToken(TokenTy.LT, c);
            case '>' -> addToken(TokenTy.GT, c);
            case '=' -> {
                if (expect('=')) {
                    addToken(TokenTy.EQ, "==");
                } else {
                    addToken(TokenTy.Assign, "=");
                }
            }
            default -> {
                if (Character.isAlphabetic(c)) {
                    scanIdent();
                } else if (Character.isDigit(c)) {
                    scanNumber();
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
            case Mul, Div -> {
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

    private Expr parseIfExpr() throws Exception {
        expect(TokenTy.If);
        Expr cond = exprBP(0);
        expect(TokenTy.Then);
        Expr lhs = exprBP(0);
        expect(TokenTy.Else);
        Expr rhs = exprBP(0);
        return new Expr.IfExpr(cond, lhs, rhs);
    }

    private ArrayList<Expr> parseCallArgs() throws Exception {
        expect(TokenTy.LParen);
        ArrayList<Expr> out = new ArrayList<>();

        if (expect(TokenTy.RParen)) {
            return out;
        } else {
            do {
                out.add(exprBP(0));
            } while (expect(TokenTy.Comma));
        }

        if (!expect(TokenTy.RParen)) {
            throw new Exception("Expected RParen");
        }

        return out;
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
            case If -> parseIfExpr();
            case LParen -> {
                Expr temp = exprBP(0);
                expect(TokenTy.RParen);
                yield temp;
            }
            default -> throw new Exception(String.format("Bad lhs token: %s", nx.toString()));
        };

        for (;;) {
            Token opToken = peek();
            BinOp op = switch (opToken.ty) {
                case Add -> BinOp.Add;
                case Sub -> BinOp.Sub;
                case Mul -> BinOp.Mul;
                case Div -> BinOp.Div;
                case LT -> BinOp.LT;
                case GT -> BinOp.GT;
                case EQ -> BinOp.EQ;
                case EOF, RParen, Then, Else -> null;
                default -> throw new Exception(String.format("Not a binary operator: %s", opToken.toString()));
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

public class Interpreter {
    public static void main(String[] args) throws Exception {
        System.out.println(Parser.parseExpr("5").eval(new HashMap<String, Atom>()).toString());
        System.out.println(Parser.parseExpr("5 + 12 * 3 - 2").eval(new HashMap<String, Atom>()).toString());
        System.out.println(Parser.parseExpr("(5 + 12) * 3 - 2").eval(new HashMap<String, Atom>()).toString());
        System.out.println(Parser.parseExpr("(5 + 12) * (3 - 2)").eval(new HashMap<String, Atom>()).toString());
        System.out.println(Parser.parseExpr("(5 + 12) * (3 - 2)").eval(new HashMap<String, Atom>()).toString());

        {
            String input = "if (1 < 2) then (2) else (3)";
            Expr expr = Parser.parseExpr(input);
            System.out.println(expr.toString());

            Atom res = expr.eval(new HashMap<String, Atom>());
            System.out.println(res.toString());
        }

        {
            Expr fib = Parser.parseExpr("if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))");
            HashMap<String, Atom> vars = new HashMap<>();
            ArrayList<String> varNames = new ArrayList<String>(0);
            varNames.add("n");
            vars.put("fib", new Atom.Lambda(fib, varNames));
            System.out.println(Parser.parseExpr("fib(10)").eval(vars).toString());
        }
    }
}
