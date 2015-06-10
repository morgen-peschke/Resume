require 'json'
require_relative 'ResumeFormatter/resume-formatter'

DATA_SOURCE = 'resume.json'

LATEX_TEMPLATE  = 'templates/standard.tex.mustache'
README_TEMPLATE = 'templates/README.md.mustache'

FORMATTER = ResumeFormatter
            .fromSource(JSON.parse(IO.read(DATA_SOURCE)))
            .reject('jobs') {|job| job['title'] == "Teaching Assistant"}

task default: %w(resume.pdf README.md)

file 'resume.pdf' => ['resume.json', 'latex/res.cls', LATEX_TEMPLATE, __FILE__] do
  FORMATTER
    .formatWith(LATEX_TEMPLATE)
    .saveAsPdf('resume.pdf')
end

file 'README.md' => ['resume.json', README_TEMPLATE, __FILE__] do
  FORMATTER
    .formatWith(README_TEMPLATE)
    .saveAsText('README.md')
end

task 'json' do
  require 'pp'
  pp FORMATTER.filteredData()

end

%w(resume.json latex/template.tex.mustache latex/res.cls).each do |extern|
  file "#{extern}" do |t| abort "FAILED! External dependency '#{t.name}' does not exist"; end
end
