require 'fileutils'
require 'mustache'
require 'json'

SOURCE  = ENV['SRC'] || ENV['SOURCE']
OUTFILE = ENV['OUT'] || ENV['OUTFILE']

LATEX_SOURCE  = SOURCE || 'templates/standard.tex.mustache'
TXT_SOURCE    = SOURCE || 'templates/simple.txt.mustache'
README_SOURCE = SOURCE || 'templates/README.md.mustache'

class MustacheToLatex < Mustache
  def escapeHTML(str)
    str.gsub!(/\\/, '\\t-e-x-t-b-a-c-k-s-l-a-s-h')
    #$stderr.puts str
    str.gsub!(/([#$%&_{}])/, '\\\\\\1')
    #$stderr.puts str
    str.gsub!(/\^/, '\\textasciicircum{}')
    #$stderr.puts str
    str.gsub!(/~/,  '\\textasciitilde{}')
    #$stderr.puts str
    str.gsub!(/\\t-e-x-t-b-a-c-k-s-l-a-s-h([^{])/, '\\textbackslash{}\1')
    str
  end
end

class GenerateResume
  def self.mark_first_and_last(obj)
    if obj.is_a?(Array)
      first, last = 0, obj.size - 1
      obj = obj.each_with_index.map do |e,i|
        if e.is_a?(Hash) and e.key?('row')
          e['first'] = true if i == first
          e['last']  = true if i == last
        else
          e = self.mark_first_and_last e
        end
        e
      end
    elsif obj.is_a?(Hash)
      obj = Hash[obj.map {|k,v| [k, self.mark_first_and_last(v)]}]
    end
    return obj
  end

  def self.json
    @@data ||= self.mark_first_and_last JSON.parse(IO.read("resume.json"))
  end

  def self.simple(template, outfile)
    source = Mustache.render(IO.read(template), self.json)
    IO.write(outfile, source)
  rescue StandardError => e
    STDERR.puts "#{e.class}: #{e.message}"
  end

  def self.latex(template, outfile)
    latex_source = MustacheToLatex.render(IO.read(template), self.json)
    FileUtils.cd('latex') do
      IO.write('temporary_resume.tex', latex_source)
      system('pdflatex', '-halt-on-error', '-output-format', 'pdf', 'temporary_resume.tex')
      FileUtils.mv 'temporary_resume.pdf', "../#{outfile}"
      FileUtils.rm Dir['temporary_resume.*']
    end
  rescue StandardError => e
    STDERR.puts "#{e.class}: #{e.message}"
  end
end

task default: %w(resume.pdf resume.txt README.md)

file 'resume.pdf' => ['resume.json', 'latex/res.cls', LATEX_SOURCE] do
  GenerateResume.latex LATEX_SOURCE, OUTFILE || 'resume.pdf'
end

file 'resume.txt' => ['resume.json', TXT_SOURCE] do
  GenerateResume.simple TXT_SOURCE, OUTFILE || 'resume.txt'
end

file 'README.md' => ['resume.json', README_SOURCE] do
  GenerateResume.simple README_SOURCE, OUTFILE || 'README.md'
end

%w(resume.json latex/template.tex.mustache latex/res.cls).each do |extern|
  file "#{extern}" do |t| abort "FAILED! External dependency '#{t.name}' does not exist"; end
end
