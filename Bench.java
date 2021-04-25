public class Bench {
    public static void main(String[] args) throws Exception {
        Interpreter i = new Interpreter();
        i.execute(
                "let ack = fn (m, n) => if (m == 0) then (n + 1) else (if (n == 0) then (ack(m - 1, 1)) else (ack(m - 1, ack(m, n - 1))))");

        long t1 = System.currentTimeMillis();
        i.execute("ack(3, 8)");
        long t2 = System.currentTimeMillis();
        System.out.println(String.format("Time taken: %d ms", t2 - t1));
    }
}
