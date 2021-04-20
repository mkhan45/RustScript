# RustScript
RustScript is a scripting language with no relation to Rust

___

I made this for a school project; it's meant to be implemented into a messaging app as a chat bot so everything should be a one liner. 

Here's some code:

```
let range = fn(a, b) => if (a == b - 1) then ([a]) else ([a] + range(a + 1, b))
let range_to = fn(a, b) => if (a == b - 1) then ([a]) else ([a] + range(a + 1, b))
let fmap = fn(f, ls) => if (ls) then ([f(^ls)] + fmap(f, $ls)) else ([])
let filter = fn(f, ls) => if (ls) then (if (f(^ls)) then ([^ls] + filter(f, $ls)) else (filter(f, $ls))) else ([])
let fold = fn(f, acc, ls) => if (ls) then (fold(f, f(acc, ^ls), $ls)) else (acc)
let sum = fn(ls) => fold(fn (a, b) => a + b, 0, ls)
let product = fn(ls) => fold(fn (a, b) => a * b, 0, ls)
```

```
let x = 5
x
```
```
5
```
___
```
let fib = fn (n) => if (n < 2) then (1) else (fib(n - 1) + fib(n - 2))
fib(10)
```
```
89
```
___
```
range(5, 25)
```
```
[5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24]
```
___
```
let numbers = range(0, 20)
```
```
fmap(fn (n) => n * 2, numbers))
```
```
[0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38]
```
___
```
filter(fn (n) => n % 2 == 0, numbers)
```
```
[0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
```
___
```
fold(fn (acc, n) => acc + n, 0, range(1, 1000))
```
```
499500
```
___
```
sum(range(1, 1000))
```
```
499500
```
___

```
let fib_step = fn (ls) => [^$ls, ^ls + ^$ls]
let efficient_fib = fn (n) => ^$fold(fib_step, [1, 1], range(0, n))
efficient_fib(30)
```
```
2178309
```
___

The `^` and `$` are the head and tail operators respectively.

It's super slow but IMO not bad to write in for basic math stuff.
