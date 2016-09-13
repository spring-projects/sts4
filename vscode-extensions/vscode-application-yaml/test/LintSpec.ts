import * as assert from 'assert';

// TODO
// describe('lint', () => {
//     it('should report a syntax error', done => {
//         EMPTY_JAVAC.then(javac => {
//             let path = 'test/examples/SyntaxError.java';
            
//             return javac.lint({ path }).then(result => {
//                 let ms = messages(result.messages);
                
//                 assert(ms.length > 0, `${ms} is empty`);

//                 done();
//             });
//         });
//     });

//     it('should report a type error', done => {
//         EMPTY_JAVAC.then(javac => {
//             let path = 'test/examples/TypeError.java';
            
//             return javac.lint({ path }).then(result => {
//                 let ms = messages(result.messages);
                
//                 assert(ms.length > 0, `${ms} is empty`);

//                 done();
//             });
//         });
//     });
// });