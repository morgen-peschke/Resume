#!/bin/bash -Eeu
set -o pipefail

DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)

cd "$DIR"/formatter

mill ResumeFormatter.run \
     --source ../resume.json \
     --render ../templates/README.md.mustache \
     --to ../README.md

mill ResumeFormatter.run \
     --source ../resume.json \
     --render ../templates/standard.tex.mustache \
     --to ../latex/generated.tex

cd "$DIR/latex"

./pdflatex.sh -halt-on-error -output-format pdf generated.tex
./pdflatex.sh -halt-on-error -output-format pdf generated.tex

mv generated.pdf ../resume.pdf
rm generated.*
