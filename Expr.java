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

        public Lambda(Expr expr) {
            this.expr = expr;
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
            System.out.println(this.getClass().getName());
            System.out.println(rhs.getClass().getName());
            throw new Exception("Bad Sub");
        }
    }

    public Atom lt(Atom rhs) throws Exception {
        if ((this instanceof Val) && (rhs instanceof Val)) {
            return (Atom) new Bool(((Val) this).val < ((Val) rhs).val);
        } else {
            throw new Exception("Bad Sub");
        }
    }
}

enum BinOp {
    Add, Sub, LT,
}

public abstract class Expr {
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
    }

    public static class BinaryExpr extends Expr {
        BinOp op;
        Expr lhs;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            return switch (op) {
                case Add -> lhs.eval(variables).add(rhs.eval(variables));
                case Sub -> lhs.eval(variables).sub(rhs.eval(variables));
                case LT -> lhs.eval(variables).lt(rhs.eval(variables));
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
    }

    public static class IfExpr extends Expr {
        Expr cond;
        Expr lhs;
        Expr rhs;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            Atom condVal = cond.eval(variables);
            if (condVal instanceof Atom.Bool && ((Atom.Bool) condVal).val) {
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
    }

    public static class LambdaCall extends Expr {
        HashMap<String, Expr> variables;
        String name;

        Atom eval(HashMap<String, Atom> variables) throws Exception {
            this.resolve(variables);
            HashMap<String, Atom> evaledVariables = new HashMap<>(variables.size());
            for (String key : this.variables.keySet()) {
                evaledVariables.put(key, this.variables.get(key).eval(variables));
            }

            Expr expr = ((Atom.Lambda) evaledVariables.get(this.name)).expr;
            return expr.eval(evaledVariables);
        }

        void resolve(HashMap<String, Atom> variables) throws Exception {
            HashMap<String, Expr> newVariables = new HashMap<>(variables.size());
            for (String key : variables.keySet()) {
                newVariables.put(key, new AtomicExpr(variables.get(key)));
            }
            newVariables.putAll(this.variables);
            this.variables = newVariables;
        }

        public LambdaCall(HashMap<String, Expr> variables, String name) {
            this.variables = variables;
            this.name = name;
        }

        public LambdaCall(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws Exception {
        Atom.Ident n = new Atom.Ident("n");
        Expr cond = new BinaryExpr(BinOp.LT, n, new Atom.Val(1));
        Expr lhs = new AtomicExpr(new Atom.Val(1));

        HashMap<String, Expr> v1 = new HashMap<>();
        v1.put("n", new BinaryExpr(BinOp.Sub, n, new Atom.Val(1)));

        HashMap<String, Expr> v2 = new HashMap<>();
        v2.put("n", new BinaryExpr(BinOp.Sub, n, new Atom.Val(2)));
        Expr rhs = new BinaryExpr(BinOp.Add, new LambdaCall(v1, "fib"), new LambdaCall(v2, "fib"));

        Expr ifExpr = new IfExpr(cond, lhs, rhs);
        Atom.Lambda fib = new Atom.Lambda(ifExpr);

        HashMap<String, Expr> outerVars = new HashMap<>();
        outerVars.put("n", new AtomicExpr(new Atom.Val(10)));
        outerVars.put("fib", new AtomicExpr(fib));
        Expr fibCall = new LambdaCall(outerVars, "fib");
        System.out.println(fibCall.eval(new HashMap<String, Atom>()));
    }
}
