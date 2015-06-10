require_relative 'entry'
require_relative 'address'
require_relative 'website'
require 'mustache'

class Education < Entry
  attr_accessor :title, :source, :date, :location, :courses, :projects

  def initialize(hash)
    return unless hash.respond_to? :fetch

    @title    = hash.fetch('title',    nil)
    @source   = hash.fetch('source',   nil)
    @date     = hash.fetch('date',     nil)
    @location = hash.fetch('location', nil)
    @courses  = hash.fetch('courses',  nil)
    @projects = hash.fetch('projects', nil)

    @location = Address.new @location
    @courses = Entry.convert @courses
    @projects = Entry.convert @projects, Website
  end

  # Expects a tag with a number to open
  def course_lines()
    lambda do |text|
      line_length, template = Entry.parse_argument text
      line_length ||= 10

      lines = Entry.break_into_lines @courses, line_length

      Mustache.render(template, {'lines' => lines})
    end
  end
end
