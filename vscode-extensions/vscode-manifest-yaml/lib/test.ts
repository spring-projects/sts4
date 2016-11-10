interface Foo {
    box : string;
    get() : string;
};

var foo = {
    box: null,
    get: () => foo.box
};

export = foo