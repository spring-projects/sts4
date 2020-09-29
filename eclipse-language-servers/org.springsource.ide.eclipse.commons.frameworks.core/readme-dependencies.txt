Important notes about dependencies for commons.frameworks.core

commons.FRAMEWORKS.core is dependended on by other framework tooling like GGTS, CloudFoundry tooling etc.
These tooling do not want to drag in dependencies on things like mylin, m2e etc. Generally only
dependencies on 'plain Eclipse' are allowed in commons fw core.

Since commons.core has dependencies on mylin, m2e etc. commons.frameworks.core should also *not*
dependend on commons.core.

However it is ok for commons.core (or really any other module) to depend on commons.fw.core.
commons.fw.core should be a 'leaf' in the dependency tree if ignore dependencies that are
commonly available in plain eclipse.