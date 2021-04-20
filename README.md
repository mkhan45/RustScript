# RustScript
RustScript is a scripting language with no relation to Rust

___

I made this for a school project; it's meant to be implemented into a messaging app as a chat bot so everything should be a one liner. 

## Usage

This is an expression based language; everything is an expression. There are global variables though.

The following examples are created using the repl.

#### Basic Arithmetic

```
> 4 * -3 + 12 - -3 + 4 * 15
63
```

### Variables

```
> let x = 5
> x * 15
75
```

### Lists

```
> let ls = [1, 2, 9, 4, 5]
> ls
[1, 2, 9, 4, 5]

> let ls = ls + [2, 4, 6, 8]
> ls
[1, 2, 9, 4, 5, 2, 4, 6, 8]

> ^ls
1

> $ls
[2, 9, 4, 5, 2, 4, 6, 8]
```

### Ranges

```
> [5..12]
[5, 6, 7, 8, 9, 10, 11]
```

### List Comprehensions

```
> [x * x for x in [0..15]]
[0, 1, 4, 9, 16, 25, 36, 49, 64, 81, 100, 121, 144, 169, 196]
```

### Lambdas

```
> let f = fn (x) => x * 2
> f(30)
60

> let apply_twice = fn (f, x) => f(f(x))
> apply_twice(f, 5)
20
```

### Conditionals
```
> if (3 < 5) then (4) else (3)
4

> [if (x % 3 == 0) then (x / 3) else (x * 2) for x in [0..10]]
[0, 2, 4, 1, 8, 10, 2, 14, 16, 3]

> let fib = fn (n) => if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))
> fib(15)
987
```

### Small Standard Library
```
> range(3, 5)
[3, 4]

> fmap(fib, [5..10] + [3, 2])
[8, 13, 21, 34, 55, 3, 2]

> filter(fn (n) => n % 3 == 0, [0..20])
[0, 3, 6, 9, 12, 15, 18]

> fold(fn (a, b) => a + b, 0, [0..20])
190

> sum([0..20])
190

> product([1..10])
362880
```
