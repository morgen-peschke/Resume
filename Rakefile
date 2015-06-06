require 'json'
require_relative 'ResumeFormatter/resume-formatter'

DATA_SOURCE = 'resume.json'

LATEX_TEMPLATE  = 'templates/standard.tex.mustache'
TXT_TEMPLATE    = 'templates/simple.txt.mustache'
README_TEMPLATE = 'templates/README.md.mustache'

FORMATTER = ResumeFormatter
            .fromSource(JSON.parse(IO.read(DATA_SOURCE)))
            .reject('jobs') {|job| job['title'] == "Teaching Assistant"}

task default: %w(resume.pdf resume.txt README.md)

file 'resume.pdf' => ['resume.json', 'latex/res.cls', LATEX_TEMPLATE] do
  FORMATTER
    .formatWith(LATEX_TEMPLATE)
    .saveAsPdf('resume.pdf')
end

file 'resume.txt' => ['resume.json', TXT_TEMPLATE] do
  FORMATTER
    .formatWith(TXT_TEMPLATE)
    .saveAsText('resume.txt')
end

file 'README.md' => ['resume.json', README_TEMPLATE] do
  FORMATTER
    .formatWith(README_TEMPLATE)
    .saveAsText('README.md')
end

%w(resume.json latex/template.tex.mustache latex/res.cls).each do |extern|
  file "#{extern}" do |t| abort "FAILED! External dependency '#{t.name}' does not exist"; end
end
