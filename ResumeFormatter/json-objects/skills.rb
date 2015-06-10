require_relative 'entry'
require 'mustache'

class Skills
  attr_accessor :concepts, :languages, :frameworks

  def initialize(hash)
    return unless hash.respond_to? :fetch
    @concepts   = hash.fetch('concepts',   nil)
    @languages  = hash.fetch('languages',  nil)
    @frameworks = hash.fetch('frameworks', nil)

    @concepts   = Entry.convert @concepts
    @languages  = Entry.convert @languages
    @frameworks = Entry.convert @frameworks
  end

  # Expects a tag with a number to open
  def concept_lines()
    lambda do |text|
      line_length, template = Entry.parse_argument text
      line_length ||= 10

      lines = Entry.break_into_lines @concepts, line_length

      Mustache.render(template, {'lines' => lines})
    end
  end

  # Expects a tag with a number to open
  def language_lines()
    lambda do |text|
      line_length, template = Entry.parse_argument text
      line_length ||= 10

      lines = Entry.break_into_lines @languages, line_length

      Mustache.render(template, {'lines' => lines})
    end
  end
end
