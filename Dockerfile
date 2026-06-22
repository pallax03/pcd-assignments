FROM debian:bookworm-slim

# Generic TeX environment for all assignments in this repository.
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        ca-certificates \
        latexmk \
        texlive-fonts-recommended \
        texlive-lang-italian \
        texlive-latex-base \
        texlive-latex-extra \
        texlive-latex-recommended \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

CMD ["latexmk", "-pdf", "-interaction=nonstopmode", "-halt-on-error", "main.tex"]