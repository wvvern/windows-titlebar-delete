#!/usr/bin/env bash
# Does the checks of docs/CODING_STANDARDS.md that Checkstyle cannot do, from the repository root.
set -u
fail=0

# The text checks examine all tracked text files, but not the tabs and line ends of .bat files, which use CRLF.
git grep -nIP '[^\x00-\x7F]' && { echo 'error: use only ASCII characters'; fail=1; }
git grep -nIP '\t' -- . ':!*.bat' && { echo 'error: use spaces, not tabs'; fail=1; }
git grep -nIP '[ \t\r]+$' -- . ':!*.bat' && { echo 'error: remove the spaces at the end of the line'; fail=1; }
while IFS= read -r -d '' f; do
    if [ -n "$(tail -c1 "$f")" ]; then echo "$f: error: add a newline at the end of the file"; fail=1; fi
done < <(git grep -zlI -e '')

# In each mixin, also in an inactive Stonecutter branch, the first line after an injector annotation that is not an
# annotation, blank or a Stonecutter marker must have a handler name that starts with wtd$.
git ls-files -z -- '*Mixin.java' | xargs -0 -r awk '
    FNR == 1 { p = 0; d = 0 }
    {
        s = $0; gsub(/"[^"]*"/, "", s)
        t = s; sub(/^[ \t]*(\/\*)?[ \t]*/, "", t)
        n = gsub(/\(/, "(", s) - gsub(/\)/, ")", s)
    }
    d > 0 { d += n; next }
    t ~ /^@(Inject|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyConstant|ModifyExpressionValue|ModifyReturnValue|WrapOperation|WrapWithCondition)\(/ { p = 1; d = n; next }
    p && t ~ /^@/ { d = n; next }
    p && (t ~ /^$/ || t ~ /^(\*\/)?\/\/\?/ || t ~ /^\*\/$/) { next }
    p {
        if (s !~ /wtd\$[A-Za-z0-9_]*[ \t]*\(/) { print FILENAME ":" FNR ": error: a handler name must start with wtd$"; bad = 1 }
        p = 0
    }
    END { exit bad }' || fail=1

exit $fail
