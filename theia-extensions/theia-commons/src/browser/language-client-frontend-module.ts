import { ContainerModule } from 'inversify';
import { ClasspathService } from './classpath-service';
import { ProgressService } from './progress-service';
import { MoveCursorService } from './move-cursor-service';

export default new ContainerModule(bind => {
    // add your contribution bindings here
    bind(ClasspathService).toSelf().inSingletonScope();
    bind(ProgressService).toSelf().inSingletonScope();
    bind(MoveCursorService).toSelf().inSingletonScope();
});