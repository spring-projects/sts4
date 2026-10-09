import { MCP_SERVER_NAME, SCOPED_TOOL_PREFIX } from './plugin-config.mjs';

const TOOL_ALIASES = { Read: ['view'], Skill: ['skill'], Glob: ['glob'], Grep: ['grep'], Bash: ['bash'], Agent: ['agent'] };

/** Copilot selects plugin-provided agents by the file-derived id, without a plugin namespace. */
export function copilotAgentId(inputMatch) {
    return typeof inputMatch === 'string' && /^[\w-]+$/.test(inputMatch) ? inputMatch : undefined;
}

/** Count trace entries matching a grader's tool name and optional JSON-input pattern. */
export function countToolMatches(grader, trace) {
    const wanted = grader.tool.startsWith(SCOPED_TOOL_PREFIX)
        ? [`${MCP_SERVER_NAME}-${grader.tool.slice(SCOPED_TOOL_PREFIX.length)}`]
        : (TOOL_ALIASES[grader.tool] ?? [grader.tool]);
    const pattern = grader.input_match ? new RegExp(grader.input_match) : null;
    return trace.filter((entry) => wanted.includes(entry.tool) && (!pattern || pattern.test(entry.input))).length;
}

/** Apply the eval grader's inclusive min/max occurrence bounds (default: at least one). */
export function toolGradePasses(grader, trace) {
    const count = countToolMatches(grader, trace);
    const min = grader.min === undefined ? 1 : Number(grader.min);
    const max = grader.max === undefined ? Number.POSITIVE_INFINITY : Number(grader.max);
    return Number.isFinite(min) && min >= 0 && !Number.isNaN(max) && max >= min && count >= min && count <= max;
}
