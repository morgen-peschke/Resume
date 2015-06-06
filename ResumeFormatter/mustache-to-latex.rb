require 'mustache'

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
    str.gsub!(/\\t-e-x-t-b-a-c-k-s-l-a-s-h([^{])/,
              '\\textbackslash{}\1')
    str
  end
end
