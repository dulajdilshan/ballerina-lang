function f1() {
    _ = from var {x} in [{"x":2, "y":3}, {"x":4, "y":5}]
            col [x];
}
