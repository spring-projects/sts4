declare module 'portfinder' {
    const exports: {
        basePort: number;
        getPort(callback: (err: any, port: number) => void);
    };
    export = exports;
}