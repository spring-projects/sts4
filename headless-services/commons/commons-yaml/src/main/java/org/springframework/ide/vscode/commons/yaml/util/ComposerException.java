package org.springframework.ide.vscode.commons.yaml.util;

import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

public class ComposerException extends org.yaml.snakeyaml.composer.ComposerException {

    protected ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
        super(context, contextMark, problem, problemMark);
    }
}
