Note To Future Me
=================

You'll need to install [Docker](https://docs.docker.com/get-docker/) and
[Mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) to
render the final resumes from JSON.

For Mill, if you've already got the JVM installed, use the Manual Install,
otherwise it'll download and install a JVM you probably won't use.

Other than that, it probably wouldn't be a bad idea to front-load some stuff,
so compiling the formatter and fetching the docker images is probably a
good idea.

```bash
docker pull blang/latex && (cd formatter && mill ResumeFormatter.compile)
```

If something goes wrong with the pdf rendering, check if there's been an
update to the [blog post that goes with the image](https://www.blang.io/posts/2015-04_docker-tooling-latex/).