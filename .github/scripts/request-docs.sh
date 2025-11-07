#!/bin/bash
FILES=$(git diff --name-only main...HEAD -- 'src/main/java/**/*.java' 2>/dev/null || echo "")
if [ -z "$FILES" ]; then
    echo "✅ No changes"
    exit 0
fi

echo "📋 Changed files:"
echo "$FILES" | head -20
echo ""
echo "Prompt: Add docs (comments only) following 6 rules"
