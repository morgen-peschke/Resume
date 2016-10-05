#!/bin/bash
which gpg >/dev/null || brew install gnupg2
which curl >/dev/null || brew install curl

which rvm >/dev/null || {
    \gpg \
        --keyserver hkp://keys.gnupg.net \
        --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3

    \curl -sSL https://get.rvm.io | bash -s
}
source ~/.rvm/scripts/rvm

RUBY_VERSION="$(cat .ruby-version)"
rvm use "$RUBY_VERSION" >/dev/null || {
    rvm install "$RUBY_VERSION"
}

gem install bundler
cd . # generate the gemset
bundle install

PDF_LATEX_PATH="/Library/TeX/texbin/pdflatex"
if [ ! -x $PDF_LATEX_PATH ]; then
    brew cask install mactex
fi

which pdflatex >/dev/null || (
    echo "Ensure that $PDF_LATEX_PATH is on the PATH"
)
