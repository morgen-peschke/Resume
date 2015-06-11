require 'fileutils'
require_relative 'mustache-to-latex'

class ResumeWriter

  def initialize(template_file, data)
    @template_file = template_file
    @data = data
  end

  def saveAsText(outfile)
    template = IO.read(@template_file)
    resume = Mustache.render(template, @data)
    IO.write outfile, resume
  rescue StandardError => e
    STDERR.puts "#{e.class}: #{e.message}"
  end

  def saveAsPdf(outfile)
    source = MustacheToLatex.render(IO.read(@template_file), @data)

    FileUtils.mkdir 'latex' unless File.exists? 'latex'

    FileUtils.cd('latex') do
      IO.write 'generated_latex_source.tex', source

      0.upto(2).each do
        system('pdflatex',
          '-halt-on-error',
          '-output-format',
          'pdf',
          'generated_latex_source.tex')
        end
      FileUtils.mv 'generated_latex_source.pdf', "../#{outfile}"
      FileUtils.rm Dir['generated_latex_source.*']
    end
  rescue StandardError => e
    STDERR.puts "#{e.class}: #{e.message}"
  end
end
