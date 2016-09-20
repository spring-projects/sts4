

declare module 'portfinder' {
    var basePort: number;

    function getPort(callback: (err: any, port: number) => void);
}